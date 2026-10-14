package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class BrewNotifier(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (e: Exception) {
            Log.w("BrewNotifier", "ToneGenerator init failed", e)
        }
    }

    /**
     * Distinct double pulse vibration when a new stage begins
     */
    fun notifyStageTransition(vibrationEnabled: Boolean = true, soundEnabled: Boolean = true) {
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP)
        }
        if (vibrationEnabled && vibrator?.hasVibrator() == true) {
            val timings = longArrayOf(0, 160, 100, 220)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        }
    }

    /**
     * Short single pulse 3 seconds before stage transition to alert barista to prepare kettle
     */
    fun notifyStagePreWarning(vibrationEnabled: Boolean = true, soundEnabled: Boolean = false) {
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_PROMPT)
        }
        if (vibrationEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(90)
            }
        }
    }

    /**
     * Celebratory vibration pattern when brewing is completely finished
     */
    fun notifyBrewFinished(vibrationEnabled: Boolean = true, soundEnabled: Boolean = true) {
        if (soundEnabled) {
            playTone(ToneGenerator.TONE_PROP_ACK)
        }
        if (vibrationEnabled && vibrator?.hasVibrator() == true) {
            val timings = longArrayOf(0, 200, 100, 200, 100, 400)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        }
    }

    private fun playTone(toneType: Int) {
        try {
            toneGenerator?.startTone(toneType, 180)
        } catch (e: Exception) {
            Log.w("BrewNotifier", "Error playing tone", e)
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
