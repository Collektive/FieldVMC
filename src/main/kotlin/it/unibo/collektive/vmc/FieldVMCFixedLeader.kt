@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.vmc

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.lib.convergeSuccess
import it.unibo.collektive.lib.findPotential
import it.unibo.collektive.lib.obtainLocalSuccess
import it.unibo.collektive.lib.spreadResource
import it.unibo.collektive.utils.SpawnerNoStability
import it.unibo.collektive.utils.determineSpawn

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies.
 */
fun Aggregate<Int>.fixedRootWithSpawning(
    devSpawn: DeviceSpawn,
    device: CollektiveDevice<*>,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double = context(device, locationS, random, resourceS, successS, devSpawn) {
    fixedRootStability()
}

/**
 * Executes the VMC algorithm with a fixed root, incorporating stability checks for spawning.
 */
context(
    device: CollektiveDevice<*>,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
    devSpawn: DeviceSpawn,
)
fun Aggregate<Int>.fixedRootStability(): Double =
    vmcFixedLeader { potential, localSuccess, success, localResource ->
        val (childrenCount, localPosition, neighborPositions) = extractNeighborhoodPositions(potential)
        determineSpawn(childrenCount, localResource, localPosition, neighborPositions)
    }

/**
 * Core execution logic of the VMC algorithm with a fixed leader.
 */
context(
    device: CollektiveDevice<*>,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
)
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.vmcFixedLeader(
    spawner: SpawnerNoStability<ID>,
): Double {
    val isLeader: Boolean = device["leader"]
    val potential = findPotential(isLeader)
    val localSuccess = obtainLocalSuccess()
    val success = convergeSuccess(potential, localSuccess)
    val localResource = spreadResource(potential, success, resourceS.maxResource)
    spawner(potential, localSuccess, success, localResource)
    return localResource
}
