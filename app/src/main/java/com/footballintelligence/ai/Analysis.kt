package com.footballintelligence.ai

import kotlin.math.exp
import kotlin.math.max

val Played.points: Int get() = if (gf > ga) 3 else if (gf == ga) 1 else 0
val TeamForm.wins: Int get() = matches.count { it.gf > it.ga }
val TeamForm.draws: Int get() = matches.count { it.gf == it.ga }
val TeamForm.losses: Int get() = matches.count { it.gf < it.ga }
val TeamForm.goalsFor: Int get() = matches.sumOf { it.gf }
val TeamForm.goalsAgainst: Int get() = matches.sumOf { it.ga }
val TeamForm.avgFor: Double get() = goalsFor.toDouble() / matches.size
val TeamForm.avgAgainst: Double get() = goalsAgainst.toDouble() / matches.size
val TeamForm.pointsTotal: Int get() = matches.sumOf { it.points }
val TeamForm.scoredIn: Int get() = matches.count { it.gf > 0 }
val TeamForm.cleanSheets: Int get() = matches.count { it.ga == 0 }

data class MatchAnalysis(
    val home: TeamForm, val away: TeamForm,
    val expHome: Double, val expAway: Double,
    val pHome: Double, val pDraw: Double, val pAway: Double,
    val pOver25: Double, val pBothScore: Double,
    val likelyScore: String, val verdict: String
)

private fun poisson(k: Int, lambda: Double): Double {
    var p = exp(-lambda)
    for (i in 1..k) p *= lambda / i
    return p
}

fun buildAnalysis(home: TeamForm, away: TeamForm): MatchAnalysis {
    val lh = max(0.2, (home.avgFor + away.avgAgainst) / 2.0)
    val la = max(0.2, (away.avgFor + home.avgAgainst) / 2.0)
    var pH = 0.0; var pD = 0.0; var pA = 0.0; var over = 0.0; var both = 0.0
    var best = 0.0; var bh = 0; var ba = 0
    for (h in 0..8) for (a in 0..8) {
        val p = poisson(h, lh) * poisson(a, la)
        if (h > a) pH += p else if (h == a) pD += p else pA += p
        if (h + a >= 3) over += p
        if (h > 0 && a > 0) both += p
        if (p > best) { best = p; bh = h; ba = a }
    }
    val total = pH + pD + pA
    pH /= total; pD /= total; pA /= total
    val verdict = when {
        pH >= 0.45 && pH > pA + 0.10 -> "Ventaja para " + home.name + " según su forma reciente."
        pA >= 0.45 && pA > pH + 0.10 -> "Ventaja para " + away.name + " según su forma reciente."
        else -> "Partido parejo: ninguno se separa con claridad."
    }
    return MatchAnalysis(home, away, lh, la, pH, pD, pA, over / total, both / total, bh.toString() + " - " + ba, verdict)
}
