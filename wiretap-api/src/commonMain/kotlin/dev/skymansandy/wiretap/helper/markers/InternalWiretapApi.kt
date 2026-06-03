/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.markers

/**
 * Marks declarations that are part of Wiretap's cross-module wiring rather
 * than its public API surface.
 *
 * These declarations have to be public so the integration modules
 * (`wiretap-ktor`, `wiretap-okhttp`) can call into them, but library
 * consumers should not depend on them — signatures may change without
 * notice and there are no compatibility guarantees.
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(
    message = "This API is internal to Wiretap's cross-module wiring and may change without notice.",
    level = RequiresOptIn.Level.ERROR,
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalWiretapApi
