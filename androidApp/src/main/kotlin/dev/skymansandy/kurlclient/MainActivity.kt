package dev.skymansandy.kurlclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.kurlclient.db.AppDatabase
import dev.skymansandy.kurlclient.db.createDatabaseDriver
import dev.skymansandy.kurlclient.db.initAndroidContext
import dev.skymansandy.kurlclient.ui.theme.KurlClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAndroidContext(this)
        AppDatabase.init(createDatabaseDriver())
        enableEdgeToEdge()
        setContent {
            KurlClientTheme {
                App()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KurlClientTheme {
        Greeting("Android")
    }
}