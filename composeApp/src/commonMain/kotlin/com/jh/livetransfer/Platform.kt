package com.jh.livetransfer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform