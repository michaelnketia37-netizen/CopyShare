package com.michaelnketia.copyshare.code

import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    fun toggle(isDark: Boolean) {

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}