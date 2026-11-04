package com.imawo.sevenseconds.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.play.core.review.ReviewManagerFactory
import com.imawo.sevenseconds.R

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object Platform {

    /** „CHALLENGE FRIEND": același text și același link ca pe iOS. */
    fun shareChallenge(context: Context, score: Int) {
        val url = "${GameConfig.CHALLENGE_REDIRECT_URL}?score=$score"
        val text = context.getString(R.string.share_challenge_text, score, url)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, context.getString(R.string.share_chooser_title))
        if (context.findActivity() == null) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (ignored: Throwable) {
        }
    }

    fun openUrl(context: Context, url: String) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (context.findActivity() == null) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(i)
        } catch (ignored: Throwable) {
        }
    }

    /** Echivalentul lui requestReview() din SwiftUI — Play In-App Review. */
    fun requestReview(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) manager.launchReviewFlow(activity, task.result)
            }
        } catch (ignored: Throwable) {
        }
    }

    /** Cere cea mai mare rată de refresh la rezoluția curentă (90/120 Hz) — esențial pentru un joc de tapping. */
    fun requestHighRefreshRate(activity: Activity) {
        if (Build.VERSION.SDK_INT < 23) return
        try {
            @Suppress("DEPRECATION")
            val display = (if (Build.VERSION.SDK_INT >= 30) activity.display else activity.windowManager.defaultDisplay)
                ?: return
            val cur = display.mode
            val best = display.supportedModes
                .filter { it.physicalWidth == cur.physicalWidth && it.physicalHeight == cur.physicalHeight }
                .maxByOrNull { it.refreshRate } ?: return
            val lp = activity.window.attributes
            lp.preferredDisplayModeId = best.modeId
            activity.window.attributes = lp
        } catch (ignored: Throwable) {
        }
    }

    /** true dacă utilizatorul a dezactivat animațiile în setările de sistem. */
    fun systemAnimationsOff(context: Context): Boolean = try {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    } catch (ignored: Throwable) {
        false
    }
}
