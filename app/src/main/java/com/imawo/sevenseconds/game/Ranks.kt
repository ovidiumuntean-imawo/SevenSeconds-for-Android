package com.imawo.sevenseconds.game

import androidx.annotation.StringRes
import com.imawo.sevenseconds.R

data class Rank(
    val min: Int,
    @StringRes val title: Int,
    @StringRes val message: Int,
    @StringRes val achievement: Int? = null,
    /** Afișează banner + efecte când scorul atinge exact `min` în timpul rundei. */
    val milestone: Boolean = true,
)

/** Pragurile, mesajele și achievement-urile sunt cele din AchievementManager.swift (iOS). */
object Ranks {
    val all: List<Rank> = listOf(
        Rank(0, R.string.rank_rookie, R.string.msg_rookie, milestone = false),
        Rank(25, R.string.rank_warming_up, R.string.msg_warming_up),
        Rank(35, R.string.rank_dedicated, R.string.msg_dedicated, R.string.achievement_dedicated_player),
        Rank(50, R.string.rank_super, R.string.msg_super, R.string.achievement_super_player),
        Rank(70, R.string.rank_master, R.string.msg_master, R.string.achievement_master),
        Rank(90, R.string.rank_hero, R.string.msg_hero, R.string.achievement_super_hero),
        Rank(110, R.string.rank_inhuman, R.string.msg_inhuman, R.string.achievement_god),
        Rank(145, R.string.rank_god, R.string.msg_god, R.string.achievement_cheater),
    )

    fun forScore(score: Int): Rank = all.last { score >= it.min }

    fun milestoneAt(score: Int): Rank? = all.firstOrNull { it.milestone && it.min == score }
}
