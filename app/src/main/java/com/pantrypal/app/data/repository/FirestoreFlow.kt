package com.pantrypal.app.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wraps a Firestore realtime listener as a cold [Flow]. This is what keeps
 * data in sync across both phones: every write from either device triggers
 * this listener on the other device automatically.
 */
fun <T> CollectionReference.observeList(map: (DocumentSnapshot) -> T?): Flow<List<T>> =
    callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull(map).orEmpty())
        }
        awaitClose { registration.remove() }
    }
