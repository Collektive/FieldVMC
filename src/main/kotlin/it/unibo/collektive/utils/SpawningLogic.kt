@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.utils

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.model.Position
import it.unibo.collektive.model.minus
import it.unibo.collektive.model.plus
import it.unibo.common.calculateAngle
import it.unibo.common.findSafeSectors
import java.io.Serializable
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
typealias Spawner<ID> = Aggregate<ID>.(
    potential: Double,
    localSuccess: Double,
    success: Double,
    localResource: Double,
) -> Unit

/**
 * Represents the stability state of a node regarding its ability to spawn or destroy.
 *
 * @property spawnStable Indicates whether the node is stable for spawning.
 * @property destroyStable Indicates whether the node is stable for destruction.
 */
data class Stability(
    val spawnStable: Boolean = false,
    val destroyStable: Boolean = false,
) : Serializable {

    /**
     * Companion object containing the serialization version UID.
     */
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    /**
     * Combines this stability state with another one using a logical AND.
     *
     * @param other The other stability state to combine with.
     * @return True if both states are completely stable for both spawning and destruction.
     */
    infix fun and(other: Stability): Boolean = spawnStable && other.spawnStable && destroyStable && other.destroyStable
}

/**
 * The policies that determine if a node should be spawned or destroyed.
 * The node is spawned if the local resources are above the lower-bound threshold,
 * if it has less than a maximum threshold of children and the neighborhood is stable.
 * The node is destroyed if the local resources are below the lower bound,
 * if it is not father of any node and the neighborhood is stable.
 */
context(
    random: RandomGenerator,
    resourceSensor: ResourceSensor,
    devSpawn: DeviceSpawn,
)
fun determineStability(
    childrenCount: Int,
    localResource: Double,
    lastChanged: Double,
    now: Double,
    potential: Double,
    localPosition: Position,
    neighborPositions: List<Position>,
    localStability: Stability,
    safeSpaceChecker: (Position) -> Double,
): Stability {
    val enoughTime = now > lastChanged + devSpawn.minSpawnWait
    val everyoneIsDestroyStable = now > lastChanged
    val everyoneIsStable = localStability.spawnStable && localStability.destroyStable && enoughTime

    val shouldDestroy =
        potential > 0.0 &&
            childrenCount == 0 &&
            localResource < resourceSensor.resourceLowerBound &&
            everyoneIsDestroyStable

    val shouldSpawn =
        neighborPositions.isEmpty() ||
            (localResource / (2 + childrenCount) > resourceSensor.resourceLowerBound &&
                childrenCount < devSpawn.maxChildren &&
                everyoneIsStable)

    return when {
        shouldDestroy -> {
            devSpawn.selfDestroy()
            Stability(spawnStable = false, destroyStable = false)
        }
        shouldSpawn -> executeSpawnLogic(
            localPosition,
            neighborPositions,
            localStability,
            safeSpaceChecker,
        )

        else -> Stability(spawnStable = enoughTime, destroyStable = everyoneIsDestroyStable)
    }
}

context(
    random: RandomGenerator,
    devSpawn: DeviceSpawn,
)
private fun executeSpawnLogic(
    localPosition: Position,
    neighborPositions: List<Position>,
    localStability: Stability,
    safeSpaceChecker: (Position) -> Double,
): Stability {
    val safeSectors = findSafeSectors(devSpawn.cloningRange) { angle ->
        val x = devSpawn.cloningRange * cos(angle)
        val y = devSpawn.cloningRange * sin(angle)
        safeSpaceChecker(localPosition + Position(x, y))
    }
    val relativePositions = neighborPositions.map { it - localPosition }
    val angles = relativePositions.map { atan2(it.y, it.x) }.sorted()
    val angle = calculateAngle(angles, random, devSpawn.maxChildren, safeSectors)

    return when {
        angle.isNaN() -> Stability(spawnStable = true, destroyStable = true)
        else -> {
            val x = devSpawn.cloningRange * cos(angle)
            val y = devSpawn.cloningRange * sin(angle)
            val absoluteDestination = localPosition + Position(x, y)
            devSpawn.spawn(absoluteDestination)
            Stability(spawnStable = false, destroyStable = localStability.destroyStable)
        }
    }
}
