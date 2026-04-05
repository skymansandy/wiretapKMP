/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.theme

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction

internal val ResponseSource.ruleColor: Color
    get() = when (this) {
        ResponseSource.Mock -> WiretapColors.RuleMock
        ResponseSource.Throttle -> WiretapColors.RuleThrottle
        ResponseSource.MockAndThrottle -> WiretapColors.RuleMockAndThrottle
        ResponseSource.Network -> Color.Unspecified
    }

internal val RuleAction.Type.ruleColor: Color
    get() = when (this) {
        RuleAction.Type.Mock -> WiretapColors.RuleMock
        RuleAction.Type.Throttle -> WiretapColors.RuleThrottle
        RuleAction.Type.MockAndThrottle -> WiretapColors.RuleMockAndThrottle
    }
