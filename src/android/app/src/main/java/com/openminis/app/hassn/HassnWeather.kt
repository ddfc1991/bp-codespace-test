package com.openminis.app.hassn

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * [Hassn-P1] Weather ambience behind the Hassn home shell, ported from Mines' WeatherAmbience
 * (Mines 38.8 v5d): one faint Canvas layer from the CURRENT weather — sun glow, stars, slow
 * clouds, fog veil, rain streaks, snow, thunder flash. ≤30 fps, only while RESUMED; a still frame
 * when system animations are off or battery saver is on.
 *
 * Data: open-meteo.com (no key) at the phone's own last known location. Hassn never asks for the
 * location permission just for this; without it (or without a recent fix) there is simply no
 * weather layer — never a guessed or sample weather.
 */
object HassnWeather {
    enum class Kind { NONE, SUN, STARS, CLOUDS, RAIN, SNOW, THUNDER, FOG }

    private const val PREFS = "hassn_weather"
    private const val MAX_AGE_MS = 60 * 60_000L
    private const val RETRY_MS = 5 * 60_000L

    private val _tick = MutableStateFlow(0)
    val tick: StateFlow<Int> = _tick.asStateFlow()

    private val http by lazy {
        OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
    }

    fun kindOf(code: Int?, day: Boolean): Kind = when (code) {
        null -> Kind.NONE
        0, 1 -> if (day) Kind.SUN else Kind.STARS
        2, 3 -> Kind.CLOUDS
        45, 48 -> Kind.FOG
        in 51..67, in 80..82 -> Kind.RAIN
        in 71..77, 85, 86 -> Kind.SNOW
        in 95..99 -> Kind.THUNDER
        else -> Kind.NONE
    }

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Current kind from the cache; NONE when there is no weather younger than 3 h. */
    fun currentKind(c: Context): Kind {
        val p = prefs(c)
        val at = p.getLong("at", 0L)
        if (at == 0L || System.currentTimeMillis() - at > 3 * MAX_AGE_MS) return Kind.NONE
        return kindOf(p.getInt("code", -1).takeIf { it >= 0 }, p.getBoolean("day", true))
    }

    private fun hasLocationPermission(c: Context): Boolean =
        ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    private fun lastFix(c: Context): Pair<Double, Double>? {
        if (!hasLocationPermission(c)) return null
        val lm = c.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val best = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { pr -> runCatching { lm.getLastKnownLocation(pr) }.getOrNull() }
            .maxByOrNull { it.time } ?: return null
        // a fix older than a day is not "where the phone is"
        if (System.currentTimeMillis() - best.time > 24 * 3600_000L) return null
        return best.latitude to best.longitude
    }

    /** Fetches when the cache is older than 1 h (at most one try per 5 min). Safe to call on every resume. */
    suspend fun refresh(c: Context): Unit = withContext(Dispatchers.IO) {
        val p = prefs(c)
        val now = System.currentTimeMillis()
        if (now - p.getLong("at", 0L) < MAX_AGE_MS) return@withContext
        if (now - p.getLong("try", 0L) < RETRY_MS) return@withContext
        p.edit().putLong("try", now).apply()
        val loc = lastFix(c) ?: return@withContext
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${loc.first}&longitude=${loc.second}" +
            "&current=weather_code,is_day&timezone=auto"
        val o = runCatching {
            http.newCall(Request.Builder().url(url).build()).execute().use { r ->
                if (r.isSuccessful) JSONObject(r.body?.string().orEmpty()) else null
            }
        }.getOrNull() ?: return@withContext
        val cur = o.optJSONObject("current") ?: return@withContext
        if (!cur.has("weather_code")) return@withContext
        p.edit()
            .putInt("code", cur.optInt("weather_code"))
            .putBoolean("day", cur.optInt("is_day", 1) == 1)
            .putLong("at", now)
            .apply()
        _tick.value++
    }
}

