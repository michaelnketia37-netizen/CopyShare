package com.michaelnketia.copyshare.code

import android.content.Context

object SessionManager {

    private const val PREF = "code_session"

    fun saveCurrentTab(context: Context, file: String) {

        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString("last_tab", file)
            .apply()
    }

    fun loadCurrentTab(context: Context): String? {

        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString("last_tab", null)
    }
}