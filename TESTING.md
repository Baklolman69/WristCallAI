# ⌚ WristCall AI — Hackathon Judging & Testing Guide

> **Autonomous AI Phone Call Delegation Assistant for Wear OS**  
> *Empowering smartwatches to search, discover, call, and converse with real-world businesses on your behalf.*

---

## 🔑 Out-of-the-Box API & Key Setup

To ensure zero-friction testing for hackathon judges:
- **Simulation DEMO Mode**: Tap **"Use Simulation DEMO Key"** during setup to instantly test all Wear OS UI screens, voice animations, status transitions, and transcripts offline.
- **Custom API Key Setup**: Enter or paste your CALL-E, SerpApi, or Groq API keys via the onboarding screen or anytime by tapping the ⚙️ **Settings Gear Icon** in the top-right corner of the home screen.

---

## 🚀 How to Test the Application

### ⌚ Method A: On a Physical Wear OS Smartwatch

1. **Hardware Quick-Launch**:
   - Double-press the physical side crown button on your smartwatch $\rightarrow$ WristCall AI opens directly into mic listening mode.
2. **Voice Input**:
   - Speak naturally into your watch mic:
     > *"Call Cattleack Barbeque in Farmers Branch and ask opening hours and best sellers"*  
     > *(or "Call Pizza Hut in Dallas to check available pizzas")*
3. **Automatic Execution**:
   - The app automatically discovers the phone number via Google, refines call instructions via Groq AI 120B, dispatches the phone call via CALL-E, and delivers live transcripts & voice summaries!

---

### 💻 Method B: On Android Studio Emulator (`Wear_Jp`)

For testing on an Android emulator:

#### **Option 1: 2-Tap Mic Demo (Fastest)**
1. **Tap 1**: Click the central **Mic 🎙️** button on the watch home screen $\rightarrow$ Activates **`🎙 Speaking...`** mode with live 3-word sliding marquee animation.
2. **Tap 2**: Click the mic button again $\rightarrow$ Instantly dispatches the AI call!

#### **Option 2: Quick-Action Preset Cards**
1. Scroll down on the watch screen to view the **2 Quick-Action Cards** (*Cattleack Barbeque* & *Pizza Hut*).
2. Tap any card for **1-click instant call dispatch**.
3. *(Optional)* Tap the **✏️ Edit** button to edit the target name or phone number live on local storage.

#### **Option 3: Type Custom Prompt & Phone Input**
1. Scroll down to the **"Type Custom Prompt & Phone"** section on the watch screen.
2. Enter custom test fields:
   - **Prompt Field**: Enter any custom task (e.g., *"Call Pizza Hut in Dallas to check available pizzas"*).
   - **Phone Field (Optional)**: Enter a direct E.164 phone number (e.g., `+15550199000`). Leave blank if you want Google SerpApi to auto-discover the number!
3. Tap **📋 Paste from Clipboard** to instantly paste any long prompt copied on your device.
4. Tap the **🗣️ Dispatch CALL-E Call** button to initiate execution immediately.

---

## 🔍 Step-by-Step Expected Verification Flow

When a call is dispatched, judges can verify the full 5-stage pipeline on the watch screen:

```
[1/5] 🔍 Searching Google via SerpApi (Discovering Phone & Location)
       ↓
[2/5] 🧠 Groq AI 120B Synthesis (Refining Prompt & Business Intent)
       ↓
[3/5] 📡 Dispatching CALL-E Call (Routing to Telecom Network)
       ↓
[4/5] 📞 AI Agent in Live Conversation (Real-Time Status Polling)
       ↓
[5/5] ✨ Executive 3-Bullet Summary & Audio Readout (TTS + Transcript)
```

---

## 🌟 Key Features for Judges to Look For

- **🎙️ 3-Word Right-to-Left Speech Marquee**: Dynamic sliding word window (`[Word 1] [Word 2] [Word 3]`) while speaking.
- **⚡ Instant Hardware Double-Click Trigger**: Double-pressing the physical side crown opens the app and starts mic listening under 1 second.
- **🔍 Zero-Phone-Number Phone Discovery**: Users only supply a business name; **SerpApi + Groq AI** automatically discover verified E.164 phone numbers and location snippets online.
- **📜 Real-Time Transcript & 🔊 Recording Playback**: Full turn-by-turn dialogue extraction with a **Listen Recording** button for audio playback of real calls.
- **✨ Executive 3-Bullet Summary Screen**: Dark slate & neon mint Wear OS aesthetic featuring automatic `TextToSpeech` voice readouts.
