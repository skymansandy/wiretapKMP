package dev.skymansandy.kurlclient

actual fun getPlatform(): Platform {

    return object : Platform {
        override val name: String
            get() = "Android"
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
