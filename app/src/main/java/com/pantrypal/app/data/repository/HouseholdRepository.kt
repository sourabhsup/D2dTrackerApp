package com.pantrypal.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pantrypal.app.data.model.Household
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * A household is just a Firestore document whose id doubles as the short
 * code people type into the "join household" screen on a second phone.
 */
class HouseholdRepository(private val firestore: FirebaseFirestore) {

    private fun households() = firestore.collection("households")

    suspend fun createHousehold(name: String): Household {
        var code: String
        do {
            code = generateCode()
        } while (households().document(code).get().await().exists())

        val household = Household(id = code, name = name)
        households().document(code).set(household).await()
        return household
    }

    suspend fun joinHousehold(code: String): Household? {
        val normalized = code.trim().uppercase()
        if (normalized.isEmpty()) return null
        val snapshot = households().document(normalized).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(Household::class.java)?.copy(id = snapshot.id)
    }

    suspend fun getHousehold(id: String): Household? {
        val snapshot = households().document(id).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(Household::class.java)?.copy(id = snapshot.id)
    }

    private fun generateCode(): String {
        // Excludes ambiguous characters (0/O, 1/I) so codes are easy to read aloud or type.
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
