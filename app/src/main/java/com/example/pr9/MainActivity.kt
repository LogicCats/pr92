package com.example.pr9

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.pr9.R
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.Manifest


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение предоставлено, запускаем WorkManager
                startWorkManager()
            } else {
                // Разрешение отклонено, можно уведомить пользователя
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверяем разрешение на отправку уведомлений
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startWorkManager()
            } else {
                // Запрашиваем разрешение
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Для устройств с API ниже 33 разрешение не требуется
            startWorkManager()
        }

        setContent {
            MyApp()
        }
    }

    // Функция для запуска WorkManager
    private fun startWorkManager() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)  // Задержка 10 секунд перед выполнением
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    var title by remember { mutableStateOf("App") } // Дефолтный заголовок

    // Используем LaunchedEffect для отслеживания изменений в навигационном стеке
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = getTitleForRoute(destination.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavigationHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

// Убираем @Composable у функции, она просто возвращает строку
fun getTitleForRoute(route: String?): String {
    return when (route) {
        Screen.Home.route -> "Home"
        Screen.Settings.route -> "Settings"
        Screen.Profile.route -> "Profile"
        Screen.Notifications.route -> "Notifications"
        else -> "App"
    }
}


// Навигация между экранами
@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(Screen.Notifications.route) { NotificationsScreen() }
    }
}

// BottomNavigation для перемещения между экранами
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomAppBar {
        BottomNavigationItem(
            icon = {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.ic_home),
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = navController.currentDestination?.route == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            selected = navController.currentDestination?.route == Screen.Settings.route,
            onClick = { navController.navigate(Screen.Settings.route) }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = navController.currentDestination?.route == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) }
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.ic_notifications),
                    contentDescription = "Notifications"
                )
            },
            label = { Text("Notifications") },
            selected = navController.currentDestination?.route == Screen.Notifications.route,
            onClick = { navController.navigate(Screen.Notifications.route) }
        )
    }
}

// Экран Home
@Composable
fun HomeScreen() {
    Text("Welcome to the Home screen!", modifier = Modifier.padding(16.dp), fontSize = 24.sp)
}

// Экран Settings
@Composable
fun SettingsScreen() {
    Text("Here are the Settings.", modifier = Modifier.padding(16.dp), fontSize = 24.sp)
}

// Экран Profile
@Composable
fun ProfileScreen() {
    Text("This is your Profile screen.", modifier = Modifier.padding(16.dp), fontSize = 24.sp)
}

// Экран Notifications
@Composable
fun NotificationsScreen() {
    Text("You have no new Notifications.", modifier = Modifier.padding(16.dp), fontSize = 24.sp)
}

// Маршруты для экранов
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp()
}
