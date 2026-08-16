package com.dmesh.prototype.mesh.routing

/**
 * Route cost function for path selection.
 *
 * routeScore = hopPenalty * hops
 *            + signalPenalty * (100 - avgRssi)
 *            + batteryPenalty * (100 - minBattery)
 *            + staleRoutePenalty * routeAgeSeconds
 *
 * Lower score is better.
 */
object RouteScorer {
    const val HOP_PENALTY = 10.0
    const val SIGNAL_PENALTY = 0.5
    const val BATTERY_PENALTY = 0.3
    const val STALE_ROUTE_PENALTY = 0.1

    data class RouteCandidate(
        val path: List<String>,
        val avgRssi: Int,
        val minBattery: Int,
        val routeAgeSeconds: Long
    ) {
        fun score(): Double {
            val hops = (path.size - 1).coerceAtLeast(0)
            return hops * HOP_PENALTY +
                SIGNAL_PENALTY * (100 - avgRssi.coerceIn(0, 100)) +
                BATTERY_PENALTY * (100 - minBattery.coerceIn(0, 100)) +
                STALE_ROUTE_PENALTY * routeAgeSeconds
        }

        fun estimatedReliability(): Int {
            val score = score()
            val reliability = (100 - score.coerceIn(0.0, 100.0)).toInt()
            return reliability.coerceIn(0, 99)
        }
    }

    fun selectBest(candidates: List<RouteCandidate>): RouteCandidate? =
        candidates.minByOrNull { it.score() }
}
