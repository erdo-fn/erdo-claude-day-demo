package com.kmpfoo.appdi

import android.app.Application
import okio.Path
import okio.Path.Companion.toOkioPath

actual fun filesPath(application: Any?): Path {
    return (application as Application).filesDir.toOkioPath()
}