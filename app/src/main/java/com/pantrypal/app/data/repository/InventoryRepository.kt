package com.pantrypal.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class InventoryRepository(private val firestore: FirebaseFirestore) {

    private fun collection(householdId: String) =
        firestore.collection("households").document(householdId).collection("inventory")

    fun observeItems(householdId: String): Flow<List<InventoryItem>> =
        collection(householdId).observeList { doc ->
            doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
        }

    suspend fun addItem(householdId: String, item: InventoryItem) {
        collection(householdId).add(item.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun updateItem(householdId: String, item: InventoryItem) {
        collection(householdId).document(item.id)
            .set(item.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }

    suspend fun adjustQuantity(householdId: String, item: InventoryItem, delta: Double) {
        val newQuantity = (item.quantity + delta).coerceAtLeast(0.0)
        updateItem(householdId, item.copy(quantity = newQuantity))
    }

    suspend fun deleteItem(householdId: String, itemId: String) {
        collection(householdId).document(itemId).delete().await()
    }
}
