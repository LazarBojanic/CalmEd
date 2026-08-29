package com.calmed.calmedtics.video

enum class VideoResolution(
    val label: String,
    val maxResolution: String?
) {
    R1080("1080p", "1080p"),
    R720("720p", "720p"),
    R480("480p", "480p");

    companion object {
        fun fromName(name: String?): VideoResolution =
            entries.firstOrNull { it.name == name } ?: R720
    }
}

fun applyMaxResolution(url: String, resolution: VideoResolution): String {
    val maxResolution = resolution.maxResolution ?: return url
    if (url.isBlank()) return url

    val fragmentIndex = url.indexOf('#')
    val base = if (fragmentIndex >= 0) url.substring(0, fragmentIndex) else url
    val fragment = if (fragmentIndex >= 0) url.substring(fragmentIndex) else ""

    val queryIndex = base.indexOf('?')
    val path = if (queryIndex >= 0) base.substring(0, queryIndex) else base
    val rawQuery = if (queryIndex >= 0) base.substring(queryIndex + 1) else ""

    val params = rawQuery
        .split('&')
        .filter { it.isNotBlank() }
        .mapNotNull { pair ->
            val idx = pair.indexOf('=')
            if (idx < 0) null else pair.substring(0, idx) to pair.substring(idx + 1)
        }
        .toMutableList()

    val existingIndex = params.indexOfFirst { it.first == MAX_RESOLUTION_KEY }
    val entry = MAX_RESOLUTION_KEY to maxResolution
    if (existingIndex >= 0) {
        params[existingIndex] = entry
    } else {
        params.add(entry)
    }

    val query = params.joinToString("&") { (key, value) -> "$key=$value" }

    return buildString {
        append(path)
        if (query.isNotEmpty()) {
            append('?')
            append(query)
        }
        append(fragment)
    }
}

private const val MAX_RESOLUTION_KEY = "max_resolution"
