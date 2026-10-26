package com.openminis.app.hassn

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.openminis.app.hassn.icons.HassnLineIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
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
 * [Hassn] Hark skin. Every value comes from Hark's own app (APK com.hark.android 1.0.0-52,
 * HarkColors in qw6.java; spec work/hark-spec/HARK_UI_SPEC.md §1). Field names are the old
 * mockup names so existing call sites keep compiling; comments give the Hark token.
 */
@Immutable
data class HassnPalette(
    val dark: Boolean,
    val text: Color,          // textPrimary
    val text2: Color,         // textSecondary (ink @50 %)
    val text3: Color,         // textSecondary (status lines, headers)
    val card: Color,          // regularSurface (white @66.75 % / ink @61.75 %)
    val cardStrong: Color,    // chatBubbleAssistantSurface (white @98 %)
    val divider: Color,       // strokeSecondary (ink @7 %)
    val ink: Color,           // fillPrimary
    val onInk: Color,         // textPrimaryOn
    val seg: Color,           // tab pill = regularSurface
    val segBorder: Color,     // strokePrimaryOn (white @10 %)
    val segText: Color,       // textSecondary
    val glassButton: Color,   // thinBg (white @55 %)
    val soft: Color,          // fillTertiary (ink @5 %)
    val track: Color,         // fillTertiary
    val input: Color,         // composerBg
    val inputHint: Color,     // textTertiary
    val userBubble: Color,    // chatBubbleUserSurface (white @30 %)
    val assistantBubble: Color, // chatBubbleAssistantSurface
    val tag: Color,           // status chip fill (ink @6 %)
    val tagText: Color,       // textSecondary
    val green: Color,         // systemGreen #00A665
    val idle: Color,          // textTertiary
    val warn: Color,          // systemOrange #FF4D00
    val red: Color,           // systemRed #DA2F35
    val link: Color,          // link (ink @50 %)
    val stroke: Color,        // strokePrimary (ink @14 %)
    val solid: Color,         // regularSolid (white @96 %)
    val text3rd: Color,       // textTertiary (ink @30 %)
    val hover: Color,         // hoverWash (ink @5 %)
)

private val HkInk = Color(0xFF1D1D1F)
private val HkPaper = Color(0xFFF5F5F7)
private val HkOnInk = Color(0xFFF9F9FA)

val HassnLight = HassnPalette(
    dark = false,
    text = HkInk,
    text2 = HkInk.copy(alpha = 0.5f),
    text3 = HkInk.copy(alpha = 0.5f),
    card = Color.White.copy(alpha = 0.6675f),
    cardStrong = Color.White.copy(alpha = 0.98f),
    divider = HkInk.copy(alpha = 0.07f),
    ink = HkInk,
    onInk = HkOnInk,
    seg = Color.White.copy(alpha = 0.6675f),
    segBorder = Color.White.copy(alpha = 0.1f),
    segText = HkInk.copy(alpha = 0.5f),
    glassButton = Color.White.copy(alpha = 0.55f),
    soft = HkInk.copy(alpha = 0.05f),
    track = HkInk.copy(alpha = 0.05f),
    input = Color.White.copy(alpha = 0.55f),
    inputHint = HkInk.copy(alpha = 0.3f),
    userBubble = Color.White.copy(alpha = 0.3f),
    assistantBubble = Color.White.copy(alpha = 0.98f),
    tag = HkInk.copy(alpha = 0.06f),
    tagText = HkInk.copy(alpha = 0.5f),
    green = Color(0xFF00A665),
    idle = HkInk.copy(alpha = 0.3f),
    warn = Color(0xFFFF4D00),
    red = Color(0xFFDA2F35),
    link = HkInk.copy(alpha = 0.5f),
    stroke = HkInk.copy(alpha = 0.14f),
    solid = Color.White.copy(alpha = 0.96f),
    text3rd = HkInk.copy(alpha = 0.3f),
    hover = HkInk.copy(alpha = 0.05f),
)

val HassnDark = HassnPalette(
    dark = true,
    text = HkPaper,
    text2 = HkPaper.copy(alpha = 0.5f),
    text3 = HkPaper.copy(alpha = 0.5f),
    card = HkInk.copy(alpha = 0.6175f),
    cardStrong = HkInk.copy(alpha = 0.7f),
    divider = HkPaper.copy(alpha = 0.07f),
    ink = HkPaper,
    onInk = HkInk,
    seg = HkInk.copy(alpha = 0.6175f),
    segBorder = HkInk.copy(alpha = 0.1f),
    segText = HkPaper.copy(alpha = 0.5f),
    glassButton = HkInk.copy(alpha = 0.615f),
    soft = HkPaper.copy(alpha = 0.05f),
    track = HkPaper.copy(alpha = 0.05f),
    input = HkInk.copy(alpha = 0.618f),
    inputHint = HkPaper.copy(alpha = 0.3f),
    userBubble = HkInk.copy(alpha = 0.3f),
    assistantBubble = HkInk.copy(alpha = 0.7f),
    tag = HkPaper.copy(alpha = 0.06f),
    tagText = HkPaper.copy(alpha = 0.5f),
    green = Color(0xFF00A665),
    idle = HkPaper.copy(alpha = 0.3f),
    warn = Color(0xFFFF4D00),
    red = Color(0xFFF1555B),
    link = HkPaper.copy(alpha = 0.5f),
    stroke = HkPaper.copy(alpha = 0.14f),
    solid = HkInk.copy(alpha = 0.96f),
    text3rd = HkPaper.copy(alpha = 0.3f),
    hover = HkPaper.copy(alpha = 0.05f),
)

val hassn: HassnPalette
    @Composable
    @ReadOnlyComposable
    get() = if (LocalChatPalette.current.isDark) HassnDark else HassnLight

// ─── glass (Haze real blur over the one sky; spec §4) ───
/** The sky behind the NavHost is the blur source; null inside dialogs/sheets (other windows). */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
/** The root view the sky lives in; glass inside another window (dialog, popup, sheet) can't blur it. */
val LocalHazeRootView = staticCompositionLocalOf<android.view.View?> { null }

object HarkBlur {
    val Chrome = 50.dp      // tab pill, circle buttons, composer, search
    val Card = 32.dp        // settings / list cards
}

/**
 * Hark glass surface: clip to [shape], blur the sky behind by [blur] (Haze), paint [fill] on top,
 * optional 1dp [stroke]. Without a blur source (dialogs, previews) it is just the fill.
 */
@Composable
fun Modifier.harkGlass(
    shape: Shape,
    fill: Color,
    blur: Dp = HarkBlur.Chrome,
    stroke: Color? = null,
): Modifier {
    val root = LocalHazeRootView.current
    val state = LocalHazeState.current?.takeIf { root != null && root === LocalView.current }
    val dark = LocalChatPalette.current.isDark
    var m = this.clip(shape)
    m = if (state != null) {
        m.hazeEffect(
            state,
            HazeStyle(
                backgroundColor = if (dark) Color.Black else Color.White,
                tints = listOf(HazeTint(fill)),
                blurRadius = blur,
                noiseFactor = 0f,
                fallbackTint = HazeTint(fill.copy(alpha = maxOf(fill.alpha, 0.9f))),
            ),
        )
    } else {
        m.background(fill)
    }
    if (stroke != null) m = m.border(1.dp, stroke, shape)
    return m
}

/** Sky + blur source behind [content]; used once at the root (MainActivity). */
@Composable
fun HassnGlassRoot(content: @Composable () -> Unit) {
    val state = remember { HazeState() }
    Box(Modifier.fillMaxSize()) {
        HassnSkyBackground(Modifier.fillMaxSize().hazeSource(state))
        CompositionLocalProvider(LocalHazeState provides state, LocalHazeRootView provides LocalView.current) { content() }
        HassnToastHost()
    }
}

/** Shell tab chosen from outside the shell (e.g. the tabs on the Settings page). */
object HassnShellState {
    val tab = mutableIntStateOf(0)
    /** CI shots: next ChatScreen seeds an in-memory sample conversation. */
    @Volatile var demoChatPending = false
    /** CI shots: the ledger list opens its domain dropdown once. */
    @Volatile var ledgerDropdownPending = false
}

// ─── line icons: our own Hark-style set (hassn/icons/HassnLineIcons.kt) ───
object HassnIcons {
    fun get(name: String, stroke: Float = HassnLineIcons.LINE): ImageVector = HassnLineIcons.get(name, stroke)
}

/**
 * Hark-style icon: [size] is the glyph box like Hark's 16 dp icons (our 24-unit drawing is cropped
 * to its 3..21 area so the glyph fills the box the way Hark's 16-grid icons do). The line is 2 dp
 * at 16 dp and scales with the icon above that, like Hark's vectors. [stroke] is ignored (legacy).
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun HassnIcon(name: String, size: Dp = 16.dp, tint: Color = hassn.text, stroke: Float = 2f, modifier: Modifier = Modifier) {
    val lineDp = 2f * maxOf(1f, size.value / 16f)
    val units = lineDp * 18f / size.value
    Icon(HassnLineIcons.tight(name, units), contentDescription = null, tint = tint, modifier = modifier.size(size))
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

// ─── building blocks (spec §6) ───
val HarkCardShape = RoundedCornerShape(24.dp)

/** Group card: r24, glass blur 32 + regularSurface (spec §6.5). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    color: Color = hassn.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .harkGlass(HarkCardShape, color, HarkBlur.Card),
        content = content,
    )
}

/** 44dp chrome circle: glass blur 50 + thinBg, 22dp line icon (spec §6.1). */
@Composable
fun GlassCircleButton(icon: String, onClick: () -> Unit, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .harkGlass(CircleShape, hassn.glassButton)
            .clickable(onClickLabel = contentDescription, onClick = onClick),
    ) { HassnIcon(icon, 20.dp) }
}

/**
 * Hark tab pill (spec §6.1): 48dp, CircleShape, glass blur 50 + regularSurface, 1dp
 * strokePrimaryOn; the selected segment is solid ink inset 2dp and slides between tabs.
 * Labels labelLarge 15/18 w500, unselected textSecondary. -1 = nothing selected.
 */
@Composable
fun HassnTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val p = hassn
    BoxWithConstraints(
        modifier
            .height(48.dp)
            .harkGlass(CircleShape, p.seg, stroke = if (p.dark) null else p.segBorder)
            .padding(2.dp),
    ) {
        val n = labels.size.coerceAtLeast(1)
        val segW = maxWidth / n
        if (selected in labels.indices) {
            val x by animateDpAsState(segW * selected, spring(dampingRatio = 0.82f, stiffness = 520f), label = "tab")
            Box(
                Modifier
                    .offset(x = x)
                    .width(segW)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(p.ink),
            )
        }
        Row(Modifier.fillMaxSize()) {
            labels.forEachIndexed { i, label ->
                val on = i == selected
                val c by animateColorAsState(if (on) p.onInk else p.segText, tween(180), label = "tabText")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable { onSelect(i) }
                        .semantics { this.selected = on },
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = c, maxLines = 1)
                }
            }
        }
    }
}

/** Top bar of the shell and of Settings: [44dp glass circle] [48dp tab pill] [44dp profile circle]. */
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
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        GlassCircleButton(leftIcon, onLeft)
        HassnTabs(labels, selected, onSelect, Modifier.weight(1f))
        GlassCircleButton("user", onProfile, contentDescription = profileDescription)
    }
}

/** Sub-page header (spec §6.2): 44dp glass back circle, centred titleLarge 20/24 w500. */
@Composable
fun HassnSubTopBar(title: String, onBack: () -> Unit, right: (@Composable () -> Unit)? = null) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
    ) {
        GlassCircleButton("back", onBack, Modifier.align(Alignment.CenterStart))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = hassn.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
        )
        if (right != null) Box(Modifier.align(Alignment.CenterEnd)) { right() }
    }
}

/** Group header: labelMedium 13/16 w500 textSecondary, 20dp inset. */
@Composable
fun HassnGroupHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = hassn.text2,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
    )
}

/** Hairline divider: 1px strokeSecondary, inset 20dp both sides (spec §6.5). */
@Composable
fun HassnDivider() {
    val px = with(LocalDensity.current) { 1f.toDp() }
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(px)
            .background(hassn.divider),
    )
}

/**
 * Settings / list row (spec §6.5): min 56dp, padding 20 / 18, gap 12, 16dp leading icon,
 * titleMedium 15/18 w500, subtitle textSecondary, trailing value textSecondary, 16dp chevron.
 */
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
    val t = MaterialTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 20.dp, vertical = if (subtitle.isNullOrBlank()) 18.dp else 14.dp),
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(12.dp)) }
        else if (icon != null) { HassnIcon(icon, 16.dp, tint = titleColor); Spacer(Modifier.width(12.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = t.titleMedium, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = t.bodyMedium, color = p.text2, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (value != null) {
            Spacer(Modifier.width(12.dp))
            Text(value, style = t.bodyLarge, color = p.text2, maxLines = 1)
        }
        if (trailing != null) { Spacer(Modifier.width(12.dp)); trailing() }
        if (chevron) {
            Spacer(Modifier.width(12.dp))
            HassnIcon("chev", 16.dp, tint = p.text)
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

/** 4dp progress bar: fillPrimary on fillTertiary. */
@Composable
fun HassnBar(progress: Float, modifier: Modifier = Modifier) {
    val p = hassn
    Box(modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(p.track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(p.ink))
    }
}

@Composable
fun HassnDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(8.dp).clip(CircleShape).background(color))
}

/** Letter avatar on a thin glass disc. */
@Composable
fun HassnLetterAvatar(ch: String, size: Dp = 36.dp) {
    val p = hassn
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size).clip(CircleShape).background(p.soft),
    ) {
        Text(ch.take(1), fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.Medium, color = p.text)
    }
}

/** Small spinner ring used in status chips. */
@Composable
fun HassnSpinner(size: Dp = 16.dp, color: Color = hassn.text, track: Color = color.copy(alpha = 0.25f)) {
    val tr = rememberInfiniteTransition(label = "spin")
    val a by tr.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "a")
    Canvas(Modifier.size(size)) {
        val w = 2.dp.toPx()
        drawCircle(track, radius = (this.size.minDimension - w) / 2, style = Stroke(w))
        drawArc(color, a, 100f, false, topLeft = Offset(w / 2, w / 2), size = Size(this.size.width - w, this.size.height - w), style = Stroke(w, cap = StrokeCap.Round))
    }
}

/**
 * Status chip (spec §6.6). running = Hark project status chip (dark glass, white labelMedium,
 * 16dp spinner); otherwise the quiet chip (ink @6 %, textSecondary, check).
 */
@Composable
fun HassnStatusPill(label: String, running: Boolean) {
    val p = hassn
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(26.dp)
            .clip(CircleShape)
            .background(if (running) HkInk.copy(alpha = 0.5f) else p.tag)
            .padding(start = 8.dp, end = 10.dp),
    ) {
        if (running) HassnSpinner(14.dp, Color.White) else HassnIcon("check", 14.dp, tint = p.text2)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (running) Color.White else p.text2, maxLines = 1)
    }
}

/** Button pill (spec §6.6 sm): primary = ink / textPrimaryOn, secondary = fillTertiary / textPrimary. */
@Composable
fun HassnChipButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val p = hassn
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(CircleShape)
            .background(if (primary) p.ink else p.soft)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (primary) p.onInk else p.text)
    }
}

/** Primary xl button: 48dp ink pill, labelLarge in textPrimaryOn. */
@Composable
fun HassnPrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val p = hassn
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(if (enabled) p.ink else p.ink.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
    ) { Text(label, style = MaterialTheme.typography.labelLarge, color = p.onInk) }
}

/** Empty state (spec §6.9): bodyLarge textSecondary, left-aligned at the 16dp margin. */
@Composable
fun HassnEmptyText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = hassn.text2, modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

