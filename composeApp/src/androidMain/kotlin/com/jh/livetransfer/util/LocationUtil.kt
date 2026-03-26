package com.jh.livetransfer.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices

/**
 * FusedLocationProviderClient를 사용해 마지막으로 캐시된 위치를 가져온다.
 *
 * - lastLocation: GPS를 새로 측위하지 않고 시스템 캐시를 조회하므로 빠르지만 null일 수 있음
 *   (기기를 껐다 켰거나 처음 위치 요청 시 null 가능 → onLocation 미호출)
 * - 권한 체크는 caller(WeatherMainScreen)에서 수행한다.
 * - @SuppressLint: 권한 확인이 caller에서 완료된 것을 전제로 억제.
 */
@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            location?.let {
                L.d("location : ${it.latitude} : ${it.longitude}")
                onLocation(it.latitude, it.longitude)
            }
        }
}