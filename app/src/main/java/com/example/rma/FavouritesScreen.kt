package com.example.rma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FavouritesScreen() {
    Text(text = "Favourites Screen", modifier = Modifier.background(Color(0xFFF0F0F0)).fillMaxSize(), textAlign = TextAlign.Center)
}