package com.openminis.app.hassn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openminis.app.R
import com.openminis.app.ui.theme.LocalChatPalette

/**
 * [Hassn-P1] Hark-style light glass skin, values ported 1:1 from the approved v2 mockups
 * (work/mock/v2/gen2.py CSS): sky background + blue tint, 26dp white glass cards, 48dp glass
 * circle buttons, black selected pill in a glass segmented control, 1.8-stroke line icons.
 */
@Immutable
data class HassnPalette(
    val dark: Boolean,
    val text: Color,          // #141416
    val text2: Color,         // .rt small  #7a7e86
    val text3: Color,         // status line / group header #3d4250
    val card: Color,          // .card rgba(255,255,255,.64)
    val cardStrong: Color,    // chat cards rgba(255,255,255,.82)
    val divider: Color,       // rgba(20,20,30,.075)
    val ink: Color,           // #18181a (selected tab, black buttons, bars)
    val onInk: Color,
    val seg: Color,           // rgba(214,222,236,.80)
    val segBorder: Color,     // rgba(255,255,255,.45)
    val segText: Color,       // #6a6f79
    val glassButton: Color,   // .gb rgba(222,228,238,.78)
    val soft: Color,          // .rnd rgba(20,20,30,.06)
    val track: Color,         // .bar rgba(20,20,30,.08)
    val input: Color,         // composer rgba(236,239,245,.80)
    val inputHint: Color,     // #8a8e96
    val userBubble: Color,    // rgba(206,216,232,.72)
    val assistantBubble: Color, // #fff
    val tag: Color,           // rgba(20,20,30,.065)
    val tagText: Color,       // #5a5e66
    val green: Color,
    val idle: Color,
    val warn: Color,
    val red: Color,
    val link: Color,
)

val HassnLight = HassnPalette(
    dark = false,
    text = Color(0xFF141416),
    text2 = Color(0xFF7A7E86),
    text3 = Color(0xFF3D4250),
    card = Color.White.copy(alpha = 0.64f),
    cardStrong = Color.White.copy(alpha = 0.82f),
    divider = Color(0x1314141E),
    ink = Color(0xFF18181A),
    onInk = Color.White,
    seg = Color(0xCCD6DEEC),
    segBorder = Color.White.copy(alpha = 0.45f),
    segText = Color(0xFF6A6F79),
    glassButton = Color(0xC7DEE4EE),
    soft = Color(0x0F14141E),
    track = Color(0x1414141E),
    input = Color(0xCCECEFF5),
    inputHint = Color(0xFF8A8E96),
    userBubble = Color(0xB8CED8E8),
    assistantBubble = Color.White,
    tag = Color(0x1114141E),
    tagText = Color(0xFF5A5E66),
    green = Color(0xFF3AA864),
    idle = Color(0xFFA3A7AE),
    warn = Color(0xFFD9952A),
    red = Color(0xFFC9473D),
    link = Color(0xFF2C66D8),
)

/** Dark theme keeps the same layout; glass turns smoky so white text stays readable. */
val HassnDark = HassnLight.copy(
    dark = true,
    text = Color(0xFFF4F5F7),
    text2 = Color(0xFFA9ADB5),
    text3 = Color(0xFFD5D8DE),
    card = Color(0xA81C1E24),
    cardStrong = Color(0xD11C1E24),
    divider = Color(0x1AFFFFFF),
    ink = Color(0xFFF4F5F7),
    onInk = Color(0xFF141416),
    seg = Color(0xB3262A33),
    segBorder = Color.White.copy(alpha = 0.12f),
    segText = Color(0xFFA9ADB5),
    glassButton = Color(0xB3262A33),
    soft = Color(0x1AFFFFFF),
    track = Color(0x24FFFFFF),
    input = Color(0xCC262A33),
    inputHint = Color(0xFF8A8E96),
    userBubble = Color(0xB83A4252),
    assistantBubble = Color(0xE01C1E24),
    tag = Color(0x1FFFFFFF),
    tagText = Color(0xFFC4C7CE),
    link = Color(0xFF7FA8F0),
)

