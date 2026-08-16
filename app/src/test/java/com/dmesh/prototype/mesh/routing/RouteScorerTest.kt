package com.dmesh.prototype.mesh.routing

import com.dmesh.prototype.mesh.routing.RouteScorer.RouteCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteScorerTest {
    @Test
    fun lowerHopCountProducesBetterScore() {
        val short = RouteCandidate(listOf("A", "B", "C"), avgRssi = -60, minBattery = 80, routeAgeSeconds = 0)
        val long = RouteCandidate(listOf("A", "B", "D", "E", "C"), avgRssi = -60, minBattery = 80, routeAgeSeconds = 0)
        assertTrue(short.score() < long.score())
    }

    @Test
    fun selectBestReturnsLowestScore() {
        val candidates = listOf(
            RouteCandidate(listOf("A", "B", "C"), -50, 90, 0),
            RouteCandidate(listOf("A", "E", "C"), -70, 40, 10)
        )
        val best = RouteScorer.selectBest(candidates)
        assertEquals(listOf("A", "B", "C"), best?.path)
    }
}
