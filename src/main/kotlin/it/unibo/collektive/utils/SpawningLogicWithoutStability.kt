@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.utils

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.model.Position
import it.unibo.collektive.model.minus
import it.unibo.collektive.model.plus
import it.unibo.common.AngularSector
import it.unibo.common.calculateAngle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Type alias for a function that spawns devices in an aggregate given some parameters:
 * - [devSpawn] the device spawn sensor;
 * - [locationSensor] the location sensor;
 * - [potential] the potential of the node;
 * - [localSuccess] the local success of the node;
 * - [success] the global success of the node;
 * - [localResource] the local resources of the node.
 */
typealias SpawnerNoStability<ID> = Aggregate<ID>.(
    potential: Double,
    localSuccess: Double,
    success: Double,
    localResource: Double,
) -> Unit

// data class Stability(val spawnStable: Boolean = false, val destroyStable: Boolean = false) {
//    infix fun and(other: Stability): Boolean =
//    spawnStable && other.spawnStable && destroyStable && other.destroyStable
// }

/**
 * The policies that determine if a node should be spawned or destroyed.
 * The node is spawned if the local resources are above the lower bound threshold,
 * if it has less than a maximum threshold of children and the neighborhood is stable.
 * The node is destroyed if the local resources are below the lower bound,
 * if it is not father of any node and the neighborhood is stable.
 */
context(
    random: RandomGenerator,
    resourceS: ResourceSensor,
    devSpawn: DeviceSpawn,
)
fun determineSpawn(
    childrenCount: Int,
    localResource: Double,
    localPosition: Position,
    neighborPositions: List<Position>,
) {
    if (neighborPositions.isEmpty() ||
        localResource / (2 + childrenCount) > resourceS.resourceLowerBound &&
        childrenCount < devSpawn.maxChildren
    ) {
        val relativePositions = neighborPositions.map { it - localPosition }
        val angles = relativePositions.map { atan2(it.y, it.x) }.sorted()
        val angle = calculateAngle(angles, random, devSpawn.maxChildren, listOf(AngularSector(0.0, 2 * PI)))
        if (!angle.isNaN()) {
            val absoluteDestination = localPosition + Position(
                devSpawn.cloningRange * cos(angle),
                devSpawn.cloningRange * sin(angle),
            )
            devSpawn.spawn(absoluteDestination)
        }
    }
}
