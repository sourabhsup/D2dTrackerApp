package com.pantrypal.app.data.model

import com.google.firebase.firestore.Exclude

data class InventoryItem(
    @get:Exclude val id: String = "",
    val name: String = "",
    val category: String = "Other",
    val quantity: Double = 0.0,
    val unit: String = "pcs",
    val lowStockThreshold: Double = 0.0,
    val expiryDateMillis: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    @get:Exclude
    val isLowStock: Boolean
        get() = quantity <= lowStockThreshold

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Produce", "Dairy", "Meat & Seafood", "Bakery", "Pantry",
            "Frozen", "Beverages", "Snacks", "Household", "Other"
        )
        val DEFAULT_UNITS = listOf("pcs", "kg", "g", "L", "mL", "pack", "dozen")
    }
}
