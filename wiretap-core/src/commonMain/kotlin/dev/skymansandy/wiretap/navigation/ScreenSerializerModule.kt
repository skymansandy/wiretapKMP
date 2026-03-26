package dev.skymansandy.wiretap.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.CreateRuleScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.HomeScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.HttpDetailScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.RuleDetailScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.SocketDetailScreen
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
        }
    }
}
