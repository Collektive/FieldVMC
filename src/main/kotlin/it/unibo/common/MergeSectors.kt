package it.unibo.common

import kotlin.math.PI
import kotlin.math.abs

/**
 * A small constant used as a tolerance threshold for floating-point comparisons.
 */
const val EPSILON = 1e-8

/**
 * Given an ordered list [sectors]
 * return a list where all the angular sectors that were contiguous are united in one single sector.
 * */
fun mergeSectors(sectors: List<AngularSector>): List<AngularSector> {
    if (sectors.size <= 1) return sectors

    val merged = mutableListOf<AngularSector>()
    var current = sectors.first()

    for (i in 1 until sectors.size) {
        val next = sectors[i]
        if (abs(current.from + current.arc - next.from) < EPSILON) {
            current = AngularSector(current.from, current.arc + next.arc)
        } else {
            merged.add(current)
            current = next
        }
    }
    merged.add(current)

    return merged
}

/**
 * Given an ordered list [sectors] check if the last angular sector is contiguous to the first one,
 * if so, merge them.
 */
fun wrapSectors(sectors: List<AngularSector>): List<AngularSector> {
    if (sectors.size <= 1) return sectors

    val first = sectors.first()
    val last = sectors.last()

    return if (abs(last.from + last.arc - (first.from + 2 * PI)) < EPSILON) {
        val combinedArc = last.arc + first.arc
        val merged = AngularSector(last.from, combinedArc)
        val middleArcs = sectors.drop(1).dropLast(1)

        middleArcs + merged
    } else {
        sectors
    }
}
