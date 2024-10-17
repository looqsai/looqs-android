package com.sean8.jeminai.core.util

import java.io.File

object MimeTypeUtil {
    fun getMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> null
        }
    }
}
