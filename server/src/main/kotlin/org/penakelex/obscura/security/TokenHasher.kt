package org.penakelex.obscura.security

import org.penakelex.obscura.config.ServerConfig
import java.security.MessageDigest

fun hashSessionToken(token: String): String = MessageDigest
    .getInstance(ServerConfig.security.session.hashAlgorithm)
    .digest(token.toByteArray())
    .joinToString("") { "%02x".format(it) }