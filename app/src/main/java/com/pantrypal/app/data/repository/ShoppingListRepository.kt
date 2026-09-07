package com.pantrypal.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ShoppingListRepository(private val firestore: FirebaseFirestore) {

    private fun collection(householdId: String) =
        firestore.collection("households").document(householdId).collection("shoppingList")

    fun observeItems(householdId: String): Flow<List<ShoppingListItem>> =
        collection(householdId).observeList { doc ->
            doc.toObject(ShoppingListItem::class.java)?.copy(id = doc.id)
        }

    suspend fun addItem(householdId: String, item: ShoppingListItem) {
        collection(householdId).add(item).await()
    }

    suspend fun addItems(householdId: String, items: List<ShoppingListItem>) {
        if (items.isEmpty()) return
        val batch = firestore.batch()
        val col = collection(householdId)
        items.forEach { item -> batch.set(col.document(), item) }
        batch.commit().await()
    }

    suspend fun setChecked(householdId: String, item: ShoppingListItem, checked: Boolean) {
        collection(householdId).document(item.id).update("checked", checked).await()
    }

    suspend fun deleteItem(householdId: String, itemId: String) {
        collection(householdId).document(itemId).delete().await()
    }

    suspend fun deleteItems(householdId: String, itemIds: List<String>) {
        if (itemIds.isEmpty()) return
        val batch = firestore.batch()
        val col = collection(householdId)
        itemIds.forEach { id -> batch.delete(col.document(id)) }
        batch.commit().await()
    }
}
