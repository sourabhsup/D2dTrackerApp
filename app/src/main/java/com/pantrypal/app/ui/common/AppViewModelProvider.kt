package com.pantrypal.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pantrypal.app.PantryPalApp

@Composable
fun rememberApp(): PantryPalApp {
    val context = LocalContext.current.applicationContext
    return context as PantryPalApp
}
