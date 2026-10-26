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

// [Hassn-P1] Hark light glass chat palette (approved v2 mockup 02). `background` stays opaque
// because sheets use it as their container; the chat screen itself paints transparent over the sky.
val LightChatPalette = ChatPalette(
    isDark = false,
    background = Color(0xFFEEF1F6),
    secondaryBg = Color.White,
    inputBg = Color(0xCCECEFF5),
    inputIconBg = Color(0x0F14141E),
    inputIconBorder = Color.Transparent,
    inputBorder = Color(0x1F14141E),
    primaryText = Color(0xFF141416),
    secondaryText = Color(0xFF5A5E66),
    tertiaryText = Color(0xFF7A7E86),
    disabledText = Color(0xFFA3A7AE),
    userBubble = Color(0xB8CED8E8),
    toolBg = Color(0xD1FFFFFF),
    toolBorder = Color(0x1414141E),
    toolCapsuleBg = Color(0x9EFFFFFF),
    separator = Color(0x1314141E),
    sendButton = Color(0xFF18181A),
    sendButtonDisabled = Color(0xFFC9CDD4),
    codeBlockBg = Color(0xFFF6F7F9),
    codeBlockText = Color(0xFF24272D),
    inlineCodeBg = Color(0x1114141E),
    inlineCodeText = Color(0xFF24272D),
    link = Color(0xFF2C66D8),
    blockquoteBar = Color(0xFFA3A7AE),
    thinking = Color(0xFF5A5E66),
    warningBg = Color(0xFFFEF3C7),
    warningText = Color(0xFF92400E),
    tableBorder = Color(0x1F14141E),
    inputShadow = Color(0x14283C5A),
    toastBg = Color(0x1A18181A),
    thumbnailBorder = Color(0x1F14141E),
    sheetHeaderBg = Color(0xFFEEF1F6),
    sheetHeaderBorder = Color(0x1314141E),
    fabAccent = Color(0xFF18181A),
)

// [Hassn-P1] Dark variant of the same glass skin.
val DarkChatPalette = ChatPalette(
    isDark = true,
    background = Color(0xFF14161B),
    secondaryBg = Color(0xFF1C1E23),
    inputBg = Color(0xCC262A33),
    inputIconBg = Color(0x1AFFFFFF),
    inputIconBorder = Color.Transparent,
    inputBorder = Color(0x24FFFFFF),
    primaryText = Color(0xFFF4F5F7),
    secondaryText = Color(0xFFC4C7CE),
    tertiaryText = Color(0xFFA9ADB5),
    disabledText = Color(0xFF5A5E66),
    userBubble = Color(0xB83A4252),
    toolBg = Color(0xD11C1E24),
    toolBorder = Color(0x1AFFFFFF),
    toolCapsuleBg = Color(0x26FFFFFF),
    separator = Color(0x1AFFFFFF),
    sendButton = Color(0xFFF4F5F7),
    sendButtonDisabled = Color(0xFF3A3E47),
    codeBlockBg = Color(0xFF0F1114),
    codeBlockText = Color(0xFFE6E8EC),
    inlineCodeBg = Color(0x1FFFFFFF),
    inlineCodeText = Color(0xFFE6E8EC),
    link = Color(0xFF7FA8F0),
    blockquoteBar = Color(0xFF5A5E66),
    thinking = Color(0xFFA9ADB5),
    warningBg = Color(0x1EF59E0B),
    warningText = Color(0xFFFDE68A),
    tableBorder = Color(0x24FFFFFF),
    inputShadow = Color(0x40000000),
    toastBg = Color(0x2EFFFFFF),
    thumbnailBorder = Color(0x24FFFFFF),
    sheetHeaderBg = Color(0xFF1C1E23),
    sheetHeaderBorder = Color(0x1AFFFFFF),
    fabAccent = Color(0xFFF4F5F7),
)

val LocalChatPalette = compositionLocalOf { LightChatPalette }

// Short accessor: ChatColors.primaryText instead of LocalChatPalette.current.primaryText
val ChatColors: ChatPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalChatPalette.current
