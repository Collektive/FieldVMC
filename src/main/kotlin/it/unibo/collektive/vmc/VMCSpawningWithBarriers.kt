@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.vmc

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.properties.CBF
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.LeaderSensor
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.utils.Stability
import it.unibo.collektive.utils.determineStability

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies,
 * constrained by the Control Barrier Function provided by the [cbf] property:
 * new nodes are only spawned in the portion of space marked as safe.
 *
 * See [withSpawning] for the unconstrained baseline.
 */
fun Aggregate<Int>.withSpawningWithBarriers(
    device: CollektiveDevice<*>,
    devSpawn: DeviceSpawn,
    leaderS: LeaderSensor,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
    cbf: CBF,
): Double = context(device, leaderS, locationS, random) {
    context(resourceS, successS, devSpawn, cbf) {
        spawnAndDestroyAfterStabilityWithBarriers()
    }
}

/**
 * Spawns a new node or destroys an old one if the conditions are met, honouring the spatial barriers.
 * The node is spawned if the local resources are above the lower bound threshold,
 * if it has less than a maximum threshold of children, the neighborhood is stable
 * and the destination lies in a safe sector according to the Control Barrier Function.
 * The node is destroyed if the local resources are below the lower bound,
 * if it is not father of any node and the neighborhood is stable.
 */
context(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
    devSpawn: DeviceSpawn,
    cbf: CBF,
)
fun Aggregate<Int>.spawnAndDestroyAfterStabilityWithBarriers(): Double =
    vmc { potential, localSuccess, success, localResource ->
        val (childrenCount, localPosition, neighborPositions) = extractNeighborhoodPositions(potential)
        val now = devSpawn.currentTime()
        share(Stability()) { neighborhoodStability ->
            val lastChanged =
                evolve(now to listOf(potential, localSuccess, success, localResource)) { last ->
                    val current = listOf(potential, localSuccess, success, localResource)
                    if (current == last.second) {
                        last
                    } else {
                        now to current
                    }
                }.first
            val localStability = neighborhoodStability.local.value
            determineStability(
                childrenCount,
                localResource,
                lastChanged,
                now,
                potential,
                localPosition,
                neighborPositions,
                localStability,
                cbf::isSafe,
            )
        }
    }
