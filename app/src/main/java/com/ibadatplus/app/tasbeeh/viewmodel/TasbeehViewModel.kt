package com.ibadatplus.app.tasbeeh.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.SoundEffectConstants
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibadatplus.app.tasbeeh.model.DefaultDhikrs
import com.ibadatplus.app.tasbeeh.model.DhikrItem
import com.ibadatplus.app.tasbeeh.model.TasbeehState
import com.ibadatplus.app.tasbeeh.repository.TasbeehRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TasbeehViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TasbeehRepository(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _uiState = MutableStateFlow(TasbeehState())
    val uiState: StateFlow<TasbeehState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val customDhikrs = repository.getCustomDhikrs()
        val allDhikrs = DefaultDhikrs.PRESETS + customDhikrs
        val activeId = repository.getActiveDhikrId()
        val currentDhikr = allDhikrs.firstOrNull { it.id == activeId } ?: DefaultDhikrs.PRESETS.first()

        val savedCount = repository.getCurrentCount(currentDhikr.id)
        val savedTarget = repository.getTargetCount(currentDhikr.id, currentDhikr.defaultTarget)
        val lifetimeTotal = repository.getLifetimeTotalCount()
        val sound = repository.isSoundEnabled()
        val vibration = repository.isVibrationEnabled()

        _uiState.update {
            it.copy(
                currentDhikr = currentDhikr,
                currentCount = savedCount,
                targetCount = savedTarget,
                lifetimeTotalCount = lifetimeTotal,
                isSoundEnabled = sound,
                isVibrationEnabled = vibration,
                allDhikrs = allDhikrs
            )
        }
    }

    /**
     * Increment counter on tap, handle feedback, and detect target milestone
     */
    fun onTapCounter() {
        val currentState = _uiState.value
        val newCount = currentState.currentCount + 1
        val newLifetime = currentState.lifetimeTotalCount + 1

        val isTargetReachedNow = currentState.targetCount > 0 && newCount == currentState.targetCount

        // 1. Persistent storage
        repository.saveCurrentCount(currentState.currentDhikr.id, newCount)
        repository.incrementLifetimeCount()

        // 2. Audio & Haptic Feedback
        if (isTargetReachedNow) {
            triggerTargetReachedVibration()
            if (currentState.isSoundEnabled) {
                playClickSound()
            }
        } else {
            if (currentState.isVibrationEnabled) {
                triggerTapHaptic()
            }
            if (currentState.isSoundEnabled) {
                playClickSound()
            }
        }

        _uiState.update {
            it.copy(
                currentCount = newCount,
                lifetimeTotalCount = newLifetime,
                isTargetReached = isTargetReachedNow
            )
        }
    }

    /**
     * Select a different Dhikr preset
     */
    fun selectDhikr(dhikr: DhikrItem) {
        val count = repository.getCurrentCount(dhikr.id)
        val target = repository.getTargetCount(dhikr.id, dhikr.defaultTarget)
        repository.saveActiveDhikrId(dhikr.id)

        _uiState.update {
            it.copy(
                currentDhikr = dhikr,
                currentCount = count,
                targetCount = target,
                isTargetReached = false
            )
        }
    }

    /**
     * Set target count (e.g. 33, 99, 100, or 0 for Free mode)
     */
    fun setTargetCount(target: Int) {
        val dhikrId = _uiState.value.currentDhikr.id
        repository.saveTargetCount(dhikrId, target)

        _uiState.update {
            it.copy(
                targetCount = target,
                isTargetReached = false,
                isSettingsDialogOpen = false
            )
        }
    }

    /**
     * Reset current count back to zero (Lifetime total is preserved)
     */
    fun confirmReset() {
        val dhikrId = _uiState.value.currentDhikr.id
        repository.saveCurrentCount(dhikrId, 0)

        if (_uiState.value.isVibrationEnabled) {
            triggerTapHaptic()
        }

        _uiState.update {
            it.copy(
                currentCount = 0,
                isTargetReached = false,
                isResetDialogOpen = false
            )
        }
    }

    /**
     * Create a new Custom Dhikr
     */
    fun addCustomDhikr(nameEnglish: String, nameArabic: String, target: Int) {
        val customDhikr = DhikrItem(
            id = "custom_" + UUID.randomUUID().toString().substring(0, 8),
            nameEnglish = nameEnglish.ifBlank { "Custom Dhikr" },
            nameArabic = nameArabic.ifBlank { "ذِكْرٌ" },
            defaultTarget = if (target < 0) 33 else target,
            meaning = "Custom User Dhikr",
            isCustom = true
        )

        repository.saveCustomDhikr(customDhikr)
        val allDhikrs = DefaultDhikrs.PRESETS + repository.getCustomDhikrs()

        _uiState.update {
            it.copy(
                allDhikrs = allDhikrs,
                isCustomDhikrDialogOpen = false
            )
        }
        selectDhikr(customDhikr)
    }

    fun toggleSound() {
        val newValue = !_uiState.value.isSoundEnabled
        repository.setSoundEnabled(newValue)
        _uiState.update { it.copy(isSoundEnabled = newValue) }
    }

    fun toggleVibration() {
        val newValue = !_uiState.value.isVibrationEnabled
        repository.setVibrationEnabled(newValue)
        _uiState.update { it.copy(isVibrationEnabled = newValue) }
    }

    fun showResetDialog(show: Boolean) {
        _uiState.update { it.copy(isResetDialogOpen = show) }
    }

    fun showCustomDhikrDialog(show: Boolean) {
        _uiState.update { it.copy(isCustomDhikrDialogOpen = show) }
    }

    fun showSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(isSettingsDialogOpen = show) }
    }

    fun dismissTargetReached() {
        _uiState.update { it.copy(isTargetReached = false) }
    }

    private fun playClickSound() {
        try {
            audioManager?.playSoundEffect(SoundEffectConstants.CLICK, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerTapHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(28)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerTargetReachedVibration() {
        try {
            val context = getApplication<Application>()
            val pattern = longArrayOf(0, 120, 80, 200, 80, 320)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
