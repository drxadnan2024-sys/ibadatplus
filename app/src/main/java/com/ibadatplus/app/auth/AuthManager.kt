package com.ibadatplus.app.auth

import android.app.Activity
import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Manages Firebase Phone and Email Authentication with real SMS OTP delivery
 * and multi-user isolation session tracking.
 * Safe fallback to guest state if Firebase is not yet initialized or unavailable.
 */
object AuthManager {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            null
        }

    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Returns current user UID for multi-user database & SharedPreferences isolation
     */
    fun getCurrentUserId(): String {
        return try {
            auth?.currentUser?.uid ?: "guest"
        } catch (e: Throwable) {
            "guest"
        }
    }

    /**
     * Returns current user profile details
     */
    fun getCurrentUser(): AuthUser? {
        return try {
            auth?.currentUser?.toAuthUser()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Checks if a genuine user is currently authenticated
     */
    fun isUserLoggedIn(): Boolean {
        return try {
            val user = auth?.currentUser
            user != null && !user.isAnonymous
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Real SMS OTP delivery via Firebase Phone Authentication to user's actual mobile number.
     * STRICT: No mock or dummy OTP is ever displayed or generated locally.
     */
    fun sendPhoneOtp(
        activity: Activity,
        fullPhoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized. Please verify network or config.")
            return
        }

        try {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval / Instant verification by Google Play Services
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result.user?.toAuthUser()
                                if (user != null) {
                                    onAutoVerified(user)
                                } else {
                                    onError("Authentication failed. Please retry.")
                                }
                            } else {
                                onError(task.exception?.localizedMessage ?: "Verification failed.")
                            }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.localizedMessage ?: "Failed to dispatch SMS OTP. Verify phone number with country code.")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    forceResendingToken = token
                    onCodeSent(verificationId)
                }
            }

            val builder = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)

            if (forceResendingToken != null) {
                builder.setForceResendingToken(forceResendingToken!!)
            }

            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (t: Throwable) {
            onError(t.localizedMessage ?: "Failed to initialize phone verification.")
        }
    }

    /**
     * Verifies the 6-digit OTP code received by user via SMS
     */
    fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String,
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized.")
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user?.toAuthUser()
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onError("Login failed. Please retry.")
                        }
                    } else {
                        onError(task.exception?.localizedMessage ?: "Invalid OTP code. Please check your SMS.")
                    }
                }
        } catch (t: Throwable) {
            onError(t.localizedMessage ?: "Error during OTP verification.")
        }
    }

    /**
     * Email / Password alternative authentication
     */
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not initialized.")
            return
        }

        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user?.toAuthUser()
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onError("Email login failed.")
                        }
                    } else {
                        // Try seamless sign up if account doesn't exist
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { createContentTask ->
                                if (createContentTask.isSuccessful) {
                                    val user = createContentTask.result.user?.toAuthUser()
                                    if (user != null) {
                                        onSuccess(user)
                                    } else {
                                        onError("Failed to create account.")
                                    }
                                } else {
                                    onError(createContentTask.exception?.localizedMessage ?: "Authentication failed.")
                                }
                            }
                    }
                }
        } catch (t: Throwable) {
            onError(t.localizedMessage ?: "Authentication error.")
        }
    }

    /**
     * Logs out the user and clears authentication session
     */
    fun signOut(context: Context) {
        try {
            auth?.signOut()
            forceResendingToken = null
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            phoneNumber = phoneNumber,
            email = email,
            displayName = displayName,
            isAnonymous = isAnonymous
        )
    }
}
