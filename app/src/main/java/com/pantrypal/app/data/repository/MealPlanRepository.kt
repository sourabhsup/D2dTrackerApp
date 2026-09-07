package com.pantrypal.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.model.MealPlanEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MealPlanRepository(private val firestore: FirebaseFirestore) {

    private fun collection(householdId: String) =
        firestore.collection("households").document(householdId).collection("mealPlan")

    /** Observes every planned meal; the UI filters this down to the visible week. */
    fun observeEntries(householdId: String): Flow<List<MealPlanEntry>> =
        collection(householdId).observeList { doc ->
            doc.toObject(MealPlanEntry::class.java)?.copy(id = doc.id)
        }

    suspend fun setEntry(householdId: String, entry: MealPlanEntry) {
        if (entry.id.isBlank()) {
            collection(householdId).add(entry).await()
        } else {
            collection(householdId).document(entry.id).set(entry).await()
        }
    }

    suspend fun deleteEntry(householdId: String, entryId: String) {
        collection(householdId).document(entryId).delete().await()
    }
}
