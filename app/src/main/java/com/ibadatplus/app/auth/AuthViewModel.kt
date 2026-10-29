package com.ibadatplus.app.auth

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AuthUiState(
    val authMethod: AuthMethod = AuthMethod.PHONE,
    val countryCode: String = "+91",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val otpCode: String = "",
    val verificationId: String = "",
    val resendTimerSeconds: Int = 30,
    val canResend: Boolean = false,
    val authState: AuthState = AuthState.Idle,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onAuthMethodChanged(method: AuthMethod) {
        _uiState.update { it.copy(authMethod = method, errorMessage = null) }
    }

    fun onPhoneNumberChanged(number: String) {
        // Filter out non-digits, max 10 digits for local mobile
        val clean = number.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phoneNumber = clean, errorMessage = null) }
    }

    fun onCountryCodeChanged(code: String) {
        _uiState.update { it.copy(countryCode = code, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onOtpCodeChanged(code: String) {
        // Limit to 6 digits
        val clean = code.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otpCode = clean, errorMessage = null) }
    }

    /**
     * Dispatches real SMS OTP via Firebase to user's phone number
     */
    fun sendPhoneOtp(activity: Activity) {
        val phone = _uiState.value.phoneNumber.trim()
        val code = _uiState.value.countryCode.trim()

        if (phone.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit mobile number.") }
            return
        }

        val fullPhoneNumber = if (phone.startsWith("+")) phone else "$code$phone"

        _uiState.update {
            it.copy(
                authState = AuthState.SendingOtp,
                errorMessage = null,
                otpCode = ""
            )
        }

        AuthManager.sendPhoneOtp(
            activity = activity,
            fullPhoneNumber = fullPhoneNumber,
            onCodeSent = { vId ->
                _uiState.update {
                    it.copy(
                        verificationId = vId,
                        authState = AuthState.OtpSent(
                            destination = fullPhoneNumber,
                            verificationId = vId,
                            resendTimeoutSeconds = 30
                        ),
                        resendTimerSeconds = 30,
                        canResend = false,
                        errorMessage = null
                    )
                }
                startResendCountdown()
            },
            onAutoVerified = { user ->
                _uiState.update {
                    it.copy(
                        authState = AuthState.Success(user),
                        errorMessage = null
                    )
                }
            },
            onError = { err ->
                _uiState.update {
                    it.copy(
                        authState = AuthState.Error(err),
                        errorMessage = err
                    )
                }
            }
        )
    }

    /**
     * Resends SMS OTP after 30-second cooldown expires
     */
    fun resendPhoneOtp(activity: Activity) {
        if (!_uiState.value.canResend) return
        sendPhoneOtp(activity)
    }

    /**
     * Verifies the 6-digit OTP code entered by user
     */
    fun verifyOtp() {
        val otp = _uiState.value.otpCode.trim()
        val vId = _uiState.value.verificationId

        if (otp.length != 6) {
            _uiState.update { it.copy(errorMessage = "Please enter the complete 6-digit OTP received via SMS.") }
            return
        }

        _uiState.update { it.copy(authState = AuthState.Verifying, errorMessage = null) }

        AuthManager.verifyPhoneOtp(
            verificationId = vId,
            otpCode = otp,
            onSuccess = { user ->
                countdownJob?.cancel()
                _uiState.update {
                    it.copy(
                        authState = AuthState.Success(user),
                        errorMessage = null
                    )
                }
            },
            onError = { err ->
                _uiState.update {
                    it.copy(
                        authState = AuthState.Error(err),
                        errorMessage = err
                    )
                }
            }
        )
    }

    /**
     * Alternative Email Sign in / Register
     */
    fun signInWithEmail() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (!email.contains("@") || email.length < 5) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(authState = AuthState.Verifying, errorMessage = null) }

        AuthManager.signInWithEmail(
            email = email,
            password = password,
            onSuccess = { user ->
                _uiState.update { it.copy(authState = AuthState.Success(user), errorMessage = null) }
            },
            onError = { err ->
                _uiState.update { it.copy(authState = AuthState.Error(err), errorMessage = err) }
            }
        )
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = 30
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update {
                    it.copy(
                        resendTimerSeconds = remaining,
                        canResend = remaining == 0
                    )
                }
            }
        }
    }

    fun changeNumber() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                authState = AuthState.Idle,
                otpCode = "",
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
