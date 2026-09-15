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
import it.unibo.collektive.model.Position
import it.unibo.collektive.utils.Spawner
import it.unibo.collektive.utils.Stability
import it.unibo.collektive.utils.determineStability

/**
 * A safe-space checker modelling the complete absence of barriers.
 *
 * It reports every point of the space as safe, and it does so with a distance from the (non-existing)
 * border which is always larger than the spawning diameter: this way the sampling performed by
 * `findZeros` stops immediately and the whole circumference around the node,
 * i.e. `AngularSector(0.0, 2 * PI)`, is returned as the only safe sector.
 */
private val noBarriers: (Position) -> Double = { Double.MAX_VALUE }

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies.
 *
 * This is the baseline behaviour: no barrier constrains the space, hence a node may spawn its children
 * in any direction which is not already occupied by its neighborhood.
 * See [withSpawningWithBarriers] for the variant constrained by a Control Barrier Function.
 */
fun Aggregate<Int>.withSpawning(
    device: CollektiveDevice<*>,
    devSpawn: DeviceSpawn,
    leaderS: LeaderSensor,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double = context(device, leaderS, locationS, random) {
    context(resourceS, successS, devSpawn) {
        spawnAndDestroyAfterStability()
    }
}

/**
 * Spawns a new node or destroys an old one if the conditions are met, with no spatial barrier.
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
                noBarriers,
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
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.vmc(spawner: Spawner<ID>): Double {
    val isLeader = isLeader()
    val potential = findPotential(isLeader)
    val localSuccess = obtainLocalSuccess()
    val success = convergeSuccess(potential, localSuccess)
    val localResource = spreadResource(potential, success)
    spawner(potential, localSuccess, success, localResource)
    return localResource
}