val hassn: HassnPalette
    @Composable
    @ReadOnlyComposable
    get() = if (LocalChatPalette.current.isDark) HassnDark else HassnLight

/** Shell tab chosen from outside the shell (e.g. the tabs on the Settings page). */
object HassnShellState {
    val tab = mutableIntStateOf(0)
    /** CI shots: next ChatScreen seeds an in-memory sample conversation. */
    @Volatile var demoChatPending = false
}

// ─── line icons (mockup IC set, 24-unit viewBox, stroke 1.8, round caps) ───
object HassnIcons {
    private val paths = mapOf(
        "menu" to "M5 7h14M5 12h10M5 17h7",
        "search" to "M4.5 11a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0 -13 0zM16 16l4 4",
        "plus" to "M12 5v14M5 12h14",
        "mic" to "M12 3.5h0a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h-0a3 3 0 0 1 -3 -3v-5a3 3 0 0 1 3 -3zM5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21",
        "back" to "M14.5 6l-6 6 6 6",
        "chev" to "M9.5 6.5l5.5 5.5-5.5 5.5",
        "down" to "M7 10l5 5 5-5",
        "more" to "M5.5 12h.01M12 12h.01M18.5 12h.01",
        "close" to "M6.5 6.5l11 11M17.5 6.5l-11 11",
        "folder" to "M3.5 7.5a2 2 0 012-2h4l2 2.2h7a2 2 0 012 2v7.8a2 2 0 01-2 2h-13a2 2 0 01-2-2z",
        "file" to "M7 3.5h6.5l4.5 4.5v12.5H7zM13.5 3.5V8H18",
        "check" to "M5 12.5l4.5 4.5L19 7.5",
        "trash" to "M5 7h14M10 7V5h4v2M7 7l1 12h8l1-12M10.5 10.5v6M13.5 10.5v6",
        "user" to "M8.2 8.5a3.8 3.8 0 1 0 7.6 0a3.8 3.8 0 1 0 -7.6 0zM4.5 20a7.5 7.5 0 0 1 15 0",
        "clock" to "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0zM12 8v4.5l3 2",
        "link" to "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1",
        "card" to "M5.5 6h13a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-13a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2zM3.5 10h17",
        "shield" to "M12 3.5l7 2.8v5.4c0 4.4-3 7.4-7 8.8-4-1.4-7-4.4-7-8.8V6.3z",
        "gear" to "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0zM12 3.5v2.2M12 18.3v2.2M3.5 12h2.2M18.3 12h2.2M6 6l1.6 1.6M16.4 16.4L18 18M6 18l1.6-1.6M16.4 7.6L18 6",
        "msg" to "M5 5h14v10H9l-4 4z",
        "info" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM12 11v5.5M12 7.8v.4",
        "logout" to "M10 5H5v14h5M14 8l4 4-4 4M18 12H9",
        "route" to "M3.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0zM15.8 6a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0zM8.2 18H14a3.5 3.5 0 0 0 0-7h-4a3.5 3.5 0 0 1 0-7h5.8",
        "users" to "M5.7 9a3.3 3.3 0 1 0 6.6 0a3.3 3.3 0 1 0 -6.6 0zM3 19a6 6 0 0 1 12 0M14.4 8a2.6 2.6 0 1 0 5.2 0a2.6 2.6 0 1 0 -5.2 0zM16 13.5a5 5 0 0 1 5.5 5",
        "server" to "M5.5 4.5h13a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-3a1.5 1.5 0 0 1 1.5 -1.5zM5.5 13.5h13a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-3a1.5 1.5 0 0 1 1.5 -1.5zM7.5 7.5h.01M7.5 16.5h.01",
        "monitor" to "M5 4.5h14a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1 -1.5 1.5h-14a1.5 1.5 0 0 1 -1.5 -1.5v-8a1.5 1.5 0 0 1 1.5 -1.5zM9 19.5h6M12 15.5v4",
        "graph" to "M4 6a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM16 7a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM10 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM7.7 7.2l3.2 8M16.6 8.5l-3.6 7M8 6.2l8 .7",
        "key" to "M4.2 15a3.8 3.8 0 1 0 7.6 0a3.8 3.8 0 1 0 -7.6 0zM10.8 12.2L19 4M15.5 7.5l2.5 2.5",
        "globe" to "M3.5 12a8.5 8.5 0 1 0 17 0a8.5 8.5 0 1 0 -17 0zM3.5 12h17M12 3.5c3 3 3 14 0 17M12 3.5c-3 3-3 14 0 17",
        "bolt" to "M13 3L5 14h6l-1 7 8-11h-6z",
        "heart" to "M12 19.5s-7-4.3-7-9.3A3.9 3.9 0 0 1 12 8a3.9 3.9 0 0 1 7 2.2c0 5-7 9.3-7 9.3z",
        "chat" to "M12 4.5c4.7 0 8 2.9 8 6.6s-3.3 6.6-8 6.6c-1 0-1.9-.1-2.7-.4L5 19l1.1-3.3C4.8 14.5 4 13 4 11.1 4 7.4 7.3 4.5 12 4.5z",
        "pause" to "M9.5 6v12M14.5 6v12",
        "gift" to "M5.5 9h13a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1 -1.5 1.5h-13a1.5 1.5 0 0 1 -1.5 -1.5v-8a1.5 1.5 0 0 1 1.5 -1.5zM12 9v11M3 9h18M12 9c-2-4-6-4-6-1.5S10 9 12 9zm0 0c2-4 6-4 6-1.5S14 9 12 9z",
        "loop" to "M17 8a6 6 0 1 0 1 6M18 4v4h-4",
        "wrench" to "M14.5 5.5a4 4 0 0 0-5 5L4 16l4 4 5.5-5.5a4 4 0 0 0 5-5l-2.5 2.5-2.5-.5-.5-2.5z",
        "refresh" to "M19 12a7 7 0 1 1-2-4.9M19 4v4h-4",
        "arrow" to "M5 12h13M13 7l5 5-5 5",
        "repo" to "M6 4.5h11a1 1 0 0 1 1 1V17H7a1.5 1.5 0 0 0 0 3h11M6 4.5v14",
        "edit" to "M4.5 19.5l4-1 10-10-3-3-10 10z",
        "cloud" to "M7 18a4.5 4.5 0 0 1-.5-9 6 6 0 0 1 11.5 1.5A3.8 3.8 0 0 1 17.5 18z",
        "flag" to "M6 21V4M6 4h11l-2 4 2 4H6",
        "cpu" to "M8 6h8a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2zM9.5 9.5h5v5h-5zM9 2.5v3M15 2.5v3M9 18.5v3M15 18.5v3M2.5 9h3M2.5 15h3M18.5 9h3M18.5 15h3",
    )
    private val cache = HashMap<String, ImageVector>()

    fun get(name: String, stroke: Float = 1.8f): ImageVector = cache.getOrPut("$name@$stroke") {
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(
                pathData = addPathNodes(paths.getValue(name)),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
}

@Composable
fun HassnIcon(name: String, size: Dp = 20.dp, tint: Color = hassn.text, stroke: Float = 1.8f, modifier: Modifier = Modifier) {
    Icon(HassnIcons.get(name, stroke), contentDescription = null, tint = tint, modifier = modifier.size(size))
}

// ─── background: Mines sky photo (pre-blurred, desaturated) + the mockup's blue tint + weather ───
private fun skyRes(kind: HassnWeather.Kind): Int = when (kind) {
    HassnWeather.Kind.SUN -> R.drawable.hassn_sky_sunny
    HassnWeather.Kind.STARS -> R.drawable.hassn_sky_night
    HassnWeather.Kind.CLOUDS -> R.drawable.hassn_sky_cloudy
    HassnWeather.Kind.RAIN, HassnWeather.Kind.THUNDER -> R.drawable.hassn_sky_rain
    HassnWeather.Kind.SNOW -> R.drawable.hassn_sky_snow
    HassnWeather.Kind.FOG -> R.drawable.hassn_sky_fog
    HassnWeather.Kind.NONE -> R.drawable.hassn_sky_overcast
}

private val LightTint = Brush.verticalGradient(
    0f to Color(0x947696CD), 0.45f to Color(0x8092ACD6), 1f to Color(0x75A6B6D4),
)
private val DarkTint = Brush.verticalGradient(
    0f to Color(0xB30A0E17), 0.5f to Color(0xA6141A26), 1f to Color(0xB80A0E17),
)

@Composable
fun HassnSkyBackground(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val tick by HassnWeather.tick.collectAsState()
    val kind = remember(tick) { HassnWeather.currentKind(ctx) }
    val dark = hassn.dark
    Box(modifier) {
        Image(
            painter = painterResource(skyRes(kind)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(if (dark) DarkTint else LightTint))
        HassnWeatherLayer(Modifier.fillMaxSize())
    }
}

// ─── building blocks ───
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    color: Color = hassn.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(color),
        content = content,
    )
}

@Composable
fun GlassCircleButton(icon: String, onClick: () -> Unit, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(hassn.glassButton)
            .clickable(onClickLabel = contentDescription, onClick = onClick),
    ) { HassnIcon(icon, 24.dp, stroke = 2f) }
}

