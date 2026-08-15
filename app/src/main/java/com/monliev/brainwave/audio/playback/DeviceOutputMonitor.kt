package com.monliev.brainwave.audio.playback

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

/**
 * DeviceOutputMonitor helps check if headphones (wired, Bluetooth, USB, etc.)
 * are connected to the Android device.
 */
object DeviceOutputMonitor {

    /**
     * Checks if headphones or equivalent devices are currently connected.
     */
    fun isHeadphoneConnected(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Scan active audio output devices (supported on API 23+)
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_HEARING_AID,
                AudioDeviceInfo.TYPE_LINE_ANALOG -> {
                    return true
                }
            }
        }
        return false
    }
}
