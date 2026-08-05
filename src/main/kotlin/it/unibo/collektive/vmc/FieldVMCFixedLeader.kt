package it.unibo.collektive.vmc

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.lib.convergeSuccess
import it.unibo.collektive.lib.findPotential
import it.unibo.collektive.lib.obtainLocalSuccess
import it.unibo.collektive.lib.spreadResource
import it.unibo.collektive.stdlib.accumulation.findParent
import it.unibo.collektive.stdlib.collapse.countMatching
import it.unibo.collektive.utils.SpawnerNoStability
import it.unibo.collektive.utils.determineSpawn

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies.
 */
fun Aggregate<Int>.fixedRootWithSpawning(
    devSpawn: DeviceSpawn,
    device: CollektiveDevice<*>,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double = fixedRootStability(devSpawn, device, env, locationS, random, resourceS, successS)

/**
 * Executes the VMC algorithm with a fixed root, incorporating stability checks for spawning.
 */
fun Aggregate<Int>.fixedRootStability(
    devSpawn: DeviceSpawn,
    device: CollektiveDevice<*>,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double =
    with(device) {
        vmcFixedLeader(
            devSpawn,
            device,
            env,
            locationS,
            resourceS,
            successS,
        ) { devSpawn, locationS, potential: Double, localSuccess: Double, success: Double, localResource: Double ->
            val (childrenCount, localPosition, neighborPositions) = extractNeighborhood(potential, env, locationS)
            determineSpawn(childrenCount, localResource, localPosition, neighborPositions, devSpawn, random, resourceS)
        }
    }

/**
 * Core execution logic of the VMC algorithm with a fixed leader.
 */
fun Aggregate<Int>.vmcFixedLeader(
    devSpawn: DeviceSpawn,
    device: CollektiveDevice<*>,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
    spawner: SpawnerNoStability,
): Double {
    val isLeader = env.get<Boolean>("leader")
    val potential = findPotential(device, isLeader)
    val localSuccess = obtainLocalSuccess(successS)
    val success = convergeSuccess(successS, potential, localSuccess)
    val localResource =
        spreadResource(env, potential, success, resourceS.maxResource)
            .also { env["local-resource"] = it }
    spawner(devSpawn, locationS, potential, localSuccess, success, localResource)
    return localResource
}
