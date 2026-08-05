@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.vmc

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.LeaderSensor
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.lib.convergeSuccess
import it.unibo.collektive.lib.findPotential
import it.unibo.collektive.lib.isLeader
import it.unibo.collektive.lib.obtainLocalSuccess
import it.unibo.collektive.lib.spreadResource
import it.unibo.collektive.utils.Spawner
import it.unibo.collektive.utils.Stability
import it.unibo.collektive.utils.determineStability
import it.unibo.collektive.alchemist.device.properties.CBF

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies.
 */
fun Aggregate<Int>.withSpawning(
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
        spawnAndDestroyAfterStability()
    }
}

/**
 * Spawns a new node or destroys an old one if the conditions are met.
 * The node is spawned if the local resources are above the lower bound threshold,
 * if it has less than a maximum threshold of children and the neighborhood is stable.
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
fun Aggregate<Int>.spawnAndDestroyAfterStability(): Double =
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

/**
 * The VMC algorithm with the spawning and destroying of nodes.
 * First it elects the leader, then it calculates the potential,
 * the local success, and the overall success of the children.
 * Finally, it calculates the local resource and checks the spawn and destroy policies.
 */
context(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    resourceSensor: ResourceSensor,
    successSensor: SuccessSensor,
)
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.vmc(
    spawner: Spawner<ID>,
): Double {
    val isLeader = isLeader()
    val potential = findPotential(isLeader)
    val localSuccess = obtainLocalSuccess()
    val success = convergeSuccess(potential, localSuccess)
    val localResource = spreadResource(potential, success)
    spawner(potential, localSuccess, success, localResource)
    return localResource
}
