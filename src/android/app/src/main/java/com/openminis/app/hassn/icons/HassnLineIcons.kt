package com.openminis.app.hassn.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

/**
 * [Hassn] Our own line icon set, drawn in the style of Hark's icons (thin even stroke, round caps
 * and joins, open geometry) — NOT Hark's artwork. 24-unit grid; [LINE] = 2 units, so a 24 dp icon
 * has a 2 dp line and a 16 dp row icon (drawn through HassnIcon) keeps a 2 dp line too.
 *
 * Each entry is "stroke path" or "stroke path|fill path" (the part after | is filled solid,
 * e.g. a play triangle or a stop square).
 */
object HassnLineIcons {
    const val LINE = 2f

    val paths: Map<String, String> = mapOf(
        // navigation / chrome
        "menu" to "M4.5 7h15M4.5 12h10M4.5 17h6.5",
        "back" to "M14.5 5.5L8 12l6.5 6.5",
        "forward" to "M9.5 5.5L16 12l-6.5 6.5",
        "chev" to "M9.5 6.5l5.5 5.5-5.5 5.5",
        "chevLeft" to "M14.5 6.5L9 12l5.5 5.5",
        "down" to "M6.5 9.5l5.5 5.5 5.5-5.5",
        "up" to "M6.5 14.5L12 9l5.5 5.5",
        "dropDown" to "M8 10.5l4 4 4-4",
        "unfold" to "M8 9l4-4 4 4M8 15l4 4 4-4",
        "arrowUp" to "M12 19V5.5M6.5 11L12 5.5 17.5 11",
        "arrowDown" to "M12 5v13.5M6.5 13L12 18.5 17.5 13",
        "arrow" to "M5 12h13.5M13 6.5l5.5 5.5-5.5 5.5",
        "arrowLeft" to "M19 12H5.5M11 6.5L5.5 12l5.5 5.5",
        "close" to "M6.5 6.5l11 11M17.5 6.5l-11 11",
        "plus" to "M12 5v14M5 12h14",
        "minus" to "M5 12h14",
        "more" to "M6 12h.01M12 12h.01M18 12h.01",
        "moreVert" to "M12 6h.01M12 12h.01M12 18h.01",
        "drag" to "M6 9.5h12M6 14.5h12",
        "search" to "M4.5 11a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0 -13 0zM16 16l4 4",
        "check" to "M5 12.5l4.5 4.5L19 7.5",
        "checkCircle" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM8.3 12.3l2.6 2.6 5-5.2",
        "circle" to "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0z",
        "disc" to "|M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0z",
        "dot" to "|M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0z",
        "radioOn" to "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0z|M8.5 12a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0z",
        "cancel" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM9.2 9.2l5.6 5.6M14.8 9.2l-5.6 5.6",
        "info" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM12 11v5.5M12 7.8v.4",
        "alert" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM12 7.5V13M12 16.2v.3",
        "warning" to "M12 4.2l8.6 15H3.4zM12 10v4M12 16.8v.2",
        "external" to "M13.5 4.5h6v6M19.5 4.5l-8 8M17.5 14v4a1.5 1.5 0 0 1-1.5 1.5H6A1.5 1.5 0 0 1 4.5 18V8A1.5 1.5 0 0 1 6 6.5h4",
        "fullscreen" to "M4.5 9V4.5H9M15 4.5h4.5V9M19.5 15v4.5H15M9 19.5H4.5V15",
        "fullscreenExit" to "M9 4.5V9H4.5M19.5 9H15V4.5M15 19.5V15h4.5M4.5 15H9v4.5",
        // actions
        "trash" to "M5 7h14M10 7V5h4v2M7 7l1 12h8l1-12M10.5 10.5v6M13.5 10.5v6",
        "edit" to "M4.5 19.5l4-1 10-10-3-3-10 10zM13.5 7.5l3 3",
        "editNote" to "M4.5 7h9M4.5 12h6M4.5 17h4M12 19.5l1-3 6-6 2 2-6 6z",
        "copy" to "M9 8.5h9a1.5 1.5 0 0 1 1.5 1.5v9a1.5 1.5 0 0 1-1.5 1.5H9A1.5 1.5 0 0 1 7.5 19v-9A1.5 1.5 0 0 1 9 8.5zM16.5 5.5V5A1.5 1.5 0 0 0 15 3.5H6A1.5 1.5 0 0 0 4.5 5v9A1.5 1.5 0 0 0 6 15.5h.5",
        "share" to "M12 14.5V4M7.5 8L12 3.5 16.5 8M7 11H6a1.5 1.5 0 0 0-1.5 1.5v6A1.5 1.5 0 0 0 6 20h12a1.5 1.5 0 0 0 1.5-1.5v-6A1.5 1.5 0 0 0 18 11h-1",
        "shareNodes" to "M4.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM14.5 6a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM14.5 18a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM9.2 10.8l5.6-3.3M9.2 13.2l5.6 3.3",
        "download" to "M12 4v11M7 10.5l5 5 5-5M5 19.5h14",
        "upload" to "M12 15.5V4.5M7 9l5-5 5 5M5 19.5h14",
        "refresh" to "M19 12a7 7 0 1 1-2-4.9M19 4.5V8h-3.5",
        "sync" to "M5 11a7 7 0 0 1 12.2-4.2M19 4.5V8h-3.5M19 13a7 7 0 0 1-12.2 4.2M5 19.5V16h3.5",
        "undo" to "M9 8.5H15a4.5 4.5 0 0 1 0 9H8M9 5L5.5 8.5 9 12",
        "history" to "M4.5 12a7.5 7.5 0 1 0 2.2-5.3M4.5 4.5v3.2h3.2M12 8.5V12l2.5 2",
        "restore" to "M4.5 12a7.5 7.5 0 1 0 2.2-5.3M4.5 4.5v3.2h3.2M12 8.5V12l2.5 2",
        "play" to "M8 5.5v13l10-6.5z|M8 5.5v13l10-6.5z",
        "playCircle" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM10 8.8v6.4l5-3.2z",
        "pause" to "M9 6v12M15 6v12",
        "stop" to "M7.5 7.5h9v9h-9z|M7.5 7.5h9v9h-9z",
        "stopCircle" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM9.5 9.5h5v5h-5z",
        "skipNext" to "M6.5 6.5l8 5.5-8 5.5zM17.5 6.5v11",
        "skipPrev" to "M17.5 6.5l-8 5.5 8 5.5zM6.5 6.5v11",
        "send" to "M12 19V5.5M6.5 11L12 5.5 17.5 11",
        "pin" to "M9 4.5h6M10 4.5v5L7 13h10l-3-3.5v-5M12 13v6.5",
        "sort" to "M5 7h14M7.5 12h9M10 17h4",
        "swapVert" to "M8 19V5M4.5 8.5L8 5l3.5 3.5M16 5v14M12.5 15.5L16 19l3.5-3.5",
        "swapHoriz" to "M5 8h14M15.5 4.5L19 8l-3.5 3.5M19 16H5M8.5 12.5L5 16l3.5 3.5",
        "compress" to "M5 9.5h14M5 14.5h14M9 4.5l3 3 3-3M9 19.5l3-3 3 3",
        "eject" to "M6 14.5h12L12 7zM6 18.5h12",
        "print" to "M7 9V4.5h10V9M7 16.5H5.5A1.5 1.5 0 0 1 4 15v-4.5A1.5 1.5 0 0 1 5.5 9h13a1.5 1.5 0 0 1 1.5 1.5V15a1.5 1.5 0 0 1-1.5 1.5H17M7 13.5h10v6H7z",
        // objects
        "folder" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2z",
        "folderOpen" to "M3.5 17.5v-10a2 2 0 012-2h4l2 2.2h6a2 2 0 012 2v1M3.5 17.5l2.3-6a1.5 1.5 0 011.4-1h12.6a1 1 0 01.9 1.3l-2 5.8a2 2 0 01-1.9 1.4H5.5a2 2 0 01-2-1.5z",
        "folderAdd" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2zM12 11v5M9.5 13.5h5",
        "folderOff" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2zM4 4l16 16",
        "folderZip" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2zM13 10.5v1.5M13 14v1.5",
        "folderShared" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2zM11 12.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM9.5 17a3 3 0 016 0",
        "file" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18",
        "fileText" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18M9.5 12.5h5M9.5 16h5",
        "fileAdd" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18M12.5 11.5v6M9.5 14.5h6",
        "fileDown" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18M12.5 11v6M10 14.5l2.5 2.5 2.5-2.5",
        "article" to "M5.5 4.5h13a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1v-13a1 1 0 0 1 1-1zM8 9h8M8 12h8M8 15h5",
        "book" to "M5 5.5A1.5 1.5 0 0 1 6.5 4H18v13H6.5A1.5 1.5 0 0 0 5 18.5zM5 18.5A1.5 1.5 0 0 0 6.5 20H18v-3",
        "checklist" to "M4.5 7l1.5 1.5 3-3M4.5 15l1.5 1.5 3-3M12 7.5h7.5M12 15.5h7.5",
        "image" to "M5.5 4.5h13a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18V6a1.5 1.5 0 0 1 1.5-1.5zM4 16l4.5-4.5 4 4 2.5-2.5 5 5M14.5 9a1 1 0 1 0 2 0a1 1 0 1 0 -2 0z",
        "images" to "M8 3.5h11a1.5 1.5 0 0 1 1.5 1.5v11M5.5 7h11A1.5 1.5 0 0 1 18 8.5v10a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 4 18.5v-10A1.5 1.5 0 0 1 5.5 7zM4 17l4-4 3.5 3.5 2-2 4.5 4.5",
        "imageAdd" to "M13 4.5H5.5A1.5 1.5 0 0 0 4 6v12a1.5 1.5 0 0 0 1.5 1.5h12A1.5 1.5 0 0 0 19 18v-7M4 16l4.5-4.5 4 4 2.5-2.5 4 4M18 3v6M15 6h6",
        "brokenImage" to "M5.5 4.5h13a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18V6a1.5 1.5 0 0 1 1.5-1.5zM4 13l4-3 4 3 4-3 4 3",
        "video" to "M4.5 6.5h10a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1-1.5 1.5h-10A1.5 1.5 0 0 1 3 16V8a1.5 1.5 0 0 1 1.5-1.5zM16 10.5l5-3v9l-5-3",
        "camera" to "M4.5 8h3l1.5-2.5h6L16.5 8h3A1.5 1.5 0 0 1 21 9.5v8a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 17.5v-8A1.5 1.5 0 0 1 4.5 8zM9 13a3 3 0 1 0 6 0a3 3 0 1 0 -6 0z",
        "music" to "M9 17.5V6l10-2v11.5M9 17.5a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0zM19 15.5a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0z",
        "pdf" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18M9.5 16.5v-4h1.5a1.2 1.2 0 0 1 0 2.4H9.5",
        "card" to "M5.5 6h13a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-13a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2zM3.5 10h17",
        "mail" to "M5 6h14a1.5 1.5 0 0 1 1.5 1.5v9A1.5 1.5 0 0 1 19 18H5a1.5 1.5 0 0 1-1.5-1.5v-9A1.5 1.5 0 0 1 5 6zM4 7l8 6 8-6",
        "at" to "M15.5 12a3.5 3.5 0 1 1-7 0a3.5 3.5 0 1 1 7 0zM15.5 12v1.3a2.2 2.2 0 0 0 4.4 0V12a7.9 7.9 0 1 0-3.2 6.3",
        "calendar" to "M5.5 5.5h13A1.5 1.5 0 0 1 20 7v11.5a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5V7a1.5 1.5 0 0 1 1.5-1.5zM4 10h16M8.5 3.5v4M15.5 3.5v4",
        "clock" to "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0zM12 8v4.5l3 2",
        "bell" to "M6.5 16.5V11a5.5 5.5 0 0 1 11 0v5.5l1.5 1.5H5zM10 20h4",
        "lock" to "M6.5 10.5h11a1.5 1.5 0 0 1 1.5 1.5v6.5a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5V12a1.5 1.5 0 0 1 1.5-1.5zM8.5 10.5V8a3.5 3.5 0 0 1 7 0v2.5",
        "lockOpen" to "M6.5 10.5h11a1.5 1.5 0 0 1 1.5 1.5v6.5a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5V12a1.5 1.5 0 0 1 1.5-1.5zM8.5 10.5V8a3.5 3.5 0 0 1 6.8-1.2",
        "key" to "M4.2 15a3.8 3.8 0 1 0 7.6 0a3.8 3.8 0 1 0 -7.6 0zM10.8 12.2L19 4M15.5 7.5l2.5 2.5",
        "fingerprint" to "M7 18.5c1-2 1.5-4.2 1.5-6.5a3.5 3.5 0 0 1 7 0c0 1.8-.2 3.6-.7 5.3M12 12c0 3-.6 5.8-1.8 8.2M5 15a14 14 0 0 0 .5-3 6.5 6.5 0 0 1 13 0c0 1.2-.1 2.3-.3 3.5",
        "shield" to "M12 3.5l7 2.8v5.4c0 4.4-3 7.4-7 8.8-4-1.4-7-4.4-7-8.8V6.3z",
        "eye" to "M2.8 12S6 5.8 12 5.8 21.2 12 21.2 12 18 18.2 12 18.2 2.8 12 2.8 12zM9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z",
        "eyeOff" to "M10 6.1c.6-.2 1.3-.3 2-.3 6 0 9.2 6.2 9.2 6.2a16 16 0 0 1-2.3 3.1M6.6 7.6C4.2 9.2 2.8 12 2.8 12S6 18.2 12 18.2c1.6 0 3-.4 4.2-1.1M10.2 10.3a2.5 2.5 0 0 0 3.5 3.5M4 4l16 16",
        "link" to "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1",
        "linkOff" to "M13 6.3l1-1A4 4 0 0 1 19.7 11l-1.3 1.3M11 17.7l-1 1A4 4 0 0 1 4.3 13l1.3-1.3M4 4l16 16",
        "globe" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM3.5 12h17M12 3.5c3 3 3 14 0 17M12 3.5c-3 3-3 14 0 17",
        "browser" to "M5 4.5h14A1.5 1.5 0 0 1 20.5 6v12a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 18V6A1.5 1.5 0 0 1 5 4.5zM3.5 9h17M6.5 6.8h.01M9 6.8h.01",
        "translate" to "M4 5.5h9M8.5 4v1.5c0 3.5-2 6.5-4.5 8M6.5 9.5c1 2 2.8 3.5 5 4.5M12.5 20l3.5-8.5 3.5 8.5M13.7 17.2h4.6",
        "map" to "M3.5 6.5L9 4.5l6 2 5.5-2v13l-5.5 2-6-2-5.5 2zM9 4.5v13M15 6.5v13",
        "terrain" to "M3 18.5l6-9 4 6 2.5-3.5 5.5 6.5z",
        "home" to "M4 10.5L12 4l8 6.5V19a1 1 0 0 1-1 1h-4.5v-5.5h-5V20H5a1 1 0 0 1-1-1z",
        "user" to "M8.2 8.5a3.8 3.8 0 1 0 7.6 0a3.8 3.8 0 1 0 -7.6 0zM4.5 20a7.5 7.5 0 0 1 15 0",
        "users" to "M5.7 9a3.3 3.3 0 1 0 6.6 0a3.3 3.3 0 1 0 -6.6 0zM3 19a6 6 0 0 1 12 0M14.4 8a2.6 2.6 0 1 0 5.2 0a2.6 2.6 0 1 0 -5.2 0zM16 13.5a5 5 0 0 1 5.5 5",
        "voice" to "M9 8.5a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0zM5.5 20a7 7 0 0 1 12 0M19 6a5 5 0 0 1 0 6M3.5 7.5a3 3 0 0 0 0 3",
        "card2" to "M5.5 6h13a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zM7 14.5h3",
        "chat" to "M12 4.5c4.7 0 8 2.9 8 6.6s-3.3 6.6-8 6.6c-1 0-1.9-.1-2.7-.4L5 19l1.1-3.3C4.8 14.5 4 13 4 11.1 4 7.4 7.3 4.5 12 4.5z",
        "chatAdd" to "M12 4.5c4.7 0 8 2.9 8 6.6s-3.3 6.6-8 6.6c-1 0-1.9-.1-2.7-.4L5 19l1.1-3.3C4.8 14.5 4 13 4 11.1 4 7.4 7.3 4.5 12 4.5zM12 8.5v5M9.5 11h5",
        "chats" to "M9.5 4.5c3.8 0 6.5 2.3 6.5 5.3s-2.7 5.3-6.5 5.3c-.8 0-1.5-.1-2.2-.3L4 16l.9-2.6C3.7 12.4 3 11.2 3 9.8 3 6.8 5.7 4.5 9.5 4.5zM18.5 9.5c1.6.9 2.5 2.3 2.5 3.9 0 1.2-.5 2.3-1.4 3.1L20.3 19l-2.9-1.1c-.6.2-1.3.3-2 .3-1.7 0-3.2-.5-4.3-1.3",
        "msg" to "M5 5h14v10H9l-4 4z",
        "mic" to "M12 3.5h0a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h-0a3 3 0 0 1 -3 -3v-5a3 3 0 0 1 3 -3zM5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21",
        "volume" to "M4 9.5h3l4.5-4v13L7 14.5H4zM15 9a4 4 0 0 1 0 6M17.5 6.5a7.5 7.5 0 0 1 0 11",
        "volumeOff" to "M4 9.5h3l4.5-4v13L7 14.5H4zM15.5 9.5l5 5M20.5 9.5l-5 5",
        "waveform" to "M4 10.5v3M8 7v10M12 4.5v15M16 7.5v9M20 10.5v3",
        "keyboard" to "M4.5 6.5h15A1.5 1.5 0 0 1 21 8v8a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 16V8a1.5 1.5 0 0 1 1.5-1.5zM7 10h.01M10.3 10h.01M13.7 10h.01M17 10h.01M8 14h8",
        "keyboardHide" to "M4.5 4h15A1.5 1.5 0 0 1 21 5.5v6a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 11.5v-6A1.5 1.5 0 0 1 4.5 4zM8 9.5h8M9 17l3 3 3-3",
        "enter" to "M19 5v6.5a2.5 2.5 0 0 1-2.5 2.5H6M10 10l-4 4 4 4",
        "tab" to "M4 12h12M12 8l4 4-4 4M20 6v12",
        "backspace" to "M8.5 5.5H19a1.5 1.5 0 0 1 1.5 1.5v10a1.5 1.5 0 0 1-1.5 1.5H8.5L3.5 12zM11 9.5l5 5M16 9.5l-5 5",
        "text" to "M5 7V5.5h14V7M12 5.5v13M9 18.5h6",
        "textSize" to "M3.5 8V6h9v2M8 6v12M6 18h4M14 12v-1.5h6.5V12M17.2 10.5v7.5M16 18h2.5",
        // system / tech
        "gear" to "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0zM12 3.5v2.2M12 18.3v2.2M3.5 12h2.2M18.3 12h2.2M6 6l1.6 1.6M16.4 16.4L18 18M6 18l1.6-1.6M16.4 7.6L18 6",
        "terminal" to "M5 4.5h14A1.5 1.5 0 0 1 20.5 6v12a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 18V6A1.5 1.5 0 0 1 5 4.5zM7.5 9l3 3-3 3M12.5 15.5h4",
        "code" to "M8.5 7.5L4 12l4.5 4.5M15.5 7.5L20 12l-4.5 4.5M13.5 5l-3 14",
        "js" to "M5 4.5h14v15H5zM10.5 9.5v5a1.5 1.5 0 0 1-3 0M16.5 10a1.6 1.6 0 0 0-3 .4c0 1.8 3 1.2 3 3a1.6 1.6 0 0 1-3 .4",
        "server" to "M5.5 4.5h13a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-3a1.5 1.5 0 0 1 1.5 -1.5zM5.5 13.5h13a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-3a1.5 1.5 0 0 1 1.5 -1.5zM7.5 7.5h.01M7.5 16.5h.01",
        "storage" to "M5 6.5c0-1.4 3.1-2.5 7-2.5s7 1.1 7 2.5-3.1 2.5-7 2.5-7-1.1-7-2.5zM5 6.5v11c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5v-11M5 12c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5",
        "sd" to "M9 3.5h8.5A1.5 1.5 0 0 1 19 5v14a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 19V7.5zM10 7v2.5M13 7v2.5M16 7v2.5",
        "monitor" to "M5 4.5h14a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1 -1.5 1.5h-14a1.5 1.5 0 0 1 -1.5 -1.5v-8a1.5 1.5 0 0 1 1.5 -1.5zM9 19.5h6M12 15.5v4",
        "phone" to "M8 3.5h8A1.5 1.5 0 0 1 17.5 5v14a1.5 1.5 0 0 1-1.5 1.5H8A1.5 1.5 0 0 1 6.5 19V5A1.5 1.5 0 0 1 8 3.5zM11 17.5h2",
        "cpu" to "M8 6h8a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2zM9.5 9.5h5v5h-5zM9 2.5v3M15 2.5v3M9 18.5v3M15 18.5v3M2.5 9h3M2.5 15h3M18.5 9h3M18.5 15h3",
        "wifi" to "M3.5 9.5a12 12 0 0 1 17 0M6.5 12.5a7.8 7.8 0 0 1 11 0M9.5 15.5a3.5 3.5 0 0 1 5 0M12 18.5h.01",
        "battery" to "M5 7.5h12A1.5 1.5 0 0 1 18.5 9v6a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 15V9A1.5 1.5 0 0 1 5 7.5zM21 10.5v3",
        "batteryAlert" to "M5 7.5h12A1.5 1.5 0 0 1 18.5 9v6a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 15V9A1.5 1.5 0 0 1 5 7.5zM21 10.5v3M11 10v2.5M11 14.5v.01",
        "cloud" to "M7 18a4.5 4.5 0 0 1-.5-9 6 6 0 0 1 11.5 1.5A3.8 3.8 0 0 1 17.5 18z",
        "cloudUp" to "M7 18a4.5 4.5 0 0 1-.5-9 6 6 0 0 1 11.5 1.5A3.8 3.8 0 0 1 17.5 18M12 20v-7M9.5 15.5L12 13l2.5 2.5",
        "cloudDown" to "M7 17a4.5 4.5 0 0 1-.5-9 6 6 0 0 1 11.5 1.5A3.8 3.8 0 0 1 17.5 17M12 12v8M9.5 17.5L12 20l2.5-2.5",
        "bolt" to "M13 3L5 14h6l-1 7 8-11h-6z",
        "bug" to "M8 9.5a4 4 0 0 1 8 0V15a4 4 0 0 1-8 0zM12 11v8M4.5 13H8M16 13h3.5M5 8l3 2M19 8l-3 2M5.5 19l2.8-2M18.5 19l-2.8-2M9.5 5.5L8.5 4M14.5 5.5l1-1.5",
        "robot" to "M7 8.5h10A2.5 2.5 0 0 1 19.5 11v6a2.5 2.5 0 0 1-2.5 2.5H7A2.5 2.5 0 0 1 4.5 17v-6A2.5 2.5 0 0 1 7 8.5zM12 8.5V5M12 5h.01M9.5 13.5v.5M14.5 13.5v.5M2.5 13v2M21.5 13v2",
        "android" to "M6 10.5a6 6 0 0 1 12 0V17H6zM9 8h.01M15 8h.01M7.5 5l1.5 2M16.5 5L15 7M9 17v3M15 17v3",
        "sparkles" to "M10 4l1.6 4.4L16 10l-4.4 1.6L10 16l-1.6-4.4L4 10l4.4-1.6zM17.5 14.5l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z",
        "brain" to "M12 5.5v13M12 5.5a3 3 0 0 0-5.6 1.5 3 3 0 0 0-1.9 4.5A3 3 0 0 0 6 16.8 3 3 0 0 0 12 18.5M12 5.5a3 3 0 0 1 5.6 1.5 3 3 0 0 1 1.9 4.5 3 3 0 0 1-1.5 5.3 3 3 0 0 1-6 1.7",
        "bulb" to "M9 17.5h6M10 20.5h4M12 3.5a5.5 5.5 0 0 0-3.2 10c.7.5 1.2 1.3 1.2 2.2v1.8h4v-1.8c0-.9.5-1.7 1.2-2.2A5.5 5.5 0 0 0 12 3.5z",
        "layers" to "M12 4l8.5 4.5L12 13 3.5 8.5zM3.5 12.5L12 17l8.5-4.5M3.5 16.5L12 21l8.5-4.5",
        "puzzle" to "M5 8h3a2 2 0 1 1 4 0h3a1 1 0 0 1 1 1v3a2 2 0 1 1 0 4v3a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z",
        "wrench" to "M14.5 5.5a4 4 0 0 0-5 5L4 16l4 4 5.5-5.5a4 4 0 0 0 5-5l-2.5 2.5-2.5-.5-.5-2.5z",
        "box" to "M4 7.5L12 4l8 3.5v9L12 20l-8-3.5zM4 7.5l8 3.5 8-3.5M12 11v9",
        "grid" to "M5 4.5h5a.5.5 0 0 1 .5.5v5a.5.5 0 0 1-.5.5H5a.5.5 0 0 1-.5-.5V5a.5.5 0 0 1 .5-.5zM14 4.5h5a.5.5 0 0 1 .5.5v5a.5.5 0 0 1-.5.5h-5a.5.5 0 0 1-.5-.5V5a.5.5 0 0 1 .5-.5zM5 13.5h5a.5.5 0 0 1 .5.5v5a.5.5 0 0 1-.5.5H5a.5.5 0 0 1-.5-.5v-5a.5.5 0 0 1 .5-.5zM14 13.5h5a.5.5 0 0 1 .5.5v5a.5.5 0 0 1-.5.5h-5a.5.5 0 0 1-.5-.5v-5a.5.5 0 0 1 .5-.5z",
        "apps" to "M6 6h.01M12 6h.01M18 6h.01M6 12h.01M12 12h.01M18 12h.01M6 18h.01M12 18h.01M18 18h.01",
        "dashboard" to "M4.5 4.5h6v8h-6zM13.5 4.5h6v4h-6zM13.5 11.5h6v8h-6zM4.5 15.5h6v4h-6z",
        "chart" to "M5 19.5V11M10 19.5V5M15 19.5v-6M20 19.5V9",
        "insights" to "M4 18l5-6 4 3 7-8M15 7h5v5",
        "data" to "M12 3.5a8.5 8.5 0 1 0 8.5 8.5H12z M15 3.9A8.5 8.5 0 0 1 20.1 9H15z",
        "calc" to "M6.5 3.5h11A1.5 1.5 0 0 1 19 5v14a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 19V5a1.5 1.5 0 0 1 1.5-1.5zM8 7.5h8M8.5 12h.01M12 12h.01M15.5 12h.01M8.5 16h.01M12 16h.01M15.5 16h.01",
        "route" to "M3.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0zM15.8 6a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0zM8.2 18H14a3.5 3.5 0 0 0 0-7h-4a3.5 3.5 0 0 1 0-7h5.8",
        "fork" to "M7 4.5v6a4 4 0 0 0 4 4h1M17 4.5v6a4 4 0 0 1-4 4M12 14.5v5M4.5 7L7 4.5 9.5 7M14.5 7L17 4.5 19.5 7",
        "hub" to "M9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM4 5.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM17 5.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM4 18.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM17 18.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM6.6 7l3.3 3.3M17.4 7l-3.3 3.3M6.6 17l3.3-3.3M17.4 17l-3.3-3.3",
        "tree" to "M4.5 4.5h5v4h-5zM14.5 10h5v4h-5zM14.5 16h5v4h-5zM7 8.5V18h7.5M7 12h7.5",
        "graph" to "M4 6a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM16 7a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM10 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM7.7 7.2l3.2 8M16.6 8.5l-3.6 7M8 6.2l8 .7",
        "palette" to "M12 3.5a8.5 8.5 0 0 0 0 17c1 0 1.5-.7 1.5-1.5 0-1.3-1-1.7-1-2.7 0-.8.7-1.3 1.5-1.3h2a4.5 4.5 0 0 0 4.5-4.5c0-3.9-3.8-7-8.5-7zM7.5 11.5h.01M10 7.5h.01M14.5 7.5h.01",
        "brush" to "M19.5 4.5L11 13M11 13l-2.5-.5C6 12 4.5 14 4.5 16c0 1.5-.5 2.5-1 3.5 4 .5 7.5-.5 8-4z",
        "sun" to "M8.5 12a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0zM12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6L7 7M17 17l1.4 1.4M5.6 18.4L7 17M17 7l1.4-1.4",
        "moon" to "M19 14.5A7.5 7.5 0 0 1 9.5 5a7.5 7.5 0 1 0 9.5 9.5z",
        "auto" to "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0zM9 15.5l3-7.5 3 7.5M10 13.2h4",
        "heart" to "M12 19.5s-7-4.3-7-9.3A3.9 3.9 0 0 1 12 8a3.9 3.9 0 0 1 7 2.2c0 5-7 9.3-7 9.3z",
        "diamond" to "M7 4.5h10l3.5 4.5L12 19.5 3.5 9zM3.5 9h17M10 4.5L8.5 9 12 19.5 15.5 9 14 4.5",
        "gift" to "M5.5 9h13a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-8a1.5 1.5 0 0 1 1.5 -1.5zM12 9v11M3 9h18M12 9c-2-4-6-4-6-1.5S10 9 12 9zm0 0c2-4 6-4 6-1.5S14 9 12 9z",
        "money" to "M4.5 6.5h15A1.5 1.5 0 0 1 21 8v8a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 16V8a1.5 1.5 0 0 1 1.5-1.5zM9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM6 9.5v.01M18 14.5v.01",
        "game" to "M7.5 8h9a4 4 0 0 1 4 4.5l-.6 3.5a2.5 2.5 0 0 1-4.4 1.2L14 15.5h-4l-1.5 1.7A2.5 2.5 0 0 1 4.1 16l-.6-3.5a4 4 0 0 1 4-4.5zM8 10.5v3M6.5 12h3M15.5 11h.01M17 13h.01",
        "touch" to "M10 11.5V5.5a1.5 1.5 0 0 1 3 0v5l4.3 1a2 2 0 0 1 1.5 2.3l-.9 5.2H11l-3.6-4.3a1.6 1.6 0 0 1 2.4-2.1l.2.2",
        "access" to "M10.5 4.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0zM5 8l7 1.5L19 8M12 9.5V14M12 14l-3 6M12 14l3 6",
        "flag" to "M6 21V4M6 4h11l-2 4 2 4H6",
        "loop" to "M17 8a6 6 0 1 0 1 6M18 4v4h-4",
        "archive" to "M4 5h16v4H4zM5.5 9v9.5a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1V9M10 13h4",
        "inventory" to "M4 5h16v4H4zM5.5 9v9.5a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1V9M10 13h4",
        "move" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2zM10 13.5h5M13 11l2.5 2.5L13 16",
        "addHome" to "M8 3.5h8A1.5 1.5 0 0 1 17.5 5v14a1.5 1.5 0 0 1-1.5 1.5H8A1.5 1.5 0 0 1 6.5 19V5A1.5 1.5 0 0 1 8 3.5zM12 9v6M9 12h6",
        "nw" to "M17.5 17.5l-10-10M7.5 15V7.5H15",
        "top" to "M5 4.5h14M12 20V9M7 13.5L12 8.5l5 5",
        "block" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM6 6l12 12",
        "deleteSweep" to "M4 7h9M7.5 7V5h2v2M5.5 7l.8 11h6.4l.8-11M15 9h5M15 13h4M15 17h3",
        "backup" to "M7 18a4.5 4.5 0 0 1-.5-9 6 6 0 0 1 11.5 1.5A3.8 3.8 0 0 1 17.5 18M12 20v-7M9.5 15.5L12 13l2.5 2.5",
        "restart" to "M5 12a7 7 0 1 0 2-4.9M5 4.5V8h3.5",
        "notify" to "M6.5 16.5V11a5.5 5.5 0 0 1 11 0v5.5l1.5 1.5H5zM10 20h4M3.5 8A8 8 0 0 1 6 4M20.5 8A8 8 0 0 0 18 4",
        "screenLock" to "M8 3.5h8A1.5 1.5 0 0 1 17.5 5v14a1.5 1.5 0 0 1-1.5 1.5H8A1.5 1.5 0 0 1 6.5 19V5A1.5 1.5 0 0 1 8 3.5zM10 12.5h4v3h-4zM10.8 12.5v-1a1.2 1.2 0 0 1 2.4 0v1",
        "logout" to "M10 5H5v14h5M14 8l4 4-4 4M18 12H9",
        "repo" to "M6 4.5h11a1 1 0 0 1 1 1V17H7a1.5 1.5 0 0 0 0 3h11M6 4.5v14",
        "sendCircle" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM12 16.5v-9M8.5 11L12 7.5l3.5 3.5",
        "clip" to "M19 11.5l-7 7a4.5 4.5 0 0 1-6.4-6.4l7.4-7.4a3 3 0 0 1 4.2 4.2l-7.3 7.3a1.5 1.5 0 0 1-2.1-2.1l6.7-6.7",
        "list" to "M9 7h10.5M9 12h10.5M9 17h10.5M4.5 7h.01M4.5 12h.01M4.5 17h.01",
        "pause2" to "M9.5 6v12M14.5 6v12",
    )

