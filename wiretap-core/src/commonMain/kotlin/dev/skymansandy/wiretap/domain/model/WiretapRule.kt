/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher

/**
 * A rule that intercepts matching HTTP requests to apply a [RuleAction] (mock or throttle).
 *
 * Rules are evaluated in order — the first enabled rule whose criteria all match wins.
 * All criteria use AND logic: method AND URL AND headers AND body must all match.
 *
 * @property id Database-generated identifier.
 * @property method HTTP method to match, or `"*"` for all methods (default).
 * @property urlMatcher Optional URL matching strategy.
 * @property headerMatchers Zero or more header conditions (all must match).
 * @property bodyMatcher Optional request body matching strategy.
 * @property action The action to perform when the rule matches ([RuleAction.Mock] or [RuleAction.Throttle]).
 * @property enabled Whether this rule is active. Disabled rules are skipped during evaluation.
 * @property createdAt Timestamp when the rule was created (epoch millis).
 */
data class WiretapRule(
    val id: Long = 0,
    val method: String = "*",
    val urlMatcher: UrlMatcher? = null,
    val headerMatchers: List<HeaderMatcher> = emptyList(),
    val bodyMatcher: BodyMatcher? = null,
    val action: RuleAction,
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)
