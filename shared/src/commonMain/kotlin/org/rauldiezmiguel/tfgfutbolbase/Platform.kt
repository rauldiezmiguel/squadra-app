package org.rauldiezmiguel.tfgfutbolbase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform