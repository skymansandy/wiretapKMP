package dev.skymansandy.wiretap.helper.initializer

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.startup.Initializer
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationManager
import dev.skymansandy.wiretap.helper.launcher.getLaunchIntent
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.open_wiretap_console
import dev.skymansandy.wiretap.resources.wiretap
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

internal class WiretapInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        WiretapContextProvider.init(context)
        WiretapNotificationManager.createChannel(context)
        addWiretapShortcut(context)
    }

    private fun addWiretapShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
            val shortcut = ShortcutInfo.Builder(context, "wiretap_inspector")
                .setShortLabel(runBlocking { getString(Res.string.wiretap) })
                .setLongLabel(runBlocking { getString(Res.string.open_wiretap_console) })
                .setIntent(getLaunchIntent())
                .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
