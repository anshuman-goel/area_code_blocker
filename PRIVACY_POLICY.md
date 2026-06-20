# Privacy Policy for Area Code Blocker

**Effective Date:** June 20, 2026  
**Last Updated:** June 20, 2026  

This Privacy Policy describes how the **Area Code Blocker** mobile application ("the App", "we", "us", "our") handles, processes, and protects your information. The App is designed to help you block unwanted phone calls, SMS messages, and notification alerts from specific area codes and keywords.

Your privacy is of paramount importance to us. **Area Code Blocker is built on a "Privacy-First" architecture.** This means that your call screening, contacts list, SMS contents, and blocking preferences are processed and stored **locally on your device**, and we do not transmit, upload, or sell your personal information to any third-party servers.

---

## 1. Information Collection and Use

To perform its core functions of call blocking, SMS screening, and notification silencing, the App requires certain Android permissions. Below is a detailed disclosure of what permissions are used, why they are required, and how the data is handled.

### A. Contacts Access (`android.permission.READ_CONTACTS`)
* **Purpose:** The App reads your contact list to ensure that calls, SMS messages, and notifications from your saved contacts are **never blocked or silenced**, even if they match a blocked area code or keyword.
* **Data Handling:** This check is performed entirely offline, on-device. Your contacts are never uploaded, shared, or sent to any external server.

### B. SMS Reception and Blocking (`android.permission.RECEIVE_SMS`)
* **Purpose:** The App intercepts incoming SMS messages to screen them against your user-defined blocked area codes and blocked keywords.
* **Data Handling:** If an incoming message matches your blocking criteria, the App silences the message and logs the event locally so you can view it in the App's "Blocked Logs" dashboard. Your SMS text contents and sender information remain securely on your device and are never sent to external servers.

### C. Phone State and Numbers (`android.permission.READ_PHONE_STATE`, `android.permission.READ_PHONE_NUMBERS`)
* **Purpose:** These permissions are used to auto-detect your own phone number and area code during onboarding or configuration. This helps the App suggest your own area code so you do not accidentally block yourself.
* **Data Handling:** Your phone number is read locally to populate the setup suggestions. This information is stored only in the App's secure private local preferences and is never transmitted off your device.

### D. Call Screening Service (`android.permission.BIND_SCREENING_SERVICE` / `ROLE_CALL_SCREENING`)
* **Purpose:** On Android 10 (API level 29) and above, the App uses the official Call Screening role to identify incoming call details in real-time.
* **Data Handling:** The incoming phone number is checked against your local contacts list and blocked area codes. If a match is found, the call is rejected, and a log entry is created locally. No call logs or caller numbers are transmitted externally.

### E. Notification Listener Service (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
* **Purpose:** The App uses a Notification Listener to screen incoming notification alerts from messaging applications (such as the default system SMS app). If a notification is triggered by a sender or text matching your blocked criteria, the App immediately dismisses/silences the notification to prevent unwanted alerts.
* **Data Handling:** The App inspects notification titles and body text in real-time on your device to execute the dismiss action. No notification content is read, stored permanently, or transmitted to any third party.

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

## 3. Third-Party Services and AI APIs

The App includes optional advanced integrations, such as the Google Gemini AI API, to assist with features like spam classification or smart filtering.
* **API Access:** If you configure the App to use Gemini AI API calls, requests are made directly from your device to the Google Gemini API using the API key you provide.
* **Data Scope:** Only the specific, user-initiated text content required for the AI query is sent. No personal identifier data, contacts lists, or unblocked call history is ever sent to the API.
* **Google Privacy Policy:** Interactions with the Gemini API are governed by Google's Privacy Policy. We recommend reviewing their terms regarding data usage.

---

## 4. Data Sharing and Disclosure

We do not sell, trade, rent, or lease any of your personal data. Because we do not collect or store your personal data on any external servers, it is physically impossible for us to share or disclose your data to third parties, law enforcement, or advertising agencies.

---

## 5. Your Data Rights

Because all data is stored entirely on your device, you have complete control over your information:
* **Viewing Data:** You can view all blocked keywords, area codes, and blocked history directly within the App's user interface.
* **Deleting Data:** You can delete individual log items, clear your entire block list, or wipe the entire block history within the App.
* **Complete Erasure:** You can erase all data associated with the App at any time by uninstalling the App or clearing its storage in your device’s Android Settings (`Settings > Apps > Area Code Blocker > Storage > Clear Data`).

---

## 6. Children's Privacy

The App is not directed to, or intended for, children under the age of 13. We do not knowingly collect or solicit personal information from children under 13, nor do we store any such data since all processing is restricted to your local device.

---

## 7. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. We will notify you of any changes by updating the "Effective Date" at the top of this document. We encourage you to review this Privacy Policy periodically for any changes.
