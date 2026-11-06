package com.imawo.sevenseconds.core

/** Toate constantele de joc într-un singur loc (valorile sunt cele din versiunea iOS). */
object GameConfig {
    /** Durata unei runde. */
    const val DURATION_MS = 7_000L

    /** Scorurile >= acestei valori NU se trimit în clasament (anti-cheat, ca pe iOS: `score < 145`). */
    const val LEADERBOARD_MAX_SCORE = 145

    /** Deep link pentru provocări: sevenseconds://challenge?score=NN */
    const val CHALLENGE_SCHEME = "sevenseconds"
    const val CHALLENGE_HOST = "challenge"

    /** Aceeași pagină de redirect ca pe iOS — linkurile de provocare funcționează între platforme. */
    const val CHALLENGE_REDIRECT_URL =
        "https://ovidiumuntean-imawo.github.io/7seconds-challenge-redirect/redirect.html"

    const val COMMUNITY_URL = "https://facebook.com/groups/sevensecondschampions"

    /**
     * true  = fiecare deget nou care atinge butonul contează (mai mulți degete ⇒ scoruri mai mari decât pe iOS);
     * false = paritate cu iOS (un singur deget odată).
     */
    const val ALLOW_MULTITOUCH = false

    /** Pauza dintre explozia finală și ecranul Game Over (lasă efectul să se vadă). */
    const val FINALE_DELAY_MS = 850L
}