/** The glass segmented control: 54dp tall, black selected pill, -1 = nothing selected. */
@Composable
fun HassnTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val p = hassn
    Row(
        modifier
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(p.seg)
            .border(2.dp, p.segBorder, RoundedCornerShape(27.dp))
            .padding(4.dp),
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (on) p.ink else Color.Transparent)
                    .clickable { onSelect(i) },
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    color = if (on) p.onInk else p.segText,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Top bar of the shell and of Settings: [left glass button] [tabs] [user glass button]. */
@Composable
fun HassnTopBar(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    leftIcon: String,
    onLeft: () -> Unit,
    onProfile: () -> Unit,
    profileDescription: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 16.dp),
    ) {
        GlassCircleButton(leftIcon, onLeft)
        HassnTabs(labels, selected, onSelect, Modifier.weight(1f))
        GlassCircleButton("user", onProfile, contentDescription = profileDescription)
    }
}

/** Sub-page top bar: glass back button, centred 18sp bold title, optional right slot. */
@Composable
fun HassnSubTopBar(title: String, onBack: () -> Unit, right: (@Composable () -> Unit)? = null) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 16.dp),
    ) {
        GlassCircleButton("back", onBack, Modifier.align(Alignment.CenterStart))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = hassn.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 60.dp),
        )
        if (right != null) Box(Modifier.align(Alignment.CenterEnd)) { right() }
    }
}

@Composable
fun HassnGroupHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = if (hassn.dark) hassn.text3 else Color(0xFF3A404B),
        modifier = modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 7.dp),
    )
}

@Composable
fun HassnDivider() {
    Box(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(hassn.divider),
    )
}

/** One mockup `.row`: icon · title/subtitle · value · chevron, min 54dp. */
@Composable
fun HassnRow(
    title: String,
    subtitle: String? = null,
    icon: String? = null,
    value: String? = null,
    chevron: Boolean = true,
    onClick: (() -> Unit)? = null,
    titleColor: Color = hassn.text,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val p = hassn
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 18.dp),
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(13.dp)) }
        else if (icon != null) { HassnIcon(icon, 20.dp); Spacer(Modifier.width(13.dp)) }
        Column(Modifier.weight(1f).padding(vertical = 9.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, fontSize = 12.5.sp, color = p.text2, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp, modifier = Modifier.padding(top = 1.dp))
            }
        }
        if (value != null) {
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 13.sp, color = p.text2, maxLines = 1)
        }
        if (trailing != null) { Spacer(Modifier.width(8.dp)); trailing() }
        if (chevron) {
            Spacer(Modifier.width(6.dp))
            HassnIcon("chev", 15.dp, tint = p.text, stroke = 2f)
        }
    }
}

/** Rows inside one card, separated by the inset hairline. */
@Composable
fun HassnRowGroup(modifier: Modifier = Modifier, rows: List<@Composable () -> Unit>) {
    GlassCard(modifier) {
        rows.forEachIndexed { i, r ->
            if (i > 0) HassnDivider()
            r()
        }
    }
}

@Composable
fun HassnBar(progress: Float, modifier: Modifier = Modifier) {
    val p = hassn
    Box(modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(p.track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(p.ink))
    }
}

@Composable
fun HassnDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(7.dp).clip(CircleShape).background(color))
}

/** Letter avatar (mockup `ava`). */
@Composable
fun HassnLetterAvatar(ch: String, size: Dp = 36.dp) {
    val p = hassn
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size).clip(CircleShape).background(if (p.dark) Color(0x33FFFFFF) else Color(0xFFDFE5EE)),
    ) {
        Text(ch.take(1), fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.SemiBold, color = if (p.dark) p.text else Color(0xFF3D4149))
    }
}

/** Status pill: running = grey glass with spinner ring, done = white glass with check. */
@Composable
fun HassnStatusPill(label: String, running: Boolean) {
    val p = hassn
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (running) Color(0xB8585A62) else Color.White.copy(alpha = if (p.dark) 0.18f else 0.55f))
            .padding(start = 9.dp, end = 13.dp),
    ) {
        if (running) {
            Box(Modifier.size(16.dp).clip(CircleShape).border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape))
        } else {
            HassnIcon("check", 16.dp, tint = if (p.dark) p.text else Color(0xFF3D4149), stroke = 2.2f)
        }
        Spacer(Modifier.width(7.dp))
        Text(label, fontSize = 13.5.sp, color = if (running) Color.White else if (p.dark) p.text else Color(0xFF3D4149))
    }
}

/** Small round action chip inside a row (mockup "确认" black / "查看" soft). */
@Composable
fun HassnChipButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val p = hassn
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (primary) p.ink else p.soft)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Normal, color = if (primary) p.onInk else p.text)
    }
}

/**
 * Drop-in replacement for Material `TopAppBar` / `CenterAlignedTopAppBar` on upstream sub-pages:
 * same parameter names, Hark look — navigation in a round glass button, centred 18sp bold title,
 * actions in one glass capsule. `colors` / `scrollBehavior` are accepted and ignored on purpose.
 * Empty slots draw nothing (the glass wraps the content's own size).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HassnAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = 66.dp,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val p = hassn
    CompositionLocalProvider(LocalContentColor provides p.text) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .heightIn(min = if (expandedHeight < 56.dp) 56.dp else expandedHeight)
                .padding(horizontal = 12.dp),
        ) {
            Box(Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.clip(CircleShape).background(p.glassButton)) { navigationIcon() }
            }
            Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                ProvideTextStyle(TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.text, textAlign = TextAlign.Center)) {
                    title()
                }
            }
            Box(Modifier.widthIn(min = 48.dp), contentAlignment = Alignment.CenterEnd) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(p.glassButton),
                    content = actions,
                )
            }
        }
    }
}

/** Glass capsule chip (chat header model pill / menu): 40dp, glass fill, optional leading line icon. */
@Composable
fun HassnGlassChip(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    trailingIcon: String? = null,
    enabled: Boolean = true,
) {
    val p = hassn
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(p.glassButton)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (label == null) 10.dp else 14.dp),
    ) {
        if (icon != null) HassnIcon(icon, 20.dp, stroke = 2f)
        if (label != null) {
            if (icon != null) Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 180.dp))
        }
        if (trailingIcon != null) { Spacer(Modifier.width(4.dp)); HassnIcon(trailingIcon, 14.dp, stroke = 2.2f) }
    }
}
