package dimensional.usagi.tools

import mu.KLogger
import kotlin.time.measureTimedValue

public inline fun <T : Any> KLogger.measure(
    message: String,
    log: (message: () -> String) -> Unit = ::info,
    block: () -> T
): T {
    val (value, took) = measureTimedValue(block)
    log { "$message $took" }

    return value
}
