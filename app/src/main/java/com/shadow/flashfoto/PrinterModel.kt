package com.shadow.flashfoto

data class PrinterModel(
    val name: String,
    val ip: String,
    var isActive: Boolean = false
)
