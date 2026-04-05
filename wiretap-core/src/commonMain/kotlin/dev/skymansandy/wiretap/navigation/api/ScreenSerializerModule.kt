/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.api

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.CreateRuleScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HomeScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HttpDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.RuleDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.SelectRuleCriteriaSheet
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.SocketDetailScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val screenSerializersModule = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeScreen::class, HomeScreen.serializer())
            subclass(HttpDetailScreen::class, HttpDetailScreen.serializer())
            subclass(SocketDetailScreen::class, SocketDetailScreen.serializer())
            subclass(RuleDetailScreen::class, RuleDetailScreen.serializer())
            subclass(CreateRuleScreen::class, CreateRuleScreen.serializer())
            subclass(SelectRuleCriteriaSheet::class, SelectRuleCriteriaSheet.serializer())
        }
    }
}
