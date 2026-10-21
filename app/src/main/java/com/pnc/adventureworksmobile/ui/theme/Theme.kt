package com.pnc.adventureworksmobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AdventureDarkColorScheme = darkColorScheme(
    primary = AdventureGreenDarkTheme,
    onPrimary = AdventureDarkBackground,
    primaryContainer = AdventureGreenDarkContainer,
    onPrimaryContainer = AdventureGreenDarkTheme,
    secondary = AdventureOrangeDarkTheme,
    onSecondary = AdventureDarkBackground,
    secondaryContainer = AdventureOrangeDarkContainer,
    onSecondaryContainer = AdventureOrangeDarkTheme,
    background = AdventureDarkBackground,
    surface = AdventureDarkSurface,
    onBackground = AdventureDarkInk,
    onSurface = AdventureDarkInk,
    onSurfaceVariant = AdventureDarkMuted,
    outline = AdventureDarkOutline,
)

private val AdventureLightColorScheme = lightColorScheme(
    primary = AdventureGreen,
    onPrimary = AdventureSurface,
    primaryContainer = AdventureGreenContainer,
    onPrimaryContainer = AdventureInk,
    secondary = AdventureOrange,
    onSecondary = AdventureSurface,
    secondaryContainer = AdventureOrangeContainer,
    onSecondaryContainer = AdventureInk,
    background = AdventureGroupedBackground,
    surface = AdventureSurface,
    onBackground = AdventureInk,
    onSurface = AdventureInk,
    onSurfaceVariant = AdventureMuted,
    outline = AdventureOutline,
)

private val ModernPlainDarkColorScheme = darkColorScheme(
    primary = ModernPlainDarkPrimary,
    onPrimary = ModernPlainDarkBackground,
    primaryContainer = ModernPlainDarkPrimaryContainer,
    onPrimaryContainer = ModernPlainDarkInk,
    secondary = ModernPlainDarkSecondary,
    onSecondary = ModernPlainDarkBackground,
    secondaryContainer = ModernPlainDarkSecondaryContainer,
    onSecondaryContainer = ModernPlainDarkInk,
    background = ModernPlainDarkBackground,
    surface = ModernPlainDarkSurface,
    onBackground = ModernPlainDarkInk,
    onSurface = ModernPlainDarkInk,
    onSurfaceVariant = ModernPlainDarkMuted,
    outline = ModernPlainDarkOutline,
)

private val ModernPlainLightColorScheme = lightColorScheme(
    primary = ModernPlainPrimary,
    onPrimary = ModernPlainSurface,
    primaryContainer = ModernPlainPrimaryContainer,
    onPrimaryContainer = ModernPlainInk,
    secondary = ModernPlainSecondary,
    onSecondary = ModernPlainSurface,
    secondaryContainer = ModernPlainSecondaryContainer,
    onSecondaryContainer = ModernPlainInk,
    background = ModernPlainBackground,
    surface = ModernPlainSurface,
    onBackground = ModernPlainInk,
    onSurface = ModernPlainInk,
    onSurfaceVariant = ModernPlainMuted,
    outline = ModernPlainOutline,
)

// Swap palettes by commenting one pair and uncommenting the other.
// private val ActiveDarkColorScheme = AdventureDarkColorScheme
// private val ActiveLightColorScheme = AdventureLightColorScheme
private val ActiveDarkColorScheme = ModernPlainDarkColorScheme
private val ActiveLightColorScheme = ModernPlainLightColorScheme

@Composable
fun AdventureWorksMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> ActiveDarkColorScheme
        else -> ActiveLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
