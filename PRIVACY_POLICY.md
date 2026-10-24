# Privacy Policy for Area Code Blocker

**Effective Date:** June 20, 2026  
**Last Updated:** October 24, 2026  

This Privacy Policy describes how the **Area Code Blocker** mobile application (the **App**) handles, processes, and protects your information. The App is designed to help you block unwanted phone calls, SMS messages, and notification alerts from specific area codes and keywords.

Your privacy is important to us. **Area Code Blocker uses a privacy-first architecture.** Your call screening, contacts list, SMS contents, and blocking preferences are processed and stored **locally on your device**. We do not transmit, upload, or sell your personal information to third-party servers.

---

## 1. Information Collection and Use

To perform its core functions of call blocking, SMS screening, and notification silencing, the App requires certain Android permissions. Below is a detailed disclosure of what permissions are used, why they are required, and how the data is handled.

### A. Contacts Access (`android.permission.READ_CONTACTS`)
* **Purpose:** The App reads your contact list to ensure that calls, SMS messages, and notifications from your saved contacts are **never blocked or silenced**, even if they match a blocked area code or keyword.
* **Data Handling:** This check is performed entirely offline, on-device. Your contacts are never uploaded, shared, or sent to any external server.

### B. SMS Notification Screening (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
* **Purpose:** The App screens notifications posted by messaging applications against your user-defined blocked area codes and blocked keywords. The App does not request permission to receive or read SMS messages directly.
* **Data Handling:** If a messaging notification matches your blocking criteria, the App dismisses the notification and may log the event locally so you can view it in the App's "Blocked Logs" dashboard. Relevant notification text and sender information remain on your device and are never sent to external servers.

### C. Phone State and Numbers (`android.permission.READ_PHONE_STATE`, `android.permission.READ_PHONE_NUMBERS`)
* **Purpose:** These permissions are used to auto-detect your own phone number and area code during onboarding or configuration. This helps the App suggest your own area code so you do not accidentally block yourself.
* **Data Handling:** Your phone number is read locally to populate the setup suggestions. This information is stored only in the App's secure private local preferences and is never transmitted off your device.

### D. Call Screening Service (`android.permission.BIND_SCREENING_SERVICE` / `ROLE_CALL_SCREENING`)
* **Purpose:** On Android 10 (API level 29) and above, the App uses the official Call Screening role to identify incoming call details in real-time.
* **Data Handling:** The incoming phone number is checked against your local contacts list and blocked area codes. If a match is found, the call is rejected, and a log entry is created locally. No call logs or caller numbers are transmitted externally.

### E. Notification Listener Service (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
* **Purpose:** The App uses a Notification Listener to screen incoming notification alerts from messaging applications (such as the default system SMS app). If a notification is triggered by a sender or text matching your blocked criteria, the App immediately dismisses/silences the notification to prevent unwanted alerts.
* **Data Handling:** The App inspects notification titles and body text in real-time on your device to execute the dismiss action. Matching notification content may be stored in the local blocked-events log but is not transmitted to any third party by the App.

---

## 2. Local Storage and Data Protection

All application settings, including:
* Your custom list of blocked area codes,
* Your custom list of blocked keywords,
* Local logs of blocked calls and SMS messages,
* Your configured preferences (e.g., app theme, own phone number),

are stored strictly in a secure **local SQLite database (Room DB)** and local private preferences on your Android device.

* **Android Sandbox:** Your data is protected by Android’s application sandbox, which prevents other apps on your device from accessing Area Code Blocker's database.
* **No Server Storage:** We do not host, operate, or maintain any databases or servers that collect or store your personal blocking configurations, call logs, or SMS details.

---

## 3. Independent Platform Processing

Your device’s operating system, app store, device manufacturer, mobile carrier, and related platform services—including Google services where applicable—may independently process installation, security, diagnostics, crash, or usage information under their own terms and privacy policies.

The developer does not control that independent processing. This statement does not mean that the App sends your contacts, caller details, message-notification content, rules, or blocked-event logs to those providers.

---

## 4. Data Sharing and Disclosure

We do not sell, trade, rent, or lease any of your personal data. The developer does not collect or store your personal data on developer-operated external servers and does not share it with advertising agencies.

---

## 5. Your Data Rights

Because all data is stored entirely on your device, you have complete control over your information:
* **Viewing Data:** You can view all blocked keywords, area codes, and blocked history directly within the App's user interface.
* **Deleting Data:** You can delete individual log items, clear your entire block list, or wipe the entire block history within the App.
* **Complete Erasure:** You can erase all data associated with the App at any time by uninstalling the App or clearing its storage in your device’s Android Settings (`Settings > Apps > Area Code Blocker > Storage > Clear Data`).

---

## 6. Children's Privacy

The App is not directed to or intended for children under the age of 13. We do not knowingly collect or solicit personal information from children under 13. We also do not store such data because all processing is restricted to your local device.

---

## 7. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. We will notify you of any changes by updating the "Effective Date" at the top of this document. We encourage you to review this Privacy Policy periodically for any changes.
