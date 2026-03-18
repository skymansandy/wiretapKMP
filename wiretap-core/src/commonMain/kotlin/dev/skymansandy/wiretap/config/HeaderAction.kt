package dev.skymansandy.wiretap.config

/**
 * Specifies how a header should be treated in logged data.
 * The original request/response objects are never mutated.
 *
 * @see WiretapConfig.headerAction
 */
sealed class HeaderAction {
    /** Header is logged as-is. */
    object Keep : HeaderAction()

    /** Header is omitted from logged data entirely. */
    object Skip : HeaderAction()

    /** Header value is replaced with [mask] in logged data. */
    data class Mask(val mask: String = "***") : HeaderAction()
}

/**
 * Applies a [headerAction] function to every entry, returning a new map.
 * Used internally by plugins before passing data to the orchestrator.
 */
fun Map<String, String>.applyHeaderAction(headerAction: (key: String) -> HeaderAction): Map<String, String> {
    val result = mutableMapOf<String, String>()
    for ((key, value) in this) {
        when (val action = headerAction(key)) {
            is HeaderAction.Keep -> result[key] = value
            is HeaderAction.Skip -> Unit
            is HeaderAction.Mask -> result[key] = action.mask
        }
    }
    return result
}
