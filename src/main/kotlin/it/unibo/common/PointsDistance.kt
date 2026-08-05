package it.unibo.common

import it.unibo.collektive.model.Position
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculate the Euclidean distance between two points in 2D space.
 */
fun pointsDistance(p1: Position, p2: Position): Double = sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
