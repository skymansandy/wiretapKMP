package dev.skymansandy.wiretap.helper.initializer

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.startup.Initializer
import dev.skymansandy.wiretap.helper.launcher.WiretapIconFactory
import dev.skymansandy.wiretap.helper.launcher.getLaunchIntent
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.setResourceReaderAndroidContext

internal class WiretapInitializer : Initializer<Unit> {

    @OptIn(ExperimentalResourceApi::class)
    override fun create(context: Context) {
        WiretapContextProvider.init(context)
        setResourceReaderAndroidContext(context)
        WiretapNotificationManager.createChannel(context)
        addWiretapShortcut(context)
    }

    private fun addWiretapShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
            val shortcut = ShortcutInfo.Builder(context, "wiretap_inspector")
                .setShortLabel("Wiretap")
                .setLongLabel("Open Wiretap Console")
                .setIcon(WiretapIconFactory.createShortcutIcon())
                .setIntent(getLaunchIntent())
                .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
