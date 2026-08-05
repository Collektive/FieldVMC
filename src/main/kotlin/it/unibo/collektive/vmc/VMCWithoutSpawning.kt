package it.unibo.collektive.vmc

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LeaderSensor
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.lib.convergeSuccess
import it.unibo.collektive.lib.findPotential
import it.unibo.collektive.lib.isLeader
import it.unibo.collektive.lib.obtainLocalSuccess
import it.unibo.collektive.lib.spreadResource
import it.unibo.collektive.stdlib.accumulation.findParent

/**
 * The VMC algorithm without spawning and destroying after stability policies.
 * First it elects the leader, then it calculates the potential,
 * the local success, and the overall success of the children.
 * Finally, it calculates and returns the local resource.
 */
fun Aggregate<Int>.withoutSpawning(
    env: EnvironmentVariables,
    device: CollektiveDevice<*>,
    leaderS: LeaderSensor,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double {
    val isLeader = isLeader(device, leaderS, resourceS)
    val potential = findPotential(device, isLeader)
    env["potential"] = potential
    env["parent"] = findParent(potential)
    val localSuccess = obtainLocalSuccess(successS)
    val success = convergeSuccess(successS, potential, localSuccess)
    val localResource = spreadResource(env, resourceS, potential, success)
        .also { env["local-resource"] = it }
    return localResource
}
