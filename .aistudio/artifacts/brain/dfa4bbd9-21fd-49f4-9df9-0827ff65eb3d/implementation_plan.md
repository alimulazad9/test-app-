# AirDroid Parental Control & Kids Management Suite

A comprehensive dual-mode Android parental control application modeling the complete client-server architecture of AirDroid Parental Control and AirDroid Kids, with live screen mirroring simulation, keyword alert detection, app downtime/limit enforcement, and dynamic QR/pairing code synchronization.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural parameters have been confirmed based on user feedback:
> - **Dual-Mode Operation**: The application provides an instant role switcher between **Parent Dashboard (AirDroid Parental Control)** and **Child Device (AirDroid Kids)**, enabling realistic end-to-end testing and observation on a single Android device.
> - **Full Feature Suite**: Implementation encompasses remote live screen viewing, notification/keyword alert engine, application downtime & time limit enforcement, and device telemetry (battery, network, usage).
> - **Dynamic Pairing Flow**: Pairing uses a 9-digit rotating authentication code with visual QR code representation and one-tap binding verification.

---

## 1. Overview & Core Concept

- **What It Does**: Simulates and operates both ends of the AirDroid ecosystem. In **Parent Mode**, parents view real-time screen activity, configure usage limits, review keyword security alerts, and track device stats. In **Child Mode**, the app simulates the background services (Device Policy Admin, simulated MediaProjection screen streaming, Notification Listener, and Usage Enforcement overlay blocking).
- **Target Audience / Persona**: Parents seeking proactive digital well-being oversight for their children, and testers/evaluators wanting to see the AirDroid Parental Control mechanics in action.
- **Key Value**: Delivers tangible, reactive feedback between parent control rules and child device constraints without requiring two separate physical phones.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Role Switcher & Pairing**:
   - Top-level persistent mode toggle ("Parent Control" vs "Child (AirDroid Kids)").
   - Parent initiates pairing -> generates 9-digit token with countdown timer and interactive QR view.
   - Child mode enters or scans pairing token -> instant bidirectional binding with persistent state.
2. **Parent Dashboard & Live Screen Viewing**:
   - Summary cards: Child device battery status, online state, current active foreground app, screen time today.
   - Live Screen Mirroring module: low-latency animated frame feed showing child app activity with remote refresh and snapshot controls.
   - Real-time Alert feed: highlighted security notifications triggered by keyword violations (e.g. cyberbullying, dangerous search terms).
3. **App Rules & Downtime Enforcement**:
   - App Management catalog: set daily time limits (e.g., TikTok 30m, YouTube 45m) or toggle instant block.
   - Instant Device Lock: remote command locks the child screen with custom parental notice.
4. **Child Mode (AirDroid Kids Experience)**:
   - Status indicators: Active Protection foreground service status, simulated Device Admin & Accessibility listener active badges.
   - Interactive Simulated App Launcher: launch child mock apps (Social, Games, Browser, Messages).
   - Enforcement in Action: when launching a restricted app or during downtime, a system blocking overlay appears ("Time is up - AirDroid Parental Control locked this app").
   - Test Notification Generator: send mock chat messages containing safe vs flagged keywords to demonstrate immediate background alert triggers on the parent side.

### Visual Identity & Theme

- **Aesthetic Direction**: High-trust, modern security dashboard aesthetic inspired by AirDroid and Google Family Link. Clean emerald green and slate accents symbolizing safety, clarity, and tech-forward oversight.
- **Color Palette & Design Tokens**:
  - `Primary`: `#00A86B` (AirDroid Emerald Green)
  - `OnPrimary`: `#FFFFFF`
  - `PrimaryContainer`: `#E0F5EB` / Dark `#0B3D2B`
  - `Secondary`: `#1E88E5` (Active Stream Blue)
  - `Surface`: Light `#F8FBF9` / Dark `#121614`
  - `SurfaceVariant`: Light `#EEF3F0` / Dark `#1E2522`
  - `Error / Risk`: `#E53935` (Keyword Security Alert Red)
  - `Warning`: `#FB8C00` (Usage Threshold Amber)
