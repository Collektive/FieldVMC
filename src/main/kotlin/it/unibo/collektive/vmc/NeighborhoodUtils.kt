package it.unibo.collektive.vmc

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.stdlib.accumulation.findParent
import it.unibo.collektive.stdlib.collapse.countMatching

/**
 * Updates the environment variables related to the neighborhood and returns the spatial data
 * necessary for spawning and destruction policies.
 */
fun Aggregate<Int>.extractNeighborhood(
    potential: Double,
    env: EnvironmentVariables,
    locationS: LocationSensor
) = run {
    val children = neighboring(findParent(potential))
    env["children-around"] = children
    env["parent"] = children.local.value

    val childrenCount = children.neighbors.countMatching { it.value == localId }
    env["children-count"] = childrenCount

    val neighbors = neighboring(locationS.coordinates())
    val localPosition = neighbors.local.value
    val neighborPositions = locationS.surroundings()

    Triple(childrenCount, localPosition, neighborPositions)
}
