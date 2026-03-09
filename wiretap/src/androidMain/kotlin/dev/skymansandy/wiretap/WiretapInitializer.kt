package dev.skymansandy.wiretap

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.startup.Initializer

class WiretapInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        WiretapContextProvider.init(context)
        addWiretapShortcut(context)
    }

    private fun addWiretapShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
            val shortcut = ShortcutInfo.Builder(context, "wiretap_inspector")
                .setShortLabel("Wiretap")
                .setLongLabel("Open Wiretap Inspector")
                .setIntent(
                    Intent(context, WiretapActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                    },
                )
                .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
