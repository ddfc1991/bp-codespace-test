package com.openminis.app.hassn

import android.content.Context
import com.openminis.app.util.EncryptedPrefsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * [Hassn-P1] 智能网关 settings: the user's own OpenAI-compatible gateway (new-api).
 *
 * - Base URL: plain prefs, defaults to [DEFAULT_BASE_URL].
 * - API key: typed by the user in-app, stored only in the app's existing encrypted
 *   store ([EncryptedPrefsFactory], Android Keystore AES-GCM). Never hardcoded, never logged.
 * - Model groups: fetched live from `<base>/models` and grouped by `owned_by`. The last
 *   successful list is cached so the screen can show it offline, always with its fetch time.
 */
object HassnGateway {
    const val DEFAULT_BASE_URL = "https://newapi.ydn44086.dpdns.org/v1"

    private const val PREFS = "hassn_gateway"
    private const val SECRETS = "hassn_gateway_secrets"
    private const val KEY_BASE = "base_url"
    private const val KEY_API = "api_key"
    private const val KEY_MODELS = "models_json"
    private const val KEY_MODELS_AT = "models_at"

    data class ModelInfo(val id: String, val ownedBy: String)

    /** One group of models as the gateway reports them (by `owned_by`). */
    data class ModelGroup(val name: String, val models: List<ModelInfo>)

    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun secrets(c: Context) = EncryptedPrefsFactory.safeCreate(c.applicationContext, SECRETS)

    fun baseUrl(c: Context): String =
        prefs(c).getString(KEY_BASE, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL

    fun setBaseUrl(c: Context, url: String) {
        val v = url.trim().trimEnd('/')
        prefs(c).edit().apply { if (v.isEmpty()) remove(KEY_BASE) else putString(KEY_BASE, v) }.apply()
    }

    fun hasApiKey(c: Context): Boolean = !apiKey(c).isNullOrBlank()

    /** The key the user typed. Only for building the Authorization header; never show it in full. */
    internal fun apiKey(c: Context): String? = secrets(c).getString(KEY_API, null)

    fun setApiKey(c: Context, key: String) {
        val v = key.trim()
        secrets(c).edit().apply { if (v.isEmpty()) remove(KEY_API) else putString(KEY_API, v) }.apply()
    }

    fun clearApiKey(c: Context) = secrets(c).edit().remove(KEY_API).apply()

    /** "sk-a…9xQ2" style preview so the user can tell which key is saved. */
    fun maskedKey(c: Context): String? = apiKey(c)?.takeIf { it.isNotBlank() }?.let { mask(it) }

    fun mask(k: String): String = if (k.length <= 8) "•".repeat(k.length) else k.take(4) + "…" + k.takeLast(4)

    /** `<base>/models`, accepting a base with or without the trailing /v1. */
    fun modelsUrl(base: String): String {
        val b = base.trim().trimEnd('/')
        return if (b.endsWith("/v1")) "$b/models" else "$b/v1/models"
    }

    fun parseModels(body: String): List<ModelInfo> {
        val arr: JSONArray = JSONObject(body).optJSONArray("data") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            ModelInfo(id, o.optString("owned_by").ifBlank { "other" })
        }
    }

    fun group(models: List<ModelInfo>): List<ModelGroup> =
        models.groupBy { it.ownedBy }
            .map { (k, v) -> ModelGroup(k, v.sortedBy { it.id.lowercase() }) }
            .sortedWith(compareByDescending<ModelGroup> { it.models.size }.thenBy { it.name })

    /** Error text from a new-api / OpenAI style error body, or null. */
    fun errorMessage(body: String?): String? = runCatching {
        val o = JSONObject(body ?: return null)
        o.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
            ?: o.optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()

    /** Live fetch of `<base>/models`; caches the result on success. */
    suspend fun fetchModels(c: Context): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        val key = apiKey(c)
        if (key.isNullOrBlank()) return@withContext Result.failure(IllegalStateException("NO_KEY"))
        runCatching {
            val req = Request.Builder()
                .url(modelsUrl(baseUrl(c)))
                .header("Authorization", "Bearer $key")
                .header("Accept", "application/json")
                .build()
            http.newCall(req).execute().use { r ->
                val body = r.body?.string().orEmpty()
                if (!r.isSuccessful) {
                    throw IllegalStateException("HTTP ${r.code}" + (errorMessage(body)?.let { ": $it" } ?: ""))
                }
                val list = parseModels(body)
                prefs(c).edit()
                    .putString(KEY_MODELS, body)
                    .putLong(KEY_MODELS_AT, System.currentTimeMillis())
                    .apply()
                list
            }
        }
    }

    fun cachedModels(c: Context): List<ModelInfo> =
        prefs(c).getString(KEY_MODELS, null)?.let { runCatching { parseModels(it) }.getOrNull() } ?: emptyList()

    /** When [cachedModels] was fetched (epoch ms), or 0 when never. */
    fun cachedAt(c: Context): Long = prefs(c).getLong(KEY_MODELS_AT, 0L)

    /** Drops the cached list (e.g. after the base URL or key changes, so stale data is never shown as current). */
    fun clearCache(c: Context) = prefs(c).edit().remove(KEY_MODELS).remove(KEY_MODELS_AT).apply()
}
