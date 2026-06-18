package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.Random
import kotlin.concurrent.thread

class AmbientSoundGenerator {
    private var audioTrack: AudioTrack? = null
    @Volatile private var isRunning = false
    private val random = Random()

    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { }

    private fun requestAudioFocus(context: Context): Boolean {
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val am = audioManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            focusRequest = request
            am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(focusChangeListener)
        }
    }

    fun startSound(context: Context, type: String) {
        stopSound()
        if (type == "Silence") return

        // Request audio focus to appease AppOps OP_CONTROL_AUDIO and OP_CONTROL_AUDIO_PARTIAL
        requestAudioFocus(context)

        isRunning = true
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
            abandonAudioFocus()
            return
        }

        thread(start = true, isDaemon = true, name = "AmbientSoundThread") {
            val buffer = ShortArray(bufferSize / 2)
            var lastValue = 0f 
            var cafeTimer = 0

            while (isRunning) {
                var index = 0
                while (index < buffer.size && isRunning) {
                    when (type) {
                        "White", "Blanc" -> {
                            val noise = (random.nextFloat() * 2f - 1f) * 0.12f
                            buffer[index] = (noise * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                        "Pluie", "Rain" -> {
                            val white = random.nextFloat() * 2f - 1f
                            lastValue = (0.94f * lastValue + 0.06f * white)
                            val droplet = if (random.nextFloat() > 0.994f) (random.nextFloat() * 2f - 1f) * 0.12f else 0f
                            val mix = lastValue * 0.22f + droplet * 0.05f
                            buffer[index] = (mix * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                        "Café", "Cafe" -> {
                            val white = random.nextFloat() * 2f - 1f
                            lastValue = (0.97f * lastValue + 0.03f * white)
                            var clink = 0f
                            if (cafeTimer <= 0) {
                                if (random.nextFloat() > 0.999f) {
                                    cafeTimer = (random.nextFloat() * 400 + 80).toInt()
                                }
                            } else {
                                cafeTimer--
                                val frequency = 700f + random.nextInt(250)
                                val t = cafeTimer / 22050f
                                clink = Math.sin(2.0 * Math.PI * frequency * t).toFloat() * 0.025f * (cafeTimer / 480f)
                            }
                            val mix = lastValue * 0.15f + clink
                            buffer[index] = (mix * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                        else -> {
                            buffer[index] = 0
                        }
                    }
                    index++
                }
                if (isRunning) {
                    try {
                        audioTrack?.write(buffer, 0, index)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun stopSound() {
        isRunning = false
        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    stop()
                    release()
                }
            }
        } catch (_: Exception) {}
        audioTrack = null
        abandonAudioFocus()
    }
}
