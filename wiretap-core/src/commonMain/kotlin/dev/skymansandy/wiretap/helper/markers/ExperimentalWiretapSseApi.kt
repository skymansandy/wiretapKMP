/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.markers

/**
 * Marks SSE (Server-Sent Events) inspection APIs as experimental.
 *
 * SSE support is in early preview and under active development.
 * APIs marked with this annotation may change in future releases.
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(
    message = "SSE inspection is experimental. APIs may change in future releases.",
    level = RequiresOptIn.Level.WARNING,
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalWiretapSseApi
