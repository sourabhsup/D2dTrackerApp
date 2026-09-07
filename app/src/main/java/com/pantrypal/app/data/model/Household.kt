package com.pantrypal.app.data.model

import com.google.firebase.firestore.Exclude

data class Household(
    @get:Exclude val id: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
