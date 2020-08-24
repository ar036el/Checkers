package el.arn.checkers.managers

import android.app.Activity
import android.util.Log
import el.arn.checkers.helpers.listeners_engine.HoldsListeners
import el.arn.checkers.helpers.listeners_engine.ListenersManager
import java.util.*

interface Timer : HoldsListeners<Timer.Listener> {
    fun start()
    fun stop()
    fun reset()
    val state: States
    val timeInSeconds: Int
    var hostingActivityForUiThread: Activity?

    interface Listener {
        fun timeWasChanged(timer: Timer, timeInSeconds: Int) {}
        fun stateWasChanged(timer: Timer, state: States) {}
    }

    enum class States { NotStarted, Running, Stopped }
}

class TimerImpl(
    override var hostingActivityForUiThread: Activity?,
    val listenersMgr: ListenersManager<Timer.Listener> = ListenersManager()
) : Timer, HoldsListeners<Timer.Listener> by listenersMgr {

    private var timerObj = Timer()
    private var timeInSecondsFloat = 0f

    override var timeInSeconds = 0
    override var state = Timer.States.NotStarted

    override fun start() {
        if (state == Timer.States.Running) {
            return
        }
        timerObj.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                hostingActivityForUiThread?.runOnUiThread {
                    timeInSecondsFloat += 0.25f
                    if (timeInSecondsFloat.toInt() != timeInSeconds) {
                        timeInSeconds = timeInSecondsFloat.toInt()
                        listenersMgr.notifyAll { it.timeWasChanged(this@TimerImpl, timeInSeconds) }
                    }
                } ?: Log.e("Timer", "no activity is registered, can't notify listeners properly for 'timeWasChanged()'")
            }
        }, 250, 250)
        state = Timer.States.Running
        listenersMgr.notifyAll { it.timeWasChanged(this@TimerImpl, timeInSeconds) }
        listenersMgr.notifyAll { it.stateWasChanged(this@TimerImpl, state) }
    }

    override fun stop() {
        if (state == Timer.States.Stopped || state == Timer.States.NotStarted) {
            return
        }
        restartTimer()
        state = Timer.States.Stopped
        listenersMgr.notifyAll { it.stateWasChanged(this@TimerImpl, state) }
    }

    override fun reset() {
        if (state == Timer.States.NotStarted) {
            return
        }
        restartTimer()
        timeInSeconds = 0
        timeInSecondsFloat = 0f
        state = Timer.States.NotStarted
        listenersMgr.notifyAll { it.timeWasChanged(this@TimerImpl, timeInSeconds) }
        listenersMgr.notifyAll { it.stateWasChanged(this@TimerImpl, state) }
    }

    private fun restartTimer() {
        timerObj.cancel()
        timerObj = Timer()
    }

}