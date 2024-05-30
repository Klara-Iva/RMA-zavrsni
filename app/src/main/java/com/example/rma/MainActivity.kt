package com.example.rma


import LocationDetailScreen
import RegisterScreen
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.rma.ui.theme.RMATheme
import com.example.rma.MapScreen

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : ComponentActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RMATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val context = LocalContext.current
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val navController = rememberNavController()
                      if (currentUser != null) {
                          NavHost(navController = navController, startDestination = "main") {
                              composable("main") {
                              MainScreen(navController)
                              }
                              composable("locationDetail/{documentId}") { backStackEntry ->
                                      val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable
                                      LocationDetailScreen(documentId)}

                          }
                        }
                    else{
                          NavHost(navController = navController, startDestination = "login") {
                              composable("login") {
                                  LoginRegisterScreen(navController)
                              }
                              composable("register") {
                                  RegisterScreen(navController)
                              }
                          }

                      }
                    }
                }
            }
        }
    }



sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Map : Screen("map", "Map", icon =  R.drawable.ic_map)
    object Find : Screen("search", "Search", icon =  R.drawable.ic_search)
    object Favourites : Screen("favourites", "Favourites", icon =  R.drawable.ic_favourite)
    object Profile : Screen("profile", "Profile",icon =  R.drawable.ic_profile)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(navController2: NavController)
{
    val context = LocalContext.current
    val navController = rememberNavController()
    val items = listOf(Screen.Map, Screen.Find, Screen.Favourites, Screen.Profile)

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color(0xFF82CC1C),
                modifier = Modifier.height(56.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(id = screen.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.label) },
                        alwaysShowLabel = false,
                        selectedContentColor = MaterialTheme.colors.onPrimary,
                        unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.medium),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    ) {
            innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Screen.Map.route) {
                composable(Screen.Map.route) { MapScreen(navController2) }
                composable("locationDetail/{documentId}") { backStackEntry ->
                    val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable
                    LocationDetailScreen(documentId)
                }
                composable(Screen.Find.route) { SearchScreen() }
                composable(Screen.Favourites.route) { FavouritesScreen() }
                composable(Screen.Profile.route) { ProfileScreen(context) }
            }
        }
    }
    }





@Composable
fun FavouritesScreen() {
    Text(text = "Favourites Screen", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
}

@Composable
fun ProfileScreen(context: Context) {

    Button(onClick = { Firebase.auth.signOut()

        (context as MainActivity).recreate()

    }
        ) {
        Text("SignOut")
    }
    Text(text = "Profile Screen", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
}

