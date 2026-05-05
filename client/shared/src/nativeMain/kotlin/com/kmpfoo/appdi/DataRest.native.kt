package com.kmpfoo.appdi

import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun filesPath(application: Any?): okio.Path {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    )
    val documentDirectory = urls.first() as NSURL
    val pathString = documentDirectory.path ?: ""
    return pathString.toPath().normalized()
}
