package com.openminis.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// [Hassn] Material3 ColorScheme built the way Hark's own app builds it (APK com.hark.android
// 1.0.0-52, ow6.java) from HarkColors (hassn/HarkTokens.kt): ink primary, no blue accent.
// background / surface stay transparent so the one sky behind the NavHost shows through every
// Scaffold; the containers Material uses for menus, dialogs and sheets are Hark's regularSolid
// (white @96 % / ink @96 %) because those windows cannot blur what is behind them.
private val HInk = Color(0xFF1D1D1F)
private val HPaper = Color(0xFFF5F5F7)
private val HOnInk = Color(0xFFF9F9FA)

private val LightColorScheme = lightColorScheme(
    primary = HInk,
    onPrimary = HOnInk,
    primaryContainer = Color.Black.copy(alpha = 0.05f),
    onPrimaryContainer = HInk,
    inversePrimary = HPaper,
    secondary = HInk,
    onSecondary = HOnInk,
    secondaryContainer = Color.Black.copy(alpha = 0.05f),
    onSecondaryContainer = HInk,
    tertiary = HInk,
    onTertiary = HOnInk,
    tertiaryContainer = Color.Black.copy(alpha = 0.05f),
    onTertiaryContainer = HInk,
    background = Color.Transparent,
    onBackground = HInk,
    surface = Color.Transparent,
    onSurface = HInk,
    surfaceVariant = Color.Black.copy(alpha = 0.05f),
    onSurfaceVariant = HInk.copy(alpha = 0.5f),
    surfaceTint = Color.Transparent,
    inverseSurface = Color.White.copy(alpha = 0.94f),
    inverseOnSurface = HInk.copy(alpha = 0.85f),
    error = Color(0xFFDA2F35),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF5B1313),
    outline = Color.Black.copy(alpha = 0.3f),
    outlineVariant = Color.Black.copy(alpha = 0.1f),
    scrim = Color.Black,
    surfaceBright = Color.White.copy(alpha = 0.96f),
    surfaceDim = HPaper,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White.copy(alpha = 0.96f),
    surfaceContainer = Color.White.copy(alpha = 0.96f),
    surfaceContainerHigh = Color.White.copy(alpha = 0.96f),
    surfaceContainerHighest = Color.White.copy(alpha = 0.6675f),
)

private val DarkColorScheme = darkColorScheme(
    primary = HPaper,
    onPrimary = HInk,
    primaryContainer = Color.White.copy(alpha = 0.1f),
    onPrimaryContainer = HPaper,
    inversePrimary = HInk,
    secondary = HPaper,
    onSecondary = HInk,
    secondaryContainer = Color.White.copy(alpha = 0.1f),
    onSecondaryContainer = HPaper,
    tertiary = HPaper,
    onTertiary = HInk,
    tertiaryContainer = Color.White.copy(alpha = 0.1f),
    onTertiaryContainer = HPaper,
    background = Color.Transparent,
    onBackground = HPaper,
    surface = Color.Transparent,
    onSurface = HPaper,
    surfaceVariant = Color.White.copy(alpha = 0.1f),
    onSurfaceVariant = HPaper.copy(alpha = 0.5f),
    surfaceTint = Color.Transparent,
    inverseSurface = Color.Black.copy(alpha = 0.9f),
    inverseOnSurface = HPaper.copy(alpha = 0.85f),
    error = Color(0xFFF1555B),
    onError = HInk,
    errorContainer = Color(0xFF5B1313),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color.White.copy(alpha = 0.3f),
    outlineVariant = Color.White.copy(alpha = 0.1f),
    scrim = Color.Black,
    surfaceBright = HInk.copy(alpha = 0.96f),
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = HInk.copy(alpha = 0.96f),
    surfaceContainer = HInk.copy(alpha = 0.96f),
    surfaceContainerHigh = HInk.copy(alpha = 0.96f),
    surfaceContainerHighest = HInk.copy(alpha = 0.6175f),
)

// App-wide FAB accent color (warm beige, matching iOS New Chat button).
// Reads from ChatPalette so it follows the in-app theme override (theme_mode pref),
// not android.isSystemInDarkTheme(), which only tracks the system setting.
@Composable
fun minisFabColor(): Color = LocalChatPalette.current.fabAccent

// [Hassn] Hark shape tokens (APK HarkShapeTokenSpec, c07.java): 4 / 8 / 12 / 20 / 24 dp.
// Dialogs and bottom sheets use extraLarge = 24, like Hark's sheets and cards.
private val MinisShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MinisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = scaledTypography(fontScale)
    val chatPalette = if (darkTheme) DarkChatPalette else LightChatPalette

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MinisShapes,
        typography = typography,
    ) {
        CompositionLocalProvider(LocalChatPalette provides chatPalette, content = content)
    }
}

private fun TextStyle.scale(factor: Float): TextStyle =
    if (factor == 1f) this else copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)

/**
 * [Hassn] Hark's Material3 Typography (APK r07.java): sizes / line heights / weights exact.
 * Font = system sans (Hark ships GT Standard, a paid font we do not bundle; Hark's own web
 * fallback is Helvetica/Arial-class sans). Body line heights are 20 sp instead of 18 because
 * CJK system fonts have a taller ascent (the spec's own CJK recommendation).
 */
