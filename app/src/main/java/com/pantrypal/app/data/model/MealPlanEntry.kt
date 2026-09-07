package com.pantrypal.app.data.model

import com.google.firebase.firestore.Exclude

enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner")
}

data class MealPlanEntry(
    @get:Exclude val id: String = "",
    val dateMillis: Long = 0L,
    val mealType: String = MealType.DINNER.name,
    val recipeId: String = "",
    val recipeName: String = ""
) {
    @get:Exclude
    val mealTypeEnum: MealType
        get() = runCatching { MealType.valueOf(mealType) }.getOrDefault(MealType.DINNER)
}
