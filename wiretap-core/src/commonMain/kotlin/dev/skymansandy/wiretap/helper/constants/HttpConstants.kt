/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.constants

/**
 * HTTP methods offered in the log filter. Includes `QUERY` (RFC 10008).
 */
internal val HTTP_METHODS = listOf(
    "GET",
    "POST",
    "PUT",
    "PATCH",
    "DELETE",
    "HEAD",
    "OPTIONS",
    "QUERY",
)

/**
 * [HTTP_METHODS] prefixed with the `*` wildcard that rules use to match any method.
 */
internal val RULE_HTTP_METHODS = listOf("*") + HTTP_METHODS