private fun harkStyle(size: Int, line: Int, weight: FontWeight, spacingEm: Float = 0f) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = if (spacingEm == 0f) 0.sp else spacingEm.em,
)

private fun scaledTypography(factor: Float): Typography {
    val w4 = FontWeight.Normal
    val w5 = FontWeight.Medium
    return Typography(
        displayLarge = harkStyle(48, 58, w4).scale(factor),
        displayMedium = harkStyle(32, 38, w4, -0.02f).scale(factor),
        displaySmall = harkStyle(28, 34, w4).scale(factor),
        headlineLarge = harkStyle(24, 29, w4).scale(factor),
        headlineMedium = harkStyle(20, 24, w4).scale(factor),
        headlineSmall = harkStyle(20, 24, w5).scale(factor),
        titleLarge = harkStyle(20, 24, w5).scale(factor),
        titleMedium = harkStyle(15, 18, w5).scale(factor),
        titleSmall = harkStyle(13, 16, w5).scale(factor),
        bodyLarge = harkStyle(15, 20, w4).scale(factor),
        bodyMedium = harkStyle(14, 20, w4).scale(factor),
        bodySmall = harkStyle(13, 16, w4).scale(factor),
        labelLarge = harkStyle(15, 18, w5).scale(factor),
        labelMedium = harkStyle(13, 16, w5).scale(factor),
        labelSmall = harkStyle(12, 14, w5).scale(factor),
    )
}

/**
 * Obsidian Canvas Design System Tokens.
 * Grounded in ui-craft quantitative research of 2,600 mobile screens.
 */
object ObsidianTokens {
    // Surfaces & Elevation (Glacial Clarity)
    val CanvasBase = Color(0xFF0A0E17)
    val SurfaceLevel1 = Color(0xFF111827)
    val SurfaceLevel2 = Color(0xFF1E293B)
    val HairlineBorder = Color(0x2638BDF8)
    val HairlineBorderLight = Color(0xFFE2E8F0)

    // Text & Content High Contrast
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFFCBD5E1)
    val TextTertiary = Color(0xFF94A3B8)

    // Signals & Accents
    val CobaltSignal = Color(0xFF1D1D1F)
    val TerminalGreen = Color(0xFF00A665)
    val AmberSignal = Color(0xFFFF4D00)
    val ErrorSignal = Color(0xFFDA2F35)

    // Layout & Geometry
    val ScreenGutter = 16.dp
    val Gutter = ScreenGutter
    val HeaderHeight = 44.dp
    val SectionTitleToContent = 12.dp
    val SectionGroupLabelToContent = 8.dp
    val SectionToSectionGap = 24.dp
    val RowDenseHeight = 44.dp
    val RowStandardHeight = 56.dp
    val CardCornerRadius = 24.dp
    val CardCornerRadiusSmall = 12.dp
    val SheetCornerRadius = 24.dp
    val GrabberWidth = 36.dp
    val GrabberHeight = 4.dp
    val ChipHeight = 30.dp
}

/**
 * ui-craft standard spacing, sizing, and typography tokens.
 * Mapped to ObsidianTokens for clean backwards compatibility.
 */
object UiCraftTokens {
    val ScreenGutter = ObsidianTokens.ScreenGutter
    val Gutter = ObsidianTokens.Gutter
    val HeaderHeight = ObsidianTokens.HeaderHeight
    val SectionTitleToContent = ObsidianTokens.SectionTitleToContent
    val SectionGroupLabelToContent = ObsidianTokens.SectionGroupLabelToContent
    val SectionToSectionGap = ObsidianTokens.SectionToSectionGap
    val RowDenseHeight = ObsidianTokens.RowDenseHeight
    val RowStandardHeight = ObsidianTokens.RowStandardHeight
    val CardCornerRadius = ObsidianTokens.CardCornerRadius
    val CardCornerRadiusSmall = ObsidianTokens.CardCornerRadiusSmall
    val SheetCornerRadius = ObsidianTokens.SheetCornerRadius
    val GrabberWidth = ObsidianTokens.GrabberWidth
    val GrabberHeight = ObsidianTokens.GrabberHeight
    val ChipHeight = ObsidianTokens.ChipHeight
}

/**
 * Tabular figures (tnum) extension for typography.
 * Prevents jitter/jumping when numbers (token count, elapsed time, tok/s) refresh rapidly.
 */
fun TextStyle.withTabularNumbers(): TextStyle = copy(
    fontFeatureSettings = "tnum"
)

/**
 * ui-craft Rule 15: Brand Atmosphere Glow.
 * A gentle vertical brand gradient (~380dp) under the top status bar.
 * Gives identity and depth without painting surfaces heavily.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun BrandAtmosphereGlow(
    modifier: Modifier = Modifier,
    height: Dp = 380.dp,
    glowColor: Color = MaterialTheme.colorScheme.primary,
) {
    // [Hassn] Hark has no brand glow; the sky behind the app is the atmosphere. Draws nothing.
}
