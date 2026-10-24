# Privacy Policy for Area Code Blocker

**Effective Date:** June 20, 2026  
**Last Updated:** October 24, 2026

This Privacy Policy explains how the **Area Code Blocker** mobile application (the **App**) handles information. The App helps you screen calls and silence message notifications that match area codes or keywords you configure.

## 1. Local Processing

The App performs its core screening functions locally on your device. Depending on the permissions and system access you grant, it may process:

- Contacts, to avoid blocking or silencing communications from saved contacts.
- Your phone number, to suggest your own area code.
- Incoming caller numbers, to compare them with blocked area-code rules.
- Messaging-notification sender details and text, to compare them with blocked area-code and keyword rules.
- Rules, preferences, and records of matched calls or notifications.

The App does not request permission to receive or read SMS messages directly. When notification access is enabled, it examines notifications posted by messaging apps. If a notification matches a rule, the App may dismiss it and store relevant sender, message, match, and time details in the local blocked-events log.

## 2. Developer Collection and Transmission

The developer does not operate servers that receive your contacts, phone number, caller information, notification content, blocking rules, or blocked-event history. The App does not include advertising or analytics SDKs, and the developer does not sell this information or share it for advertising.

Network libraries included in the App do not by themselves transmit the locally processed information described above.

## 3. Permissions and System Access

### Contacts (`android.permission.READ_CONTACTS`)
The App checks whether a caller or notification sender is in your contacts so saved contacts can be allowed. Contact matching occurs locally.

### Phone State and Number (`android.permission.READ_PHONE_STATE`, `android.permission.READ_PHONE_NUMBERS`)
These optional permissions let the App detect your phone number and suggest its area code. The number is stored only in the App's private local preferences.

### Call Screening (`android.permission.BIND_SCREENING_SERVICE` / `ROLE_CALL_SCREENING`)
When you grant the system Call Screening role, the App compares incoming caller numbers with your local contacts and area-code rules. Matched events may be recorded in the local log.

### Notification Access (`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`)
When you grant notification access, the App examines messaging-notification sender information and text locally. A matching notification may be dismissed and recorded in the local log.

## 4. Local Storage and Protection

Blocking rules and blocked-event logs are stored in the App's private Room database. Preferences, such as theme and your optionally saved phone number, are stored in private app preferences. Android's application sandbox restricts other apps from directly accessing this storage under normal operation.

You can remove individual rules or log entries, clear logs within the App, clear the App's storage in Android settings, or uninstall the App.

## 5. Backup and Device Transfer

The App excludes its databases and shared preferences from Android cloud backup and device-to-device transfer. This is intended to prevent sensitive local rules, preferences, and blocked-event logs from being copied through those mechanisms. Clearing storage or uninstalling the App removes its local data, subject to the behavior of your device and operating system.

## 6. Independent Platform Processing

Your device’s operating system, app store, device manufacturer, mobile carrier, and related platform services—including Google services where applicable—may independently process installation, security, diagnostics, crash, or usage information under their own terms and privacy policies.

The developer does not control that independent processing. This statement does not mean that the App sends your contacts, caller details, message-notification content, rules, or blocked-event logs to those providers.

## 7. Data Sharing and Legal Requirements

The developer does not sell or rent your locally processed information. Because the developer does not receive that information on developer-operated servers, the developer ordinarily cannot disclose it. Information may still be processed or disclosed by your device or platform providers under their own terms, or accessed on your device as permitted by applicable law.

## 8. Children's Privacy

The App is not directed to children under the age of 13. The developer does not knowingly collect personal information from children through developer-operated servers.

## 9. Changes to This Policy

This Privacy Policy may be updated to reflect changes to the App, platform requirements, or applicable practices. The date at the top identifies the latest revision. Material changes may require renewed acknowledgment in the App.
