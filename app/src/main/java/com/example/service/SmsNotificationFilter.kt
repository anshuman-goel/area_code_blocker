package com.example.service

import com.example.util.PhoneUtils

/** Pure matching logic shared by the notification listener and unit tests. */
object SmsNotificationFilter {

    data class Match(
        val areaCode: String,
        val keyword: String?,
    )

    fun findMatch(
        senderIdentifiers: List<String>,
        messageText: String,
        blockedAreaCodes: Collection<String>,
        blockedKeywords: Collection<String>,
    ): Match? {
        val matchedKeyword = blockedKeywords.firstOrNull { keyword ->
            messageText.contains(keyword, ignoreCase = true) ||
                senderIdentifiers.any { it.contains(keyword, ignoreCase = true) }
        }

        val matchedAreaCode = senderIdentifiers
            .asSequence()
            .mapNotNull(PhoneUtils::extractNanpAreaCodeOrNull)
            .firstOrNull(blockedAreaCodes::contains)

        if (matchedKeyword == null && matchedAreaCode == null) return null

        return Match(
            areaCode = matchedAreaCode.orEmpty(),
            keyword = matchedKeyword,
        )
    }
}
