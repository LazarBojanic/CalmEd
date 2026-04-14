package com.calmed.calmedtics.localization

/**
 * Resolves which localized strings the backend provides (e.g. [titleEs], Spanish video URLs).
 *
 * When the user picks a specific language in settings, that wins.
 */
fun resolveContentLanguage(
    appLanguageOverride: String?,
    uiLocaleTag: String
): String {
    val tag = if (!appLanguageOverride.isNullOrBlank()) {
        appLanguageOverride.trim()
    } else {
        uiLocaleTag
    }
    val primary = tag.substringBefore('-').lowercase()
    return if (primary == "es") "es" else "en"
}
