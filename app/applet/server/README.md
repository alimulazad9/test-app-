# AirDroid Parental Control - Render Server & Firebase Setup Guide

This server acts as the central signaling, command, and telemetry relay between:
1. **AirDroid Kids Android Background Service** (running on the child's phone).
2. **AirDroid Parent Web Dashboard** (accessible from any web browser).
3. **AirDroid Parent Android App** (managing devices remotely).
4. **Firebase Realtime Database** (optional cloud persistent sync).

---

## 1. Quick Deploy to Render (Free Tier)

### Method A: Deploy via GitHub (Recommended)
1. Push this repository to your **GitHub** account.
2. Go to [https://dashboard.render.com](https://dashboard.render.com) and log in.
3. Click **New +** -> **Web Service**.
4. Connect your GitHub repository.
5. Configure the settings:
   - **Name**: `airdroid-parental-server` (or your preferred name)
   - **Region**: Closest to you (e.g., Singapore, Frankfurt, Oregon)
   - **Branch**: `main`
   - **Root Directory**: `server`
   - **Runtime**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Plan**: `Free`
6. Click **Create Web Service**.
7. Once deployed, Render will provide your public HTTPS URL:
   `https://<your-service-name>.onrender.com`

---

## 2. Firebase Realtime Database Integration (Optional)

If you want data automatically saved to your Firebase Realtime Database:
1. Open the [Firebase Console](https://console.firebase.google.com).
2. Create or select your Firebase project.
3. In the left navigation, click **Build** -> **Realtime Database** -> **Create Database**.
4. Copy your Realtime Database URL, for example:
   `https://my-parental-project-default-rtdb.firebaseio.com/`
5. In your **Render Dashboard** -> Web Service -> **Environment**:
   - Add environment variable:
     - **Key**: `FIREBASE_DB_URL`
     - **Value**: `https://my-parental-project-default-rtdb.firebaseio.com`
6. Click **Save Changes**. The server will automatically sync every telemetry update, alert, and lock status to Firebase!

---

## 3. Configuring the Child Android Phone (AirDroid Kids)

1. Install and launch the **AirDroid Control** app on the child's Android phone.
2. At the top of the app, switch to **AirDroid Kids** mode (or toggle "This phone is: Child Device").
3. In the **Cloud Server Configuration** card:
   - Enter your Render Server URL:
     `https://<your-service-name>.onrender.com`
   - (Optional) Enter your Firebase Realtime Database URL if using direct Firebase sync.
4. Tap **"Start Background Protection Service"**.
5. The Android device will start a persistent foreground service with an ongoing notification (`KidsBackgroundService`), automatically registering with your Render backend and relaying:
   - Real-time battery percentage and charging state
   - Active foreground application name
   - Intercepted risk keywords (cyberbullying, dangerous meetings, adult content)
   - Listening for remote commands (`LOCK_DEVICE`, `UNLOCK_DEVICE`, `BLOCK_APP`)

---

## 4. Accessing the Parent Web Dashboard

Open your Render URL in **any browser** on your computer, iPhone, iPad, or Android:
`https://<your-service-name>.onrender.com`

The dashboard provides:
- Live battery, connection, and screen time metrics
- Live screen mirror preview
- Immediate popup and highlighted risk keyword alerts
- One-click **"Lock Child Phone"** button (which locks the child phone immediately via WebSocket push)
- Application limits and instant app blocker toggles

---

## 5. Local Testing (Without Deploying)

To run the server locally on your computer:
```bash
cd server
npm install
node server.js
```
Open [http://localhost:3000](http://localhost:3000) in your browser.
To connect an Android emulator or phone on the same Wi-Fi, enter your local machine's IP (e.g. `http://192.168.1.100:3000` or `http://10.0.2.2:3000` for Android emulator).
