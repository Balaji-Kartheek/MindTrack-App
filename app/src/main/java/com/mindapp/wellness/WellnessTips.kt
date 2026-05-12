package com.mindapp.wellness

import com.mindapp.usage.UsageStatsHelper
import kotlin.math.roundToInt

/**
 * Curated wellness copy (no network). Used by notifications and the Wellness tab.
 */
object WellnessTips {

    private val generalHeavy = listOf(
        "Take a 2-minute break: look out a window and unclench your jaw.",
        "Stand up, roll your shoulders back 5 times, then sit with both feet on the floor.",
        "Try 4-7-8 breathing: inhale 4s, hold 7s, exhale 8s — repeat 3 times.",
        "Put the phone face-down for five minutes; notice sounds around you.",
        "Drink a glass of water slowly before opening the next app."
    )

    private val socialHeavy = listOf(
        "Mute non-essential notifications for one hour — single-task on what matters.",
        "Set a 10-minute timer before opening feeds again; stick to it.",
        "Replace one scroll session with a short voice note to someone you care about.",
        "Notice if comparison thoughts appear; label them ‘story’ and return to the present."
    )

    private val exercisesShort = listOf(
        "Neck: gently tilt ear toward shoulder, hold 15s each side.",
        "Wrists: circle hands 10 times each direction.",
        "Hips: stand, march in place 30 seconds.",
        "Eyes: 20-20-20 — every 20 min, look 20 feet away for 20 seconds."
    )

    private val microMeditation = listOf(
        "Count 10 slow breaths; on each exhale, soften your shoulders.",
        "Body scan: notice feet, then legs, belly, chest, face — no fixing, just noticing.",
        "Rest attention on the cool air entering your nose for one minute."
    )

    fun pickTipForNotification(
        totalOtherMs: Long,
        socialMediaMs: Long,
        topAppName: String?
    ): String {
        val socialRatio = if (totalOtherMs > 0) socialMediaMs.toDouble() / totalOtherMs else 0.0
        val pool = if (socialRatio >= 0.35) socialHeavy else generalHeavy
        val base = pool[(totalOtherMs / 3_600_000L).toInt().coerceIn(0, pool.lastIndex)]
        return if (topAppName != null) {
            "$base (You’ve spent notable time in apps like $topAppName today.)"
        } else {
            base
        }
    }

    fun exerciseForDay(seed: Long): String =
        exercisesShort[(seed % exercisesShort.size).toInt().coerceIn(0, exercisesShort.lastIndex)]

    fun meditationForDay(seed: Long): String =
        microMeditation[(seed / 7 % microMeditation.size).toInt().coerceIn(0, microMeditation.lastIndex)]

    /**
     * Simple mental-load signal from today’s usage (not clinical; for self-awareness only).
     */
    data class MentalLoadSnapshot(
        val headline: String,
        val detail: String,
        val suggestion: String
    )

    fun mentalLoadFromUsage(totalOtherMs: Long, socialMediaMs: Long): MentalLoadSnapshot {
        val hours = totalOtherMs / 3_600_000.0
        val socialRatio = if (totalOtherMs > 0) socialMediaMs.toDouble() / totalOtherMs else 0.0
        val socialPct = (socialRatio * 100).roundToInt()

        return when {
            hours < 1.0 && socialRatio < 0.25 -> MentalLoadSnapshot(
                headline = "Digital balance: comfortable",
                detail = "Your other-app time today is moderate. Keep checking in with breaks.",
                suggestion = generalHeavy.random()
            )
            hours < 3.0 && socialRatio < 0.4 -> MentalLoadSnapshot(
                headline = "Digital balance: elevated",
                detail = "You’ve been on other apps about ${UsageStatsHelper.formatTime(totalOtherMs)} today. " +
                    "Short resets help attention and mood.",
                suggestion = generalHeavy.random()
            )
            socialRatio >= 0.4 && hours >= 1.0 -> MentalLoadSnapshot(
                headline = "Social & feeds: high share",
                detail = "Roughly $socialPct% of tracked time looks social/feed-heavy. That can increase mental fatigue.",
                suggestion = socialHeavy.random()
            )
            hours >= 5.0 -> MentalLoadSnapshot(
                headline = "Digital load: high",
                detail = "Long stretches on device can drain focus. Tiny movement and breath breaks still count.",
                suggestion = generalHeavy.random()
            )
            else -> MentalLoadSnapshot(
                headline = "Digital balance: worth a pause",
                detail = "About ${UsageStatsHelper.formatTime(totalOtherMs)} on other apps today.",
                suggestion = pickTipForNotification(totalOtherMs, socialMediaMs, null)
            )
        }
    }
}
