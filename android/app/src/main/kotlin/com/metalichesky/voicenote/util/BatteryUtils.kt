package com.metalichesky.voicenote.util

import android.os.Build

import android.os.PowerManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.RequiresApi

object BatteryUtils {
    const val DEFAULT_BATTERY_MAX_LEVEL = 100

    fun getPowerManager(context: Context): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getBatteryManager(context: Context): BatteryManager {
        return context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    fun getBatteryLevel(context: Context): Double {
        val batteryLevel: Double
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getBatteryManager(context)
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            batteryLevel = level.toDouble() / DEFAULT_BATTERY_MAX_LEVEL
        } else {
            val intent = ContextWrapper(context).registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val batteryScale =
                intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    ?: DEFAULT_BATTERY_MAX_LEVEL
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            batteryLevel = level.toDouble() / batteryScale
        }
        return batteryLevel
    }

    @SuppressLint("ObsoleteSdkInt")
    fun isAppBlacklisted(context: Context): Boolean {
        val powerManager = getPowerManager(context)
        val name: String = context.getPackageName()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(name)
        } else {
            false
        }
    }
}