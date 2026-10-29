package com.ibadatplus.app.auth

/**
 * Clean data model representing authenticated user profile
 */
data class AuthUser(
    val uid: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val isAnonymous: Boolean = false
) {
    val displayIdentifier: String
        get() = when {
            !phoneNumber.isNullOrBlank() -> phoneNumber
            !email.isNullOrBlank() -> email
            !displayName.isNullOrBlank() -> displayName
            else -> "User ${uid.take(6)}"
        }
}

/**
 * UI State for Phone/Email OTP Authentication flow
 */
sealed interface AuthState {
    object Idle : AuthState
    object SendingOtp : AuthState
    data class OtpSent(
        val destination: String,
        val verificationId: String,
        val resendTimeoutSeconds: Int = 30
    ) : AuthState
    object Verifying : AuthState
    data class Success(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}

enum class AuthMethod {
    PHONE,
    EMAIL
}
