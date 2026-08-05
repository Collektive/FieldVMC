@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.vmc

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.model.Position
import it.unibo.collektive.stdlib.accumulation.findParent
import it.unibo.collektive.stdlib.collapse.countMatching

/**
 * Updates the environment variables related to the neighborhood and returns the spatial data
 * necessary for spawning and destruction policies.
 */
context(
    environmentVariables: EnvironmentVariables,
    locationSensor: LocationSensor,
)
inline fun <reified ID : Comparable<ID>> Aggregate<ID>.extractNeighborhoodPositions(potential: Double): LocalInfo {
    val children = neighboring(findParent(potential))
    val childrenCount = children.neighbors.countMatching { it.value == localId }
    environmentVariables["parent"] = children.local.value
    val positions = neighboring(locationSensor.coordinates())
    return LocalInfo(
        childrenCount = childrenCount,
        localPosition = positions.local.value,
        neighborPositions = positions.neighbors.values.list,
    )
}

/** Spatial information about a device and its neighborhood. */
data class LocalInfo(
    /** The number of neighboring devices whose parent is the local device. */
    val childrenCount: Int,

    /** The position of the local device. */
    val localPosition: Position,

    /** The positions of the neighboring devices. */
    val neighborPositions: List<Position>,
)
