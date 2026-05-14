package com.michaelnketia.copyshare.code.phase3

data class FileNode(val name:String,val isFolder:Boolean,val children:MutableList<FileNode> = mutableListOf())