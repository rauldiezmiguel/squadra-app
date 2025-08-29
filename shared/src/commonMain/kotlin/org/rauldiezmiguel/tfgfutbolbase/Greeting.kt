package org.rauldiezmiguel.tfgfutbolbase

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}