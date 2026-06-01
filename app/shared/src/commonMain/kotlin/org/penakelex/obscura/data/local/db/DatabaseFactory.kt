package org.penakelex.obscura.data.local.db

expect class DatabaseFactory {
    fun create(): ObscuraDatabase
}