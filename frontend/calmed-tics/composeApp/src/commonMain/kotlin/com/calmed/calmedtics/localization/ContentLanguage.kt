package com.calmed.calmedtics.localization

/**
 * Resolves which localized strings the backend provides (e.g. [titleEs], Spanish video URLs).
 *
 * When the user picks a specific language in settings, that wins. When they use AUTO (null),
 * we follow the active UI locale tag (system/configuration), not English by default.
 */
fun resolveContentLanguage(
    appLanguageOverride: String?,
    uiLocaleTag: String
): String {
    val trimmed = appLanguageOverride?.trim()
    val tag = if (!trimmed.isNullOrEmpty()) trimmed else uiLocaleTag
    val primary = tag.substringBefore('-').lowercase()
    return if (primary == "es") "es" else "en"
}
