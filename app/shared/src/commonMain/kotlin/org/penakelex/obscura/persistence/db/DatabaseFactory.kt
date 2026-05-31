package org.penakelex.obscura.persistence.db

expect class DatabaseFactory {
    fun create(): ObscuraDatabase
}