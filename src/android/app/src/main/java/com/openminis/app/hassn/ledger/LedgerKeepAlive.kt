package com.openminis.app.hassn.ledger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.openminis.app.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * [Hassn-P1 临时邮箱台账] 自动延时 / 探针保活 timer. Mines used WorkManager; Hassn has no WorkManager
 * dependency, so this uses the same AlarmManager + manifest receiver pattern as the app's scheduled
 * tasks: an inexact repeating alarm (battery friendly, Doze may delay it), armed only while some box
 * has a switch on ([LedgerCore.alarmIntervalMs]), cancelled otherwise.
 */
object LedgerKeepAlive {
    const val ACTION = "com.openminis.app.hassn.LEDGER_KEEPALIVE"
    private const val REQ = 7312

    private fun pending(c: Context): PendingIntent {
        val i = Intent(c.applicationContext, LedgerKeepAliveReceiver::class.java).setAction(ACTION)
        return PendingIntent.getBroadcast(c.applicationContext, REQ, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(c: Context) {
        val am = c.applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val every = LedgerCore.alarmIntervalMs(LedgerStore.state.value)
        val pi = pending(c)
        if (every == null) {
            am.cancel(pi)
            return
        }
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + every, every, pi)
    }
}

class LedgerKeepAliveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != LedgerKeepAlive.ACTION) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // a few HTTP calls at most; stay well inside the broadcast budget
                withTimeoutOrNull(25_000L) {
                    LedgerStore.init(app)
                    LedgerStore.runDue(app)
                }
                LedgerKeepAlive.schedule(app)
            } catch (t: Throwable) {
                AppLogger.warning("HassnLedger", "keep-alive run failed: ${t.javaClass.simpleName}")
            } finally {
                pending.finish()
            }
        }
    }
}
