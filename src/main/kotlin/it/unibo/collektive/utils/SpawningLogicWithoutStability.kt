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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Type alias for a function applying the spawning policy of a node, given:
 * - [potential] the potential of the node;
 * - [localSuccess] the local success of the node;
 * - [success] the global success of the node;
 * - [localResource] the local resources of the node.
 *
 * Differently from [Spawner], no stability of the neighborhood is required before spawning,
 * and nodes are never destroyed.
 * The sensors needed to perform the spawning are taken from the context.
 */
typealias SpawnerNoStability<ID> = Aggregate<ID>.(
    potential: Double,
    localSuccess: Double,
    success: Double,
    localResource: Double,
) -> Unit

/**
 * The policy that determines if a node should be spawned, with no stability requirement.
 * The node is spawned if the local resources are above the lower bound threshold
 * and it has less than a maximum threshold of children, or if it has no neighbor at all.
 * The whole space is considered safe, hence the new node may be placed in any direction
 * which is not already occupied by the neighborhood.
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
    val shouldSpawn = neighborPositions.isEmpty() ||
        (
            localResource / (2 + childrenCount) > resourceS.resourceLowerBound &&
                childrenCount < devSpawn.maxChildren
            )
    if (shouldSpawn) {
        val relativePositions = neighborPositions.map { it - localPosition }
        val angles = relativePositions.map { atan2(it.y, it.x) }.sorted()
        val angle = calculateAngle(angles, random, devSpawn.maxChildren, listOf(AngularSector.fullCircle))
        if (!angle.isNaN()) {
            val absoluteDestination = localPosition + Position(
                devSpawn.cloningRange * cos(angle),
                devSpawn.cloningRange * sin(angle),
            )
            devSpawn.spawn(absoluteDestination)
        }
    }
}
