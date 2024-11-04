package com.yandex.demeter.internal

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.yandex.demeter.annotations.InternalDemeterApi
import java.io.File

@InternalDemeterApi
class DemeterFileProvider : FileProvider()

internal fun Context.getUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(this, "com.yandex.demeter.fileprovider.${packageName}", file)
}
