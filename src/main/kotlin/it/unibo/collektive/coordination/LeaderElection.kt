package it.unibo.collektive.coordination

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.stdlib.consensus.boundedElection

/**
 * Elect the leader in an area limited by the [radius], based on the [localStrength] of the node.
 */
inline fun <reified ID : Any, reified C : Comparable<C>> Aggregate<ID>.boundedElection(
    device: CollektiveDevice<*>,
    localStrength: C,
    radius: Double,
): ID = with(device) {
    boundedElection(
        strength = localStrength,
        bound = radius,
        metric = distances()
    )
}
