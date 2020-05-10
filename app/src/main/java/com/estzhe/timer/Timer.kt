package com.estzhe.timer

import java.time.Duration
import java.time.Instant

class Timer {
    val targetTime: Instant
        get() {
            check(isRunning) { "This property can only be accessed if timer is running." }
            return runningTargetTime!!
        }

    val timeLeft: Duration
        get() = if (isRunning) {
                    Duration.between(Instant.now(), targetTime)
                }
                else {
                    pausedTimeLeft!!
                }

    val isRunning: Boolean
    val isPaused: Boolean get() = !isRunning

    private val runningTargetTime: Instant?
    private val pausedTimeLeft: Duration?

    private constructor(targetTime: Instant) {
        this.isRunning = true
        this.runningTargetTime = targetTime
        this.pausedTimeLeft = null
    }

    private constructor(timeLeft: Duration) {
        check(timeLeft > Duration.ZERO) { "Time left must be greater than zero." }

        this.isRunning = false
        this.runningTargetTime = null
        this.pausedTimeLeft = timeLeft
    }

    companion object {
        fun running(targetTime: Instant) = Timer(targetTime)
        fun paused(timeLeft: Duration) = Timer(timeLeft)
    }

    fun toPaused(): Timer {
        return if (isPaused) this else paused(timeLeft)
    }

    fun toRunning(): Timer {
        return if (isRunning) this else running(Instant.now().plus(timeLeft))
    }
}