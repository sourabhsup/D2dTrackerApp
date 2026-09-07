package com.pantrypal.app.data.model

import com.google.firebase.firestore.Exclude

data class RecipeIngredient(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)

data class Recipe(
    @get:Exclude val id: String = "",
    val name: String = "",
    val servings: Int = 2,
    val instructions: String = "",
    val ingredients: List<RecipeIngredient> = emptyList()
)
