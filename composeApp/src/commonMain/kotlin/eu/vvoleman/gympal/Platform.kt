package eu.vvoleman.gympal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform