package com.example.installedapps.ui.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val icon: Drawable, val name: String, val version: String,
    val packageName: String, val checkSum: String
)