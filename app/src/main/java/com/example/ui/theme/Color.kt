package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Artisan Coffee Palette based on user's color swatch:
// Stripe 1: Deep Burgundy / Coffee Cherry Maroon (#78141F)
// Stripe 2: Warm Sand / Oat Cream (#E5D0AC)
// Stripe 3: Soft Ivory / Warm Milk White (#FAF6EE)
// Stripe 4: Gentle Slate / Pastel Water Blue (#9DB6C5)
// =========================================================================

// Signature Swatch Colors
val PaletteCherryMaroon = Color(0xFF78141F)
val PaletteWarmSand = Color(0xFFE5D0AC)
val PaletteIvoryWhite = Color(0xFFFAF6EE)
val PaletteSlateWaterBlue = Color(0xFF9DB6C5)

// Complementary Functional Tones
val PaletteDeepCherry = Color(0xFF5A0B13)
val PaletteBrightCherry = Color(0xFF921E2C)
val PaletteDeepSlateBlue = Color(0xFF3E5C6E)
val PaletteLightSlateBlueContainer = Color(0xFFD6E4ED)
val PaletteSandOatContainer = Color(0xFFEFE2CA)
val PaletteSandOatDarker = Color(0xFFD8C197)

// Light Theme (Clean, Warm, Bright Scandinavian Coffee Aesthetic)
val CoffeePrimaryLight = PaletteCherryMaroon
val CoffeeOnPrimaryLight = Color(0xFFFFFFFF)
val CoffeePrimaryContainerLight = PaletteSandOatContainer
val CoffeeOnPrimaryContainerLight = PaletteDeepCherry

val CoffeeSecondaryLight = PaletteDeepSlateBlue
val CoffeeOnSecondaryLight = Color(0xFFFFFFFF)
val CoffeeSecondaryContainerLight = PaletteLightSlateBlueContainer
val CoffeeOnSecondaryContainerLight = Color(0xFF162D3B)

val CoffeeTertiaryLight = Color(0xFFB56A1E) // Warm Honey Caramel
val CoffeeOnTertiaryLight = Color(0xFFFFFFFF)
val CoffeeTertiaryContainerLight = Color(0xFFFDECD4)
val CoffeeOnTertiaryContainerLight = Color(0xFF3B1E00)

val CoffeeBackgroundLight = PaletteIvoryWhite
val CoffeeOnBackgroundLight = Color(0xFF261D1A)
val CoffeeSurfaceLight = Color(0xFFFFFDF9)
val CoffeeOnSurfaceLight = Color(0xFF261D1A)
val CoffeeSurfaceVariantLight = Color(0xFFF1E6D4)
val CoffeeOnSurfaceVariantLight = Color(0xFF55483F)
val CoffeeOutlineLight = Color(0xFFC7B7A4)

// Dark Theme (Deep Roasted Cherry & Slate with Cream Accents)
val CoffeePrimaryDark = Color(0xFFF08C94)
val CoffeeOnPrimaryDark = Color(0xFF4C020B)
val CoffeePrimaryContainerDark = PaletteDeepCherry
val CoffeeOnPrimaryContainerDark = PaletteSandOatContainer

val CoffeeSecondaryDark = PaletteSlateWaterBlue
val CoffeeOnSecondaryDark = Color(0xFF102735)
val CoffeeSecondaryContainerDark = Color(0xFF2B4454)
val CoffeeOnSecondaryContainerDark = PaletteLightSlateBlueContainer

val CoffeeTertiaryDark = Color(0xFFECC085)
val CoffeeOnTertiaryDark = Color(0xFF462700)
val CoffeeTertiaryContainerDark = Color(0xFF643D05)
val CoffeeOnTertiaryContainerDark = Color(0xFFFDECD4)

val CoffeeBackgroundDark = Color(0xFF1E1716)
val CoffeeOnBackgroundDark = PaletteIvoryWhite
val CoffeeSurfaceDark = Color(0xFF271F1E)
val CoffeeOnSurfaceDark = PaletteIvoryWhite
val CoffeeSurfaceVariantDark = Color(0xFF3A2E2C)
val CoffeeOnSurfaceVariantDark = PaletteWarmSand
val CoffeeOutlineDark = Color(0xFF6F5E59)

// Stage Specific Accents directly matching the palette:
val StageBloomColor = Color(0xFFC97318) // Warm blooming honey/amber
val StagePourColor = PaletteCherryMaroon // Top Stripe: Signature Coffee Cherry Burgundy
val StageDrawdownColor = PaletteDeepSlateBlue // Bottom Stripe: Slate Blue
val StageFinishedColor = Color(0xFF3B6E47) // Forest Sage
