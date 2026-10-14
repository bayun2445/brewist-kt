package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CoffeePrimaryDark,
    onPrimary = CoffeeOnPrimaryDark,
    primaryContainer = CoffeePrimaryContainerDark,
    onPrimaryContainer = CoffeeOnPrimaryContainerDark,
    secondary = CoffeeSecondaryDark,
    onSecondary = CoffeeOnSecondaryDark,
    secondaryContainer = CoffeeSecondaryContainerDark,
    onSecondaryContainer = CoffeeOnSecondaryContainerDark,
    tertiary = CoffeeTertiaryDark,
    onTertiary = CoffeeOnTertiaryDark,
    tertiaryContainer = CoffeeTertiaryContainerDark,
    onTertiaryContainer = CoffeeOnTertiaryContainerDark,
    background = CoffeeBackgroundDark,
    onBackground = CoffeeOnBackgroundDark,
    surface = CoffeeSurfaceDark,
    onSurface = CoffeeOnSurfaceDark,
    surfaceVariant = CoffeeSurfaceVariantDark,
    onSurfaceVariant = CoffeeOnSurfaceVariantDark,
    outline = CoffeeOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = CoffeePrimaryLight,
    onPrimary = CoffeeOnPrimaryLight,
    primaryContainer = CoffeePrimaryContainerLight,
    onPrimaryContainer = CoffeeOnPrimaryContainerLight,
    secondary = CoffeeSecondaryLight,
    onSecondary = CoffeeOnSecondaryLight,
    secondaryContainer = CoffeeSecondaryContainerLight,
    onSecondaryContainer = CoffeeOnSecondaryContainerLight,
    tertiary = CoffeeTertiaryLight,
    onTertiary = CoffeeOnTertiaryLight,
    tertiaryContainer = CoffeeTertiaryContainerLight,
    onTertiaryContainer = CoffeeOnTertiaryContainerLight,
    background = CoffeeBackgroundLight,
    onBackground = CoffeeOnBackgroundLight,
    surface = CoffeeSurfaceLight,
    onSurface = CoffeeOnSurfaceLight,
    surfaceVariant = CoffeeSurfaceVariantLight,
    onSurfaceVariant = CoffeeOnSurfaceVariantLight,
    outline = CoffeeOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We default dynamicColor to false so the warm artisan coffee aesthetic is consistently preserved
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
