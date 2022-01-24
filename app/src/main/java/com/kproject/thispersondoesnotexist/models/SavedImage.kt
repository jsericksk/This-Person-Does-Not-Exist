package com.kproject.thispersondoesnotexist.models

import android.net.Uri

data class SavedImage(
    val id: Long,
    val contentUri: Uri,
    val name: String
)