@Composable
fun HassnWeatherLayer(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val tick by HassnWeather.tick.collectAsState()
    @Suppress("DEPRECATION")
    val owner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { HassnWeather.refresh(ctx) }
    }
    val kind = remember(tick) { HassnWeather.currentKind(ctx) }
    if (kind == HassnWeather.Kind.NONE) return
    val still = remember {
        val noAnim = runCatching {
            android.provider.Settings.Global.getFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
        val saver = runCatching {
            (ctx.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager).isPowerSaveMode
        }.getOrDefault(false)
        noAnim || saver
    }
    val time = remember { mutableFloatStateOf(3.7f) }
    if (!still) LaunchedEffect(owner, kind) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = 0L
            var base = 0L
            while (true) {
                withFrameMillis { now ->
                    if (base == 0L) base = now - (time.floatValue * 1000).toLong()
                    if (now - last >= 33L) { last = now; time.floatValue = (now - base) / 1000f }
                }
            }
        }
    }
    val dark = hassn.dark // background is transparent in the Hassn theme
    Canvas(modifier) {
        val t = time.floatValue
        val w = size.width
        val h = size.height
        val sc = w / 1080f
        val twoPi = (2 * Math.PI).toFloat()
        fun rnd(i: Int, k: Int): Float {
            val x = kotlin.math.sin(i * 12.9898f + k * 78.233f) * 43758.547f
            return x - kotlin.math.floor(x)
        }
        fun rain(n: Int, alpha: Float) {
            val col = if (dark) Color.White.copy(alpha = alpha * 0.8f) else Color(0xFF6E6290).copy(alpha = alpha)
            for (i in 0 until n) {
                val speed = h / (0.9f + 0.5f * rnd(i, 1))
                val len = (26f + 22f * rnd(i, 2)) * sc
                val x0 = rnd(i, 3) * (w + 120f * sc)
                val y = ((rnd(i, 4) * h + t * speed) % (h + len)) - len
                val x = x0 - (y / h) * 90f * sc
                drawLine(col, Offset(x, y), Offset(x - 9f * sc, y + len), strokeWidth = 1.4f * sc, cap = StrokeCap.Round)
            }
        }
        when (kind) {
            HassnWeather.Kind.SUN -> {
                val c = Offset(w * 0.86f + 34f * sc * kotlin.math.sin(twoPi * t / 22f), h * 0.07f + 26f * sc * kotlin.math.cos(twoPi * t / 27f))
                val r = w * 0.62f
                val a = (if (dark) 0.16f else 0.34f) * (0.88f + 0.12f * kotlin.math.sin(twoPi * t / 6f))
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFC778).copy(alpha = a), Color(0xFFFFB27A).copy(alpha = a * 0.45f), Color.Transparent), center = c, radius = r), radius = r, center = c)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1C8).copy(alpha = a * 0.9f), Color.Transparent), center = c, radius = r * 0.28f), radius = r * 0.28f, center = c)
            }
            HassnWeather.Kind.STARS -> {
                val col = if (dark) Color.White else Color(0xFFC9963E)
                for (i in 0 until 26) {
                    val x = rnd(i, 1) * w
                    val y = rnd(i, 2) * h * 0.42f
                    val tw = 0.5f + 0.5f * kotlin.math.sin(twoPi * t / (2.4f + 2f * rnd(i, 3)) + i)
                    drawCircle(col.copy(alpha = (if (dark) 0.55f else 0.45f) * (0.25f + 0.75f * tw)), radius = (1.4f + 1.6f * rnd(i, 4)) * sc * 1.6f, center = Offset(x, y))
                }
            }
            HassnWeather.Kind.CLOUDS, HassnWeather.Kind.FOG -> {
                val col = if (dark) Color(0xFFD8D0E0) else Color.White
                val a = if (dark) 0.07f else 0.30f
                for (i in 0 until 4) {
                    val cw = (320f + 160f * rnd(i, 1)) * sc
                    val speed = (14f + 10f * rnd(i, 2)) * sc
                    val x = ((rnd(i, 3) * (w + cw) + t * speed) % (w + 2 * cw)) - cw
                    val y = h * (0.08f + 0.17f * i + 0.05f * rnd(i, 4))
                    for (k in 0 until 4) {
                        val px = x + cw * (k / 3f)
                        val py = y + (if (k == 1 || k == 2) -0.16f else 0f) * cw
                        val rr = cw * (if (k == 1 || k == 2) 0.30f else 0.22f)
                        drawCircle(Brush.radialGradient(listOf(col.copy(alpha = a), col.copy(alpha = a * 0.5f), Color.Transparent), center = Offset(px, py), radius = rr), radius = rr, center = Offset(px, py))
                    }
                }
                if (kind == HassnWeather.Kind.FOG) {
                    for (b in 0 until 3) {
                        val y = h * (0.25f + 0.25f * b) + 40f * sc * kotlin.math.sin(twoPi * t / (18f + 4f * b) + b)
                        val bh = h * 0.22f
                        drawRect(
                            Brush.verticalGradient(listOf(Color.Transparent, col.copy(alpha = if (dark) 0.08f else 0.26f), Color.Transparent), startY = y - bh / 2, endY = y + bh / 2),
                            topLeft = Offset(0f, y - bh / 2), size = Size(w, bh),
                        )
                    }
                }
            }
            HassnWeather.Kind.RAIN -> rain(70, 0.22f)
            HassnWeather.Kind.THUNDER -> {
                rain(90, 0.24f)
                val ph = t % 9f
                if (ph < 0.35f) {
                    val f = if (ph < 0.12f) ph / 0.12f else (0.35f - ph) / 0.23f
                    drawRect(Color.White.copy(alpha = (if (dark) 0.10f else 0.22f) * f.coerceIn(0f, 1f)))
                }
            }
            HassnWeather.Kind.SNOW -> {
                for (i in 0 until 46) {
                    val speed = h / (10f + 6f * rnd(i, 1))
                    val r = (2.2f + 2.4f * rnd(i, 2)) * sc * 1.4f
                    val y = ((rnd(i, 3) * h + t * speed) % (h + 20f)) - 10f
                    val x = rnd(i, 4) * w + 22f * sc * kotlin.math.sin(twoPi * t / (5f + 3f * rnd(i, 5)) + i)
                    if (!dark) drawCircle(Color(0xFF9C8CB8).copy(alpha = 0.22f), radius = r + 1.2f * sc, center = Offset(x, y))
                    drawCircle(Color.White.copy(alpha = if (dark) 0.55f else 0.92f), radius = r, center = Offset(x, y))
                }
            }
            HassnWeather.Kind.NONE -> {}
        }
    }
}
