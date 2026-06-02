package org.penakelex.obscura.presentation.util.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

class AndroidClipboard(context: Context) : Clipboard {
    private val clipboardManager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun setText(text: String) {
        val clip = ClipData.newPlainText(CLIP_LABEL, text)
        clipboardManager.setPrimaryClip(clip)
    }

    override fun getText(): String? {
        if (!clipboardManager.hasPrimaryClip()) return null
        val description = clipboardManager.primaryClipDescription ?: return null
        if (!description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) return null
        return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
    }

    private companion object {
        const val CLIP_LABEL = "Obscura"
    }
}