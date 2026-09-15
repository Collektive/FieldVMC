@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.lib

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.coordination.spreadToChildren

/**
 * Spreads the available resources to the children of this device,
 * proportionally to the [success] of each child.
 */
context(
    environmentVariables: EnvironmentVariables,
    resourceSensor: ResourceSensor,
)
inline fun <reified ID> Aggregate<ID>.spreadResource(
    potential: Double,
    success: Double,
): Double where ID : Comparable<ID> =
    spreadToChildren(potential, if (potential > 0) 0.0 else resourceSensor.getResource(), success).also {
        resourceSensor.setCurrentOverallResource(it)
    }

/**
 * Given a fixed [resource] value for the root, spreads the available resources to the children of this device,
 * proportionally to the [success] of each child.
 */
context(environmentVariables: EnvironmentVariables)
inline fun <reified ID> Aggregate<ID>.spreadResource(
    potential: Double,
    success: Double,
    resource: Double,
): Double where ID : Comparable<ID> = spreadToChildren(potential, if (potential > 0) 0.0 else resource, success)
