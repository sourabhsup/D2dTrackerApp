package com.pantrypal.app.data.model

import com.google.firebase.firestore.Exclude

enum class ShoppingListSource { MANUAL, LOW_STOCK, MEAL_PLAN }

data class ShoppingListItem(
    @get:Exclude val id: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val checked: Boolean = false,
    val source: String = ShoppingListSource.MANUAL.name,
    val addedAt: Long = System.currentTimeMillis()
)
