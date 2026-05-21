package org.penakelex.obscura

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform