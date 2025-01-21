package com.imawo.sevenseconds

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.Games
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException

/**
 * Rough analog to iOS's GameCenterManager, but uses Google Play Games Services.
 *
 * Make sure to configure:
 *  1) The Play Games Services in the Google Play Console,
 *  2) The achievements + leaderboards IDs,
 *  3) Add the corresponding dependencies and sign in logic.
 */
object GooglePlayGamesManager {

    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null

    fun signIn(activity: Activity, onResult: (Boolean) -> Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        if (account == null) {
            // Initiate sign in flow
            // You could also use 'GoogleSignIn.getClient(...)' with the right options
            // For simplicity, we assume sign-in is triggered outside & we just check success here
            onResult(false)
        } else {
            // Already signed in
            initClients(activity)
            onResult(true)
        }
    }

    private fun initClients(context: Context) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            leaderboardsClient = Games.getLeaderboardsClient(context, account)
            achievementsClient = Games.getAchievementsClient(context, account)
        }
    }

    fun submitScore(context: Context, leaderboardId: String, score: Int) {
        val client = leaderboardsClient ?: run {
            initClients(context)
            leaderboardsClient
        } ?: return

        client.submitScore(leaderboardId, score.toLong())
    }

    fun showLeaderboard(activity: Activity, leaderboardId: String) {
        val client = leaderboardsClient ?: return
        client.getLeaderboardIntent(leaderboardId).addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, 9999)
        }.addOnFailureListener { e ->
            Log.e("GooglePlayGames", "Error showing leaderboard: ${e.localizedMessage}")
        }
    }

    fun unlockAchievement(context: Context, achievementId: String) {
        val client = achievementsClient ?: run {
            initClients(context)
            achievementsClient
        } ?: return

        client.unlock(achievementId)
    }

    fun showAchievements(activity: Activity) {
        val client = achievementsClient ?: return
        client.achievementsIntent.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, 8888)
        }.addOnFailureListener { e ->
            Log.e("GooglePlayGames", "Error showing achievements: ${e.localizedMessage}")
        }
    }
}