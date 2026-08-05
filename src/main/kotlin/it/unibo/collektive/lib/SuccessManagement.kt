@file:Suppress("UndocumentedPublicFunction")

package it.unibo.collektive.lib

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.coordination.convergeCast

/**
 * Converge the success of the children nodes to itself.
 */
context(successSensor: SuccessSensor)
inline fun <reified ID> Aggregate<ID>.convergeSuccess(
    potential: Double,
    localSuccess: Double,
): Double where ID : Comparable<ID> =
    convergeCast(potential, localSuccess) { a, b -> a + b }.also { successSensor.setSuccess(it) }

/**
 * Get the local success of the node.
 */
context(successSensor: SuccessSensor)
fun obtainLocalSuccess(): Double = successSensor.getLocalSuccess().also { successSensor.setLocalSuccess(it) }
