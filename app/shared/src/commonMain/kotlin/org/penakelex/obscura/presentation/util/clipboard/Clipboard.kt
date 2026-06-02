package org.penakelex.obscura.presentation.util.clipboard

interface Clipboard {
    fun setText(text: String)
    fun getText(): String?
}