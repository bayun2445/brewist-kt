package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object GoogleAuthHelper {

    private const val TAG = "GoogleAuthHelper"

    fun onGoogleSignInClicked(
        context: Context,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Konfigurasi Google Sign-In tidak ditemukan (default_web_client_id)")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context as? Activity
                    ?: throw IllegalStateException("Context must be an Activity for Credential Manager")
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onAuthError("Tipe kredensial Google tidak terduga.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In dibatalkan pengguna: ${e.message}")
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In gagal", e)
                onAuthError(e.localizedMessage ?: "Gagal masuk dengan akun Google")
            }
        }
    }

    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        Firebase.auth.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Gagal membersihkan status kredensial", e)
            } finally {
                onComplete()
            }
        }
    }
}
