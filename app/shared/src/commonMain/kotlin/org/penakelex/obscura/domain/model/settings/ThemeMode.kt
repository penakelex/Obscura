package org.penakelex.obscura.domain.model.settings

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        val DEFAULT: ThemeMode = SYSTEM

        fun fromId(id: Int): ThemeMode =
            entries.firstOrNull { it.ordinal == id } ?: DEFAULT
    }

    val id: Int get() = ordinal
}