    private val cache = HashMap<String, ImageVector>()

    fun has(name: String) = paths.containsKey(name)

    /** Material-sized (24-unit viewport, glyph inset like Material icons). */
    fun get(name: String, stroke: Float = LINE): ImageVector = build(name, stroke, false)

    /** Hark-sized: viewport cropped to 3..21 so the glyph fills the box like Hark's 16-grid icons. */
    fun tight(name: String, stroke: Float): ImageVector = build(name, stroke, true)

    private fun build(name: String, stroke: Float, tight: Boolean): ImageVector = cache.getOrPut("$name@$stroke@$tight") {
        val spec = paths[name] ?: paths.getValue("dot")
        val strokePath = spec.substringBefore('|')
        val fillPath = if ('|' in spec) spec.substringAfter('|') else null
        val vp = if (tight) 18f else 24f
        ImageVector.Builder(name, 24.dp, 24.dp, vp, vp).apply {
            if (tight) group(translationX = -3f, translationY = -3f) {
                addParts(strokePath, fillPath, stroke)
            } else addParts(strokePath, fillPath, stroke)
        }.build()
    }

    private fun ImageVector.Builder.addParts(strokePath: String, fillPath: String?, stroke: Float) {
        run {
            if (strokePath.isNotBlank()) {
                addPath(
                    pathData = addPathNodes(strokePath),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = stroke,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
            if (fillPath != null) {
                addPath(pathData = addPathNodes(fillPath), fill = SolidColor(Color.Black))
            }
        }
    }
}
