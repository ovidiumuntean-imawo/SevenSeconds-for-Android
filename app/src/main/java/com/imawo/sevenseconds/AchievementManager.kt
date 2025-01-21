package com.imawo.sevenseconds

/**
 * Analog to iOS's AchievementManager, returning a textual message for a given score
 * and reporting achievements to GooglePlayGamesManager.
 */
object AchievementManager {

    // Replace these IDs with your Google Play Games Achievements IDs
    private const val DEDICATED_PLAYER_ID = "Cgk..._dedicated_player"
    private const val SUPER_PLAYER_ID = "Cgk..._super_player"
    private const val MASTER_ID       = "Cgk..._master"
    private const val SUPER_HERO_ID   = "Cgk..._super_hero"
    private const val GOD_ID          = "Cgk..._god"
    private const val CHEATER_ID      = "Cgk..._cheater"

    fun handleAchievements(score: Int): Pair<String?, String?> {
        var achievementMessage: String? = null
        var achievementToUnlock: String? = null

        when (score) {
            in 0 until 25 -> {
                achievementMessage =
                    "Every pro starts somewhere. " +
                            "\nYour journey just began! 🌟" +
                            "\n\nDon’t stop now – the button’s getting nervous!"
            }
            in 25 until 35 -> {
                achievementMessage =
                    "You’re warming up. " +
                            "\nGreatness is just a few clicks away! ⚡" +
                            "\n\nKeep at it – the leaderboard is calling your name!"
            }
            in 35 until 50 -> {
                achievementToUnlock = DEDICATED_PLAYER_ID
                achievementMessage =
                    "Dedicated player alert. " +
                            "\nYou’re officially in the game now! 🎯" +
                            "\n\nNow go smash that button like it owes you money!"
            }
            in 50 until 70 -> {
                achievementToUnlock = SUPER_PLAYER_ID
                achievementMessage =
                    "Super player unlocked. " +
                            "\nYou’re crushing it – what’s next? 💥" +
                            "\n\nKeep going – the button might file a restraining order!"
            }
            in 70 until 90 -> {
                achievementToUnlock = MASTER_ID
                achievementMessage =
                    "Master status achieved. " +
                            "\nThat’s some serious button-smashing talent! 👑" +
                            "\n\nPush harder – you’re one click away from greatness!"
            }
            in 90 until 110 -> {
                achievementToUnlock = SUPER_HERO_ID
                achievementMessage =
                    "Legends play like this. " +
                            "\nLeaderboard domination in progress! 🏆" +
                            "\n\nKeep smashing – you’re the hero this button deserves!"
            }
            in 110 until 125 -> {
                achievementToUnlock = GOD_ID
                achievementMessage =
                    "Did you just bend time? " +
                            "\nThe button fears you now. Respect!" +
                            "\n\nThe gods are watching – don’t let them down!"
            }
            in 125..1000 -> {
                achievementToUnlock = CHEATER_ID
                achievementMessage =
                    "Are your fingers okay? " +
                            "\nIf this is cheating, you’re the Picasso of it. 🎨" +
                            "\n\nJust make sure your button doesn’t explode! 😅"
            }
        }

        return achievementMessage to achievementToUnlock
    }

    /**
     * Helper to return a Pair<String?, String?> so we can also deliver the ID to unlock.
     */
    fun handleAchievementsPair(score: Int): Pair<String?, String?> {
        var message: String? = null
        var unlockId: String? = null

        when (score) {
            in 0 until 25 -> {
                message =
                    "Every pro starts somewhere. \nYour journey just began! 🌟" +
                            "\n\nDon’t stop now – the button’s getting nervous!"
            }
            in 25 until 35 -> {
                message =
                    "You’re warming up. " +
                            "\nGreatness is just a few clicks away! ⚡" +
                            "\n\nKeep at it – the leaderboard is calling your name!"
            }
            in 35 until 50 -> {
                unlockId = DEDICATED_PLAYER_ID
                message =
                    "Dedicated player alert. \nYou’re officially in the game now! 🎯" +
                            "\n\nNow go smash that button like it owes you money!"
            }
            in 50 until 70 -> {
                unlockId = SUPER_PLAYER_ID
                message =
                    "Super player unlocked. \nYou’re crushing it – what’s next? 💥" +
                            "\n\nKeep going – the button might file a restraining order!"
            }
            in 70 until 90 -> {
                unlockId = MASTER_ID
                message =
                    "Master status achieved. " +
                            "\nThat’s some serious button-smashing talent! 👑" +
                            "\n\nPush harder – you’re one click away from greatness!"
            }
            in 90 until 110 -> {
                unlockId = SUPER_HERO_ID
                message =
                    "Legends play like this. " +
                            "\nLeaderboard domination in progress! 🏆" +
                            "\n\nKeep smashing – you’re the hero this button deserves!"
            }
            in 110 until 125 -> {
                unlockId = GOD_ID
                message =
                    "Did you just bend time? \nThe button fears you now. Respect!" +
                            "\n\nThe gods are watching – don’t let them down!"
            }
            in 125..1000 -> {
                unlockId = CHEATER_ID
                message =
                    "Are your fingers okay? " +
                            "\nIf this is cheating, you’re the Picasso of it. 🎨" +
                            "\n\nJust make sure your button doesn’t explode! 😅"
            }
        }

        return Pair(message, unlockId)
    }
}