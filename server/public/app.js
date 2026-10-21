const TARGET_DEVICE_ID = 'AIR-KID-8831';
let serverState = {
  devices: {},
  alerts: [],
  appRules: {}
};

let ws = null;

function initWebSocket() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = `${protocol}//${window.location.host}`;

  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    const textEl = document.getElementById('wsStatusText');
    if (textEl) textEl.innerText = 'Connected to Render';
    const ind = document.querySelector('.server-status-card .status-indicator');
    if (ind) ind.className = 'status-indicator live';
    ws.send(JSON.stringify({ type: 'IDENTIFY', role: 'PARENT' }));
  };

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data);
      if (msg.type === 'INIT_STATE') {
        serverState = msg.data;
        updateUI();
      } else if (msg.type === 'TELEMETRY_UPDATE') {
        serverState.devices[msg.device.deviceId] = msg.device;
        updateUI();
      } else if (msg.type === 'NEW_ALERT') {
        serverState.alerts.unshift(msg.alert);
        updateUI();
      } else if (msg.type === 'COMMAND_ISSUED') {
        if (msg.command.action === 'LOCK_DEVICE') {
          if (serverState.devices[msg.deviceId]) serverState.devices[msg.deviceId].isDeviceLocked = true;
        } else if (msg.command.action === 'UNLOCK_DEVICE') {
          if (serverState.devices[msg.deviceId]) serverState.devices[msg.deviceId].isDeviceLocked = false;
        }
        updateUI();
      }
    } catch (err) {
      console.error('WS Parse Error:', err);
    }
  };

  ws.onclose = () => {
    const textEl = document.getElementById('wsStatusText');
    if (textEl) textEl.innerText = 'Reconnecting...';
    const ind = document.querySelector('.server-status-card .status-indicator');
    if (ind) ind.className = 'status-indicator danger';
    setTimeout(initWebSocket, 3000);
  };
}

async function fetchServerState() {
  try {
    const res = await fetch('/api/parent/state');
    if (res.ok) {
      serverState = await res.json();
      updateUI();
    }
  } catch (e) {
    console.error('REST fetch state error:', e);
  }
}

function updateUI() {
  const device = serverState.devices[TARGET_DEVICE_ID] || Object.values(serverState.devices)[0];
  if (!device) return;

  const batEl = document.getElementById('batteryVal');
  if (batEl) batEl.innerText = `${device.batteryLevel || 82}%`;

  const phoneBatEl = document.getElementById('phoneBatText');
  if (phoneBatEl) phoneBatEl.innerText = `${device.batteryLevel || 82}%`;

  const isOnline = device.isOnline !== false;
  const onlineEl = document.getElementById('onlineVal');
  if (onlineEl) onlineEl.innerText = isOnline ? 'ONLINE' : 'OFFLINE';

  const onInd = document.getElementById('onlineIndicator');
  if (onInd) onInd.className = isOnline ? 'status-indicator live' : 'status-indicator';

  const appName = device.activeAppName || 'Home Screen';
  const activeAppEl = document.getElementById('activeAppVal');
  if (activeAppEl) activeAppEl.innerText = appName;

  const phoneTitleEl = document.getElementById('phoneAppTitle');
  if (phoneTitleEl) phoneTitleEl.innerText = appName;

  const fullActiveEl = document.getElementById('fullActiveAppName');
  if (fullActiveEl) fullActiveEl.innerText = appName;

  const isLocked = device.isDeviceLocked === true;
  const lockStatusEl = document.getElementById('lockStatusVal');
  if (lockStatusEl) {
    lockStatusEl.innerText = isLocked ? 'LOCKED' : 'ACTIVE';
    lockStatusEl.style.color = isLocked ? '#E53935' : '#00A86B';
  }

  const btnLock = document.getElementById('btnLockToggle');
  const btnText = document.getElementById('lockButtonText');
  if (btnLock && btnText) {
    if (isLocked) {
      btnText.innerText = 'Unlock Child Phone';
      btnLock.className = 'btn btn-primary';
    } else {
      btnText.innerText = 'Lock Child Phone';
      btnLock.className = 'btn btn-danger';
    }
  }

  const activeAlerts = serverState.alerts.filter(a => !a.isDismissed);
  const badgeEl = document.getElementById('alertsBadge');
  if (badgeEl) badgeEl.innerText = activeAlerts.length;

  const recentCard = document.getElementById('recentAlertCard');
  const recentMsg = document.getElementById('recentAlertMsg');
  if (recentCard && recentMsg) {
    if (activeAlerts.length > 0) {
      const top = activeAlerts[0];
      recentCard.style.display = 'flex';
      recentMsg.innerText = `Flagged keyword "${top.matchedKeyword}" intercepted in ${top.appSource} from ${top.sender}`;
    } else {
      recentCard.style.display = 'none';
    }
  }

  renderAlertsTable(activeAlerts);
  renderAppLimits(serverState.appRules[TARGET_DEVICE_ID] || []);
}

