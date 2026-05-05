package org.secondthought.countdoodle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.secondthought.countdoodle.ui.detail.EventDetailScreen
import org.secondthought.countdoodle.ui.edit.EventEditScreen
import org.secondthought.countdoodle.ui.list.EventListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLinkEventId: Long? = intent?.data
            ?.takeIf { it.scheme == "countdoodle" && it.host == "event" }
            ?.lastPathSegment?.toLongOrNull()

        setContent {
            CountDoodleTheme {
                Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
                    AppNav(initialEventId = deepLinkEventId)
                }
            }
        }
    }
}

@Composable
private fun CountDoodleTheme(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val dark = isSystemInDarkTheme()
    val colors = remember(dark) {
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun AppNav(initialEventId: Long?) {
    val nav = rememberNavController()
    val start = if (initialEventId != null) "detail/$initialEventId" else "list"
    NavHost(navController = nav, startDestination = start) {
        composable("list") {
            EventListScreen(
                onAdd = { nav.navigate("edit/new") },
                onOpen = { id -> nav.navigate("detail/$id") },
            )
        }
        composable("edit/{id}") { entry ->
            val raw = entry.arguments?.getString("id")
            val id = raw?.toLongOrNull()
            EventEditScreen(
                eventId = id,
                onDone = { nav.popBackStack() },
            )
        }
        composable("detail/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            EventDetailScreen(
                eventId = id,
                onEdit = { nav.navigate("edit/$id") },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
