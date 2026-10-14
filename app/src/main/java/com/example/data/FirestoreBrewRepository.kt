package com.example.data

import android.content.Context
import com.example.R
import com.example.model.BrewRecord
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreBrewRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    /**
     * Real-time observation of brews scoped to current authenticated user
     */
    fun observeUserBrews(): Flow<List<BrewRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/brews"
        val query = db.collection("users")
            .document(uid)
            .collection("brews")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(BrewRecord::class.java)
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    /**
     * Saves a new coffee brew record to Firestore
     */
    suspend fun saveBrew(
        beanName: String,
        recipeName: String,
        doseGrams: Float,
        ratio: Float,
        totalWaterGrams: Int,
        durationSeconds: Int,
        rating: Int,
        tastingNotes: String
    ): String {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("brews")
        val docRef = collectionRef.document()

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "beanName" to beanName,
            "recipeName" to recipeName,
            "doseGrams" to doseGrams.toDouble(),
            "ratio" to ratio.toDouble(),
            "totalWaterGrams" to totalWaterGrams.toLong(),
            "durationSeconds" to durationSeconds.toLong(),
            "rating" to rating.toLong(),
            "tastingNotes" to tastingNotes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    /**
     * Deletes a brew record from Firestore
     */
    suspend fun deleteBrew(brewId: String) {
        val uid = requireUserId()
        require(brewId.isNotBlank()) { "Brew ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("brews").document(brewId)

        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