/**
 * Drop-in replacement for Material `TopAppBar` / `CenterAlignedTopAppBar` on upstream sub-pages
 * (same parameter names), Hark sub-page header: navigation in a 44dp glass circle, centred
 * titleLarge 20/24 w500, actions in one 44dp glass capsule. `colors` / `scrollBehavior` are
 * accepted and ignored on purpose. Empty slots draw nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HassnAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = 60.dp,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val p = hassn
    CompositionLocalProvider(
        LocalContentColor provides p.text,
        LocalMinimumInteractiveComponentSize provides 44.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .heightIn(min = if (expandedHeight < 56.dp) 56.dp else expandedHeight)
                .padding(horizontal = 16.dp),
        ) {
            Box(Modifier.widthIn(min = 44.dp), contentAlignment = Alignment.CenterStart) {
                Box(Modifier.harkGlass(CircleShape, p.glassButton)) { navigationIcon() }
            }
            Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(color = p.text, textAlign = TextAlign.Center)) {
                    title()
                }
            }
            Box(Modifier.widthIn(min = 44.dp), contentAlignment = Alignment.CenterEnd) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.harkGlass(CircleShape, p.glassButton),
                    content = actions,
                )
            }
        }
    }
}

/** Thin glass capsule chip (chat header model pill / menus): 40dp, thinBg glass, optional icons. */
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
            .harkGlass(CircleShape, p.glassButton)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (label == null) 12.dp else 16.dp),
    ) {
        if (icon != null) HassnIcon(icon, 16.dp)
        if (label != null) {
            if (icon != null) Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 180.dp))
        }
        if (trailingIcon != null) { Spacer(Modifier.width(4.dp)); HassnIcon(trailingIcon, 12.dp, tint = p.text2) }
    }
}

// ─── toast (spec §6.8): r24, white @90 % (black @90 % dark), 1dp text @20 % border, 13sp text @85 %,
// sits above the composer, slides up 300 ms. Shown by HassnToast.show from anywhere. ───
object HassnToast {
    internal val current = kotlinx.coroutines.flow.MutableStateFlow<Pair<Long, String>?>(null)
    @Volatile internal var hostAttached = false

    /** Shows [text] in the app's toast; falls back to the system toast when no screen is up. */
    fun show(context: android.content.Context, text: CharSequence?, long: Boolean = false) {
        val msg = text?.toString() ?: return
        if (!hostAttached) {
            android.widget.Toast.makeText(context.applicationContext, msg, if (long) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        current.value = System.nanoTime() to msg
    }

    fun show(context: android.content.Context, resId: Int, long: Boolean = false) =
        show(context, context.getString(resId), long)
}

@Composable
fun HassnToastHost() {
    val item by HassnToast.current.collectAsState()
    // Attached only while this screen is resumed, so toasts from other activities (web apps,
    // share sheet) or from the background fall back to the system toast.
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            when (e) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> HassnToast.hostAttached = true
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> HassnToast.hostAttached = false
                else -> {}
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs); HassnToast.hostAttached = false }
    }
    var shown by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(item) {
        val it = item ?: return@LaunchedEffect
        shown = it.second
        visible = true
        kotlinx.coroutines.delay(if (it.second.length > 40) 3500 else 2200)
        visible = false
    }
    val dark = LocalChatPalette.current.isDark
    val textC = (if (dark) HkPaper else HkInk)
    Box(Modifier.fillMaxSize().navigationBarsPadding().imePadding(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 84.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 424.dp)
                    .shadow(24.dp, HarkCardShape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.35f))
                    .clip(HarkCardShape)
                    .background(if (dark) Color.Black.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f))
                    .border(1.dp, textC.copy(alpha = 0.2f), HarkCardShape)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            ) {
                HassnIcon("info", 16.dp, tint = textC.copy(alpha = 0.6f))
                Spacer(Modifier.width(10.dp))
                Text(shown.orEmpty(), fontSize = 13.sp, lineHeight = 18.sp, color = textC.copy(alpha = 0.85f), maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
