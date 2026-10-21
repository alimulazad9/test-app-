const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const path = require('path');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

const PORT = process.env.PORT || 3000;
const FIREBASE_DB_URL = process.env.FIREBASE_DB_URL || ''; // Optional: https://<PROJECT_ID>.firebaseio.com

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.static(path.join(__dirname, 'public')));

// In-Memory Data Store
const state = {
  devices: {
    'AIR-KID-8831': {
      deviceId: 'AIR-KID-8831',
      childName: "Leo's Phone",
      modelName: 'Pixel 8 Pro',
      batteryLevel: 82,
      isCharging: false,
      isOnline: true,
      isDeviceLocked: false,
      activeAppName: 'ChatApp Social',
      activePackage: 'com.mock.social',
      lastSeen: Date.now()
    }
  },
  alerts: [
    {
      id: 'alert-init-1',
      timestamp: Date.now() - 1000 * 60 * 15,
      deviceId: 'AIR-KID-8831',
      appSource: 'ChatApp Social',
      sender: '@unknown_shadow',
      messageText: "Hey are you alone? don't tell parents and let's meet alone at the park",
      matchedKeyword: "don't tell parents",
      riskLevel: 'HIGH',
      isDismissed: false
    }
  ],
  commands: {}, // deviceId -> [command objects]
  appRules: {
    'AIR-KID-8831': [
      { packageName: 'com.mock.tiktok', appName: 'TikVid Stream', dailyLimitMinutes: 45, usedMinutesToday: 40, isBlocked: false },
      { packageName: 'com.mock.social', appName: 'ChatApp Social', dailyLimitMinutes: 60, usedMinutesToday: 60, isBlocked: false },
      { packageName: 'com.mock.game', appName: 'BlockCraft World', dailyLimitMinutes: 60, usedMinutesToday: 25, isBlocked: false },
      { packageName: 'com.mock.secretchat', appName: 'Ghost Messenger', dailyLimitMinutes: 30, usedMinutesToday: 0, isBlocked: true }
    ]
  },
  latestFrames: {}
};

// WebSocket connections mapping
const clients = {
  parents: new Set(),
  children: new Map() // deviceId -> WebSocket
};

// Optional helper to sync data to Firebase Realtime Database via REST
async function syncToFirebase(path, data) {
  if (!FIREBASE_DB_URL) return;
  try {
    const url = `${FIREBASE_DB_URL.replace(/\/$/, '')}/${path}.json`;
    await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
  } catch (err) {
    console.error('Firebase RTDB sync error:', err.message);
  }
}

// WebSocket Signaling & Real-Time Events
wss.on('connection', (ws, req) => {
  let clientRole = null;
  let clientDeviceId = null;

  ws.on('message', (message) => {
    try {
      const payload = JSON.parse(message);
      
      if (payload.type === 'IDENTIFY') {
        clientRole = payload.role; // 'PARENT' or 'CHILD'
        if (clientRole === 'PARENT') {
          clients.parents.add(ws);
          ws.send(JSON.stringify({ type: 'INIT_STATE', data: state }));
        } else if (clientRole === 'CHILD') {
          clientDeviceId = payload.deviceId || 'AIR-KID-8831';
          clients.children.set(clientDeviceId, ws);
          if (state.devices[clientDeviceId]) {
            state.devices[clientDeviceId].isOnline = true;
            state.devices[clientDeviceId].lastSeen = Date.now();
          }
          broadcastToParents({ type: 'DEVICE_ONLINE', deviceId: clientDeviceId });
        }
      } else if (payload.type === 'CHILD_TELEMETRY') {
        const { deviceId, battery, isCharging, isLocked, activeApp, activePackage } = payload;
        if (!state.devices[deviceId]) {
          state.devices[deviceId] = { deviceId, childName: payload.childName || 'Child Device' };
        }
        Object.assign(state.devices[deviceId], {
          batteryLevel: battery,
          isCharging,
          isDeviceLocked: isLocked,
          activeAppName: activeApp,
          activePackage,
          isOnline: true,
          lastSeen: Date.now()
        });
        broadcastToParents({ type: 'TELEMETRY_UPDATE', device: state.devices[deviceId] });
        syncToFirebase(`devices/${deviceId}`, state.devices[deviceId]);
      } else if (payload.type === 'SECURITY_ALERT') {
        state.alerts.unshift(payload.alert);
        broadcastToParents({ type: 'NEW_ALERT', alert: payload.alert });
        syncToFirebase(`alerts/${payload.alert.id}`, payload.alert);
      } else if (payload.type === 'SCREEN_FRAME') {
        state.latestFrames[payload.deviceId] = payload.frame;
        broadcastToParents({ type: 'SCREEN_FRAME', deviceId: payload.deviceId, frame: payload.frame });
      } else if (payload.type === 'PARENT_COMMAND') {
        // Parent sent remote command (LOCK, UNLOCK, BLOCK_APP)
        const targetWs = clients.children.get(payload.deviceId);
        if (targetWs && targetWs.readyState === WebSocket.OPEN) {
          targetWs.send(JSON.stringify({ type: 'EXECUTE_COMMAND', command: payload.command }));
        } else {
          // Queue command
          if (!state.commands[payload.deviceId]) state.commands[payload.deviceId] = [];
          state.commands[payload.deviceId].push(payload.command);
        }
      }
    } catch (err) {
      console.error('WS Error:', err.message);
    }
  });

  ws.on('close', () => {
    if (clientRole === 'PARENT') {
      clients.parents.delete(ws);
    } else if (clientRole === 'CHILD' && clientDeviceId) {
      clients.children.delete(clientDeviceId);
      if (state.devices[clientDeviceId]) {
        state.devices[clientDeviceId].isOnline = false;
        broadcastToParents({ type: 'DEVICE_OFFLINE', deviceId: clientDeviceId });
      }
    }
  });
});