- **Typography & Rhythm**:
  - M3 Typography hierarchy with clear numerical display for pairing codes, countdown timers, and screen time metrics.
  - Card elevation with soft rounded corners (`16.dp` to `20.dp`), crisp status chips, and edge-to-edge safe area padding.
- **Interactive Feedback & Motion**:
  - Smooth animated transitions between Parent and Child views.
  - Pulsing radar badge on live screen streaming session.
  - Immediate toast and alert banner arrivals upon keyword interception.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Unified State Repository (Shared Flow Engine)**
  - *Chosen Approach*: A shared singleton state & Flow repository syncing events between Parent and Child modes instantly.
  - *Why*: Allows instantaneous demonstration of push commands (e.g., locking child device immediately locks Child view; triggering a message in Child immediately pops alert in Parent).
  - *Alternatives Considered*: Independent apps requiring multi-device networking — rejected because it requires two devices and separate emulator instances.
- **Decision 2: Realistic Screen Mirroring Simulation**
  - *Chosen Approach*: High-fidelity simulated frame buffer renderer mirroring child's active mock environment with dynamic updates and latency telemetry.
  - *Why*: MediaProjection API requires user consent dialogs and cannot easily stream to the same app process without WebRTC loopback complexity; this delivers the exact UX of AirDroid's screen viewer.
- **Decision 3: Interactive Keyword Test Suite**
  - *Chosen Approach*: Built-in quick simulator allowing one-tap generation of incoming chat messages (e.g. WhatsApp, Instagram, SMS) containing flagged terms like "bully", "kill", "meet alone".
  - *Why*: Allows instantaneous verification of the NotificationListenerService and Keyword Detection Engine architecture.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        AirDroid Parental Suite                         │
│                                                                        │
│  ┌──────────────────────────────┐    ┌──────────────────────────────┐  │
│  │   Parent View (Dashboard)    │    │   Child View (AirDroid Kids) │  │
│  │                              │    │                              │  │
│  │ • Live Screen Viewer         │    │ • Background Service Badge   │  │
│  │ • App Limits & Downtime      │    │ • Mock Child Apps Launcher   │  │
│  │ • Keyword Risk Alert Center  │    │ • Enforcement Lock Overlay   │  │
│  │ • Device Telemetry & Pairing │    │ • Notification Simulator     │  │
│  └──────────────▲───────────────┘    └──────────────┬───────────────┘  │
│                 │                                   │                  │
│                 │   Control Commands & Rule Sync    │                  │
│                 ├───────────────────────────────────┤                  │
│                 │   Screen Frames & Alert Payloads  │                  │
│                 ▼                                   ▼                  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                 AirDroid Sync & Rule Engine                      │  │
│  │  • Pairing & Session State (9-digit rotating token)              │  │
│  │  • Keyword Rules Engine (RegEx & matching risk categories)       │  │
│  │  • App Quota & Time Tracking Store                               │  │
│  │  • Device Status Store (Battery, Network, Active App, Lock)      │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Core Entities

- `ChildDeviceProfile`: ID, name, pairingCode, pairedAt, isOnline, batteryLevel, isLocked, activeAppPackage.
- `AppRule`: packageName, appName, iconType, dailyLimitMinutes, usedMinutesToday, isBlocked.
- `SecurityAlert`: id, timestamp, appSource, sender, messageText, matchedKeyword, riskLevel (High, Medium, Low).
- `KeywordRule`: id, phrase, category (Bullying, Adult Content, Self-Harm, Suspicious), isEnabled.
- `ScreenFrameState`: currentApp, screenImageRes/CanvasType, fps, latencyMs, isStreamingActive.

### Interactive State Transitions

- **Lock Device**: Parent taps "Lock Device" -> `isLocked = true` -> Child mode immediately renders the persistent blocking lockscreen until unlocked.
- **Trigger Message**: Child sends mock message -> Keyword Detection Engine scans text against `KeywordRule`s -> match produces `SecurityAlert` -> Parent alert badge and alert list updates in real time.
- **App Usage Enforcement**: Child selects an app (e.g. "Game") -> Engine checks `AppRule` -> If `usedMinutesToday >= dailyLimit` or `isBlocked == true`, displays overlay blocker with "Ask for more time" option.
