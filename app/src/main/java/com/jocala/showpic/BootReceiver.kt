package com.jocala.showpic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/**
 * Starts MainActivity after boot. The Echo Show brings up com.amazon.ambienthome
 * as HOME during boot, so the launch waits a few seconds to land after it.
 */
class BootReceiver : BroadcastReceiver() {

    private companion object {
        private const val LAUNCH_DELAY_MS = 8000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val launch = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pending = goAsync()
        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { context.startActivity(launch) }
            pending.finish()
        }, LAUNCH_DELAY_MS)
    }
}
