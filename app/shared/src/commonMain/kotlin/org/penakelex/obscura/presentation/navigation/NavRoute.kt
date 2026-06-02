package org.penakelex.obscura.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {
    @Serializable
    data object Splash : NavRoute

    sealed interface Auth : NavRoute {
        @Serializable
        data object Login : Auth

        @Serializable
        data object Register : Auth
    }

    sealed interface Main : NavRoute {
        @Serializable
        data object NotesList : Main

        @Serializable
        data class NoteEditor(
            val noteId: String? = null
        ) : Main

        @Serializable
        data object Settings : Main

        @Serializable
        data object Account : Main

        @Serializable
        data object Sessions : Main
    }
}