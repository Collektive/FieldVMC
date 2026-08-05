@file:Suppress("UndocumentedPublicFunction")

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
context(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    resourceSensor: ResourceSensor,
)
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.chooseLeader(
): ID = boundedElection(resourceSensor.getResource(), leaderSensor.leaderRadius)

/**
 * Find the potential of the current node.
 */
context(device: CollektiveDevice<*>)
inline fun <reified ID: Comparable<ID>> Aggregate<ID>.findPotential(
    leader: Boolean,
): Double = distanceTo(leader, with(device) { distances() })

/**
 * Check if the current node is the leader.
 */
context(
    device: CollektiveDevice<*>,
    leaderSensor: LeaderSensor,
    resourceSensor: ResourceSensor,
)
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.isLeader(
): Boolean = (chooseLeader() == localId).also { leaderSensor.setLeader(it) }