function broadcastToParents(messageObj) {
  const msgString = JSON.stringify(messageObj);
  clients.parents.forEach((ws) => {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(msgString);
    }
  });
}

// REST Endpoints for Child Android App (works even without persistent WebSocket)
app.post('/api/child/telemetry', (req, res) => {
  const { deviceId, battery, isCharging, isLocked, activeApp, activePackage, childName, modelName } = req.body;
  if (!deviceId) return res.status(400).json({ error: 'Missing deviceId' });

  if (!state.devices[deviceId]) {
    state.devices[deviceId] = {
      deviceId,
      childName: childName || "Child's Phone",
      modelName: modelName || 'Android Device'
    };
  }

  Object.assign(state.devices[deviceId], {
    batteryLevel: battery !== undefined ? battery : state.devices[deviceId].batteryLevel,
    isCharging: isCharging !== undefined ? isCharging : state.devices[deviceId].isCharging,
    isDeviceLocked: isLocked !== undefined ? isLocked : state.devices[deviceId].isDeviceLocked,
    activeAppName: activeApp || state.devices[deviceId].activeAppName,
    activePackage: activePackage || state.devices[deviceId].activePackage,
    isOnline: true,
    lastSeen: Date.now()
  });

  broadcastToParents({ type: 'TELEMETRY_UPDATE', device: state.devices[deviceId] });
  syncToFirebase(`devices/${deviceId}`, state.devices[deviceId]);

  // Return any pending commands for the child device
  const queued = state.commands[deviceId] || [];
  state.commands[deviceId] = [];

  res.json({
    success: true,
    isLocked: state.devices[deviceId].isDeviceLocked,
    pendingCommands: queued,
    appRules: state.appRules[deviceId] || []
  });
});

app.post('/api/child/alert', (req, res) => {
  const alert = req.body;
  if (!alert || !alert.id) return res.status(400).json({ error: 'Invalid alert payload' });

  state.alerts.unshift(alert);
  broadcastToParents({ type: 'NEW_ALERT', alert });
  syncToFirebase(`alerts/${alert.id}`, alert);

  res.json({ success: true, message: 'Alert recorded' });
});

app.post('/api/child/screen-frame', (req, res) => {
  const { deviceId, frame } = req.body;
  if (deviceId && frame) {
    state.latestFrames[deviceId] = frame;
    broadcastToParents({ type: 'SCREEN_FRAME', deviceId, frame });
  }
  res.json({ success: true });
});

// REST Endpoints for Parent Web Dashboard
app.get('/api/parent/state', (req, res) => {
  res.json(state);
});

app.get('/api/parent/devices', (req, res) => {
  res.json(Object.values(state.devices));
});

app.post('/api/parent/command', (req, res) => {
  const { deviceId, action, params } = req.body;
  if (!deviceId || !action) return res.status(400).json({ error: 'Missing parameters' });

  const command = { action, params, timestamp: Date.now() };

  // Update server state accordingly
  if (action === 'LOCK_DEVICE') {
    if (state.devices[deviceId]) state.devices[deviceId].isDeviceLocked = true;
  } else if (action === 'UNLOCK_DEVICE') {
    if (state.devices[deviceId]) state.devices[deviceId].isDeviceLocked = false;
  } else if (action === 'BLOCK_APP') {
    const rules = state.appRules[deviceId] || [];
    const found = rules.find(r => r.packageName === params.packageName);
    if (found) found.isBlocked = params.isBlocked;
  }

  // Push via WebSocket if child is connected
  const childWs = clients.children.get(deviceId);
  if (childWs && childWs.readyState === WebSocket.OPEN) {
    childWs.send(JSON.stringify({ type: 'EXECUTE_COMMAND', command }));
  } else {
    if (!state.commands[deviceId]) state.commands[deviceId] = [];
    state.commands[deviceId].push(command);
  }

  broadcastToParents({ type: 'COMMAND_ISSUED', deviceId, command });
  if (state.devices[deviceId]) syncToFirebase(`devices/${deviceId}`, state.devices[deviceId]);

  res.json({ success: true, command });
});

app.post('/api/parent/dismiss-alert', (req, res) => {
  const { alertId } = req.body;
  const alert = state.alerts.find(a => a.id === alertId);
  if (alert) {
    alert.isDismissed = true;
    broadcastToParents({ type: 'ALERT_DISMISSED', alertId });
  }
  res.json({ success: true });
});

server.listen(PORT, () => {
  console.log(`===============================================`);
  console.log(` AirDroid Parental Control Server Running`);
  console.log(` Local URL: http://localhost:${PORT}`);
  console.log(` Deploy Target: Render.com (Node Web Service)`);
  console.log(` WebSocket Signaling: Active on ws://localhost:${PORT}`);
  console.log(`===============================================`);
});
