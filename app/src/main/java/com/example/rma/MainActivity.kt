
package com.example.rma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.rma.ui.theme.RMATheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Map : Screen("map", "Map", R.drawable.ic_map)
    object Find : Screen("search", "Search", R.drawable.ic_search)
    object Favourites : Screen("favourites", "Faves", R.drawable.ic_favourite)
    object Profile : Screen("profile", "Profile", R.drawable.ic_profile)
    object AddNewLocation: Screen("addnewlocation","Add",R.drawable.ic_add)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RMATheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    RMA()
                }
            }
        }
    }
}

@Composable
fun RMA() {
    val navController = rememberNavController()
    val bottomBarState = remember { mutableStateOf(true) }
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomBarState.value = when (destination.route) {
                Screen.Map.route, Screen.Find.route, Screen.Favourites.route,Screen.AddNewLocation.route, Screen.Profile.route -> true
                else -> false
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (bottomBarState.value) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavigationGraph(navController, Modifier.padding(innerPadding))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation(
        backgroundColor = Color(0xFF82CC1C),
        contentColor = Color.White
    ) {
        val items = listOf(Screen.Map, Screen.Find, Screen.Favourites,Screen.AddNewLocation, Screen.Profile)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            BottomNavigationItem(
                alwaysShowLabel = true,
                selectedContentColor = Color.Black,
                unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(8.dp),
                icon = { Icon(painterResource(id = screen.icon), contentDescription = null) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "login", modifier = modifier) {
        composable(Screen.Map.route) { AnimatedScreen { MapScreen(navController) } }
        composable(Screen.Find.route) { AnimatedScreen { SearchScreen(navController) } }
        composable(Screen.Favourites.route) { AnimatedScreen { FavouritesScreen(navController) } }
        composable(Screen.Profile.route) { AnimatedScreen { ProfileScreen(navController) } }
        composable(Screen.AddNewLocation.route) { AnimatedScreen { AddNewLocationScreen(navController) } }
        if (FirebaseAuth.getInstance().currentUser == null) {
            composable("login") { AnimatedScreen { LoginRegisterScreen(navController) } }
            composable("register") { AnimatedScreen { RegisterScreen(navController) } }
        } else {
            composable("login") {

                navController.navigate(Screen.Map.route)

            }
            composable("register") {

                navController.navigate(Screen.Map.route)

            }
        }
        composable("locationDetail/{documentId}") { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable
            LocationDetailScreenWithAnimation (documentId, navController)
        }

        composable("addnewlocation") { AnimatedScreen {  AddNewLocationScreen(navController) } }

    }
}


@Composable
fun AnimatedScreen(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(animationSpec = tween(700)),
        exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(animationSpec = tween(700)),
    ) {
        content()
    }
}

@Composable
fun LocationDetailScreenWithAnimation(documentId: String, navController: NavController) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(initialAlpha = 0.3f) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500)),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(500))
    ) {
        LocationDetailScreen(documentId, navController)
    }
}