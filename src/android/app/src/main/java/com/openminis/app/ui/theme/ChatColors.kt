package com.openminis.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic chat colors mirroring iOS ChatColors (AIChatView.swift).
// Resolved from LocalChatPalette, which is provided by MinisTheme.
//
// iOS reference:
//   systemBackground        -> background
//   secondarySystemBackground -> secondaryBg
//   tertiarySystemFill      -> userBubble
//   tertiarySystemGroupedBackground -> toolBg
//   label                   -> primaryText
//   secondaryLabel          -> secondaryText
//   tertiaryLabel           -> tertiaryText
//   quaternaryLabel         -> sendButtonDisabled
//   separator               -> border
//   systemGray6             -> inlineCodeBg / toolCapsuleBg
@Immutable
data class ChatPalette(
    val isDark: Boolean,
    val background: Color,
    val secondaryBg: Color,
    val inputBg: Color,
    val inputIconBg: Color,
    val inputIconBorder: Color,
    val inputBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val disabledText: Color,
    val userBubble: Color,
    val toolBg: Color,
    val toolBorder: Color,
    val toolCapsuleBg: Color,
    val separator: Color,
    val sendButton: Color,
    val sendButtonDisabled: Color,
    val codeBlockBg: Color,
    val codeBlockText: Color,
    val inlineCodeBg: Color,
    val inlineCodeText: Color,
    val link: Color,
    val blockquoteBar: Color,
    val thinking: Color,
    val warningBg: Color,
    val warningText: Color,
    val tableBorder: Color,
    val inputShadow: Color,
    val toastBg: Color,
    val thumbnailBorder: Color,
    val sheetHeaderBg: Color,
    val sheetHeaderBorder: Color,
    val fabAccent: Color,
)

// [Hassn] Chat palette from HarkColors (APK com.hark.android 1.0.0-52, qw6.java). `background` /
// `secondaryBg` stay opaque (Hark paper / white, ink in dark) because bottom sheets use them as
// their container; the chat screen itself paints transparent over the sky.
private val PInk = Color(0xFF1D1D1F)
private val PPaper = Color(0xFFF5F5F7)

val LightChatPalette = ChatPalette(
    isDark = false,
    background = PPaper,
    secondaryBg = Color.White,
    inputBg = Color.White.copy(alpha = 0.55f),        // composerBg
    inputIconBg = PInk.copy(alpha = 0.05f),           // fillTertiary
    inputIconBorder = Color.Transparent,
    inputBorder = PPaper.copy(alpha = 0.5f),          // strokeComposer
    primaryText = PInk,
    secondaryText = PInk.copy(alpha = 0.5f),
    tertiaryText = PInk.copy(alpha = 0.3f),
    disabledText = PInk.copy(alpha = 0.3f),
    userBubble = Color.White.copy(alpha = 0.3f),      // chatBubbleUserSurface
    toolBg = Color.White.copy(alpha = 0.6675f),       // regularSurface
    toolBorder = PInk.copy(alpha = 0.07f),            // strokeSecondary
    toolCapsuleBg = Color.White.copy(alpha = 0.55f),  // thinBg
    separator = PInk.copy(alpha = 0.07f),
    sendButton = PInk,
    sendButtonDisabled = PInk.copy(alpha = 0.3f),
    codeBlockBg = PInk.copy(alpha = 0.05f),
    codeBlockText = PInk,
    inlineCodeBg = PInk.copy(alpha = 0.07f),
    inlineCodeText = PInk,
    link = PInk.copy(alpha = 0.5f),                   // Hark link
    blockquoteBar = PInk.copy(alpha = 0.14f),
    thinking = PInk.copy(alpha = 0.5f),
    warningBg = Color(0xFFFF4D00).copy(alpha = 0.145f),
    warningText = Color(0xFFFF4D00),
    tableBorder = PInk.copy(alpha = 0.14f),
    inputShadow = Color.Transparent,
    toastBg = Color.White.copy(alpha = 0.9f),
    thumbnailBorder = PInk.copy(alpha = 0.14f),
    sheetHeaderBg = PPaper,
    sheetHeaderBorder = PInk.copy(alpha = 0.07f),
    fabAccent = PInk,
)

val DarkChatPalette = ChatPalette(
    isDark = true,
    background = PInk,
    secondaryBg = Color(0xFF252527),
    inputBg = PInk.copy(alpha = 0.618f),
    inputIconBg = PPaper.copy(alpha = 0.05f),
    inputIconBorder = Color.Transparent,
    inputBorder = PInk.copy(alpha = 0.5f),
    primaryText = PPaper,
    secondaryText = PPaper.copy(alpha = 0.5f),
    tertiaryText = PPaper.copy(alpha = 0.3f),
    disabledText = PPaper.copy(alpha = 0.3f),
    userBubble = PInk.copy(alpha = 0.3f),
    toolBg = PInk.copy(alpha = 0.6175f),
    toolBorder = PPaper.copy(alpha = 0.07f),
    toolCapsuleBg = PInk.copy(alpha = 0.615f),
    separator = PPaper.copy(alpha = 0.07f),
    sendButton = PPaper,
    sendButtonDisabled = PPaper.copy(alpha = 0.3f),
    codeBlockBg = PPaper.copy(alpha = 0.05f),
    codeBlockText = PPaper,
    inlineCodeBg = PPaper.copy(alpha = 0.08f),
    inlineCodeText = PPaper,
    link = PPaper.copy(alpha = 0.5f),
    blockquoteBar = PPaper.copy(alpha = 0.14f),
    thinking = PPaper.copy(alpha = 0.5f),
    warningBg = Color(0xFFFF4D00).copy(alpha = 0.145f),
    warningText = Color(0xFFFF4D00),
    tableBorder = PPaper.copy(alpha = 0.14f),
    inputShadow = Color.Transparent,
    toastBg = Color.Black.copy(alpha = 0.9f),
    thumbnailBorder = PPaper.copy(alpha = 0.14f),
    sheetHeaderBg = PInk,
    sheetHeaderBorder = PPaper.copy(alpha = 0.07f),
    fabAccent = PPaper,
)

val LocalChatPalette = compositionLocalOf { LightChatPalette }

// Short accessor: ChatColors.primaryText instead of LocalChatPalette.current.primaryText
val ChatColors: ChatPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalChatPalette.current
