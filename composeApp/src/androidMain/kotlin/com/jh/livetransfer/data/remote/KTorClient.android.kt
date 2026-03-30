package com.jh.livetransfer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun createHttpClient(): HttpClient = HttpClient(Android)