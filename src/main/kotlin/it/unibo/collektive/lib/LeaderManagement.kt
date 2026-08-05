package it.unibo.collektive.lib

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.LeaderSensor
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.coordination.boundedElection
import it.unibo.collektive.stdlib.spreading.distanceTo

/**
 * Elect the leader of the current node.
 */
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.chooseLeader(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    resourceSensor: ResourceSensor,
): ID = boundedElection(device, resourceSensor.getResource(), leaderSensor.leaderRadius)

/**
 * Find the potential of the current node.
 */
inline fun <reified ID: Comparable<ID>> Aggregate<ID>.findPotential(
    device: CollektiveDevice<*>,
    leader: Boolean,
): Double = distanceTo(leader, with(device) { distances() })

/**
 * Check if the current node is the leader.
 */
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.isLeader(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    resourceSensor: ResourceSensor,
): Boolean = (chooseLeader(device, leaderSensor, resourceSensor) == localId).also { leaderSensor.setLeader(it) }
