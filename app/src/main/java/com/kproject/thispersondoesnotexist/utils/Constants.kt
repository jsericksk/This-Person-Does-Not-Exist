package com.kproject.thispersondoesnotexist.utils

import android.os.Environment
import java.io.File
import java.util.*

object Constants {
    private const val IMAGE_URL = "https://thispersondoesnotexist.com/image"
    val APP_FOLDER: String = "${Environment.DIRECTORY_PICTURES}${File.separator}TPDNE Images"

    fun getRandomUrl(): String {
        return IMAGE_URL + "?${UUID.randomUUID()}"
    }
}