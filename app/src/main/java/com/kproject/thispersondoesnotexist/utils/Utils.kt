package com.kproject.thispersondoesnotexist.utils

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object Utils {

    fun isSdk29OrAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View,  message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }
}