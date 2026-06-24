package com.bazzi.app.model

data class FunctionItem(
    val name: String,
    val description: String,
    val targetActivity: Class<*>
)