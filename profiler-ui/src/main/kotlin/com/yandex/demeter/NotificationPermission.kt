package com.yandex.demeter

import android.os.Build

internal object NotificationPermission {
    fun canPost(sdkInt: Int, permissionGranted: Boolean): Boolean {
        return sdkInt < Build.VERSION_CODES.TIRAMISU || permissionGranted
    }
}