function renderAlertsTable(alerts) {
  const container = document.getElementById('alertsTable');
  if (!container) return;

  if (alerts.length === 0) {
    container.innerHTML = '<p style="color:#55625B; padding:20px; text-align:center">No active risk alerts recorded from child device.</p>';
    return;
  }

  container.innerHTML = alerts.map(a => `
    <div class="alert-item">
      <div class="alert-item-left">
        <span class="alert-source">${a.appSource} • From ${a.sender}</span>
        <p class="alert-text">${highlightKeyword(a.messageText, a.matchedKeyword)}</p>
      </div>
      <div>
        <button class="btn btn-outline" onclick="dismissAlert('${a.id}')">Dismiss</button>
      </div>
    </div>
  `).join('');
}

function highlightKeyword(text, keyword) {
  if (!keyword) return text;
  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, '<span class="alert-highlight">$1</span>');
}

function renderAppLimits(rules) {
  const container = document.getElementById('overviewLimitsList');
  const manager = document.getElementById('appRulesManager');

  if (container) {
    container.innerHTML = rules.map(r => `
      <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid #DFE8E2; font-size:0.85rem">
        <span><strong>${r.appName}</strong></span>
        <span style="color:${r.isBlocked ? '#E53935' : '#00A86B'}">${r.isBlocked ? 'Blocked' : `${r.usedMinutesToday}m / ${r.dailyLimitMinutes}m`}</span>
      </div>
    `).join('');
  }

  if (manager) {
    manager.innerHTML = rules.map(r => `
      <div class="rule-row">
        <div class="rule-info">
          <h4>${r.appName}</h4>
          <span>Usage: ${r.usedMinutesToday}m / ${r.dailyLimitMinutes}m limit</span>
        </div>
        <button class="btn ${r.isBlocked ? 'btn-primary' : 'btn-danger'}" onclick="toggleAppBlock('${r.packageName}', ${!r.isBlocked})">
          ${r.isBlocked ? 'Unblock App' : 'Block App'}
        </button>
      </div>
    `).join('');
  }
}

async function sendCommand(action, params = {}) {
  const payload = {
    deviceId: TARGET_DEVICE_ID,
    action,
    params
  };

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'PARENT_COMMAND', ...payload, command: { action, params } }));
  }

  try {
    await fetch('/api/parent/command', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  } catch (e) {
    console.error('Send command error:', e);
  }
  fetchServerState();
}

async function dismissAlert(alertId) {
  try {
    await fetch('/api/parent/dismiss-alert', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ alertId })
    });
    fetchServerState();
  } catch (e) {
    console.error('Dismiss alert error:', e);
  }
}

function toggleAppBlock(packageName, isBlocked) {
  sendCommand('BLOCK_APP', { packageName, isBlocked });
}

const btnLock = document.getElementById('btnLockToggle');
if (btnLock) {
  btnLock.addEventListener('click', () => {
    const device = serverState.devices[TARGET_DEVICE_ID];
    const currentlyLocked = device ? device.isDeviceLocked : false;
    sendCommand(currentlyLocked ? 'UNLOCK_DEVICE' : 'LOCK_DEVICE');
  });
}

const btnClear = document.getElementById('btnClearAlerts');
if (btnClear) {
  btnClear.addEventListener('click', () => {
    serverState.alerts.forEach(a => a.isDismissed = true);
    updateUI();
  });
}

function switchTab(tabId) {
  document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));

  const targetTab = document.getElementById(`tab-${tabId}`);
  if (targetTab) targetTab.classList.add('active');

  const navBtn = document.querySelector(`[data-tab="${tabId}"]`);
  if (navBtn) navBtn.classList.add('active');

  const titles = {
    dashboard: ['Child Phone Overview', 'Live status relayed from AirDroid Kids on Render'],
    screen: ['Live Screen Mirroring', 'H.264 WebRTC Stream from Child MediaProjection'],
    alerts: ['Risk Keyword Alerts', 'NotificationListenerService intercepted messages'],
    limits: ['Application Quotas & Remote Blocker', 'Enforce daily time limits on child phone'],
    api: ['Server Setup & Firebase Deployment', 'Render.com deployment guide and API configuration']
  };

  if (titles[tabId]) {
    const titleEl = document.getElementById('pageTitle');
    const subEl = document.getElementById('pageSubtitle');
    if (titleEl) titleEl.innerText = titles[tabId][0];
    if (subEl) subEl.innerText = titles[tabId][1];
  }
}

document.querySelectorAll('.nav-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    switchTab(btn.dataset.tab);
  });
});

initWebSocket();
fetchServerState();
setInterval(fetchServerState, 5000);
