package com.michaelnketia.copyshare.code

object TabManager {

    val openTabs = mutableListOf<String>()

    var currentTab: String? = null

    fun open(file: String) {
        if (!openTabs.contains(file)) {
            openTabs.add(file)
        }
        currentTab = file
    }

    fun close(file: String) {
        openTabs.remove(file)

        if (currentTab == file) {
            currentTab = openTabs.lastOrNull()
        }
    }
}