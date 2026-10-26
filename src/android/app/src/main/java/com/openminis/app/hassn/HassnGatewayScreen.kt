package com.openminis.app.hassn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.openminis.app.R
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * [Hassn-P1] Settings → 智能网关. Base URL + API key (typed in-app, encrypted at rest) and the
 * model groups the gateway reports at `/v1/models`. Nothing here is sample data: before the
 * first successful fetch the list is empty and says so.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HassnGatewayScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var baseUrl by remember { mutableStateOf(HassnGateway.baseUrl(ctx)) }
    var keyInput by remember { mutableStateOf("") }
    var masked by remember { mutableStateOf(HassnGateway.maskedKey(ctx)) }
    var models by remember { mutableStateOf(HassnGateway.cachedModels(ctx)) }
    var fetchedAt by remember { mutableStateOf(HassnGateway.cachedAt(ctx)) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(setOf<String>()) }

    val savedText = stringResource(R.string.hassn_gateway_saved)
    val noKeyText = stringResource(R.string.hassn_gateway_need_key)
    val failText = stringResource(R.string.hassn_gateway_fetch_failed)
    val okFmt = stringResource(R.string.hassn_gateway_fetch_ok)

    fun refresh() {
        loading = true
        message = null
        scope.launch {
            val r = HassnGateway.fetchModels(ctx)
            loading = false
            r.onSuccess {
                models = it
                fetchedAt = HassnGateway.cachedAt(ctx)
                message = String.format(okFmt, it.size)
            }.onFailure {
                message = if (it.message == "NO_KEY") noKeyText else "$failText: ${it.message ?: it.javaClass.simpleName}"
            }
        }
    }

    Scaffold(
        topBar = {
            HassnAppBar(
                title = { Text(stringResource(R.string.hassn_gateway_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(com.openminis.app.hassn.HassnIcons.get("back", 2f), contentDescription = stringResource(R.string.settings_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.hassn_gateway_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text(stringResource(R.string.hassn_gateway_base_url)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = { baseUrl = HassnGateway.DEFAULT_BASE_URL }) {
                Text(stringResource(R.string.hassn_gateway_reset_default))
            }

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text(stringResource(R.string.hassn_gateway_api_key)) },
                placeholder = {
                    Text(masked?.let { stringResource(R.string.hassn_gateway_key_saved, it) }
                        ?: stringResource(R.string.hassn_gateway_key_none))
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.hassn_gateway_key_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val oldBase = HassnGateway.baseUrl(ctx)
                    HassnGateway.setBaseUrl(ctx, baseUrl)
                    val keyChanged = keyInput.isNotBlank()
                    if (keyChanged) HassnGateway.setApiKey(ctx, keyInput)
                    if (keyChanged || oldBase != HassnGateway.baseUrl(ctx)) {
                        HassnGateway.clearCache(ctx)
                        models = emptyList()
                        fetchedAt = 0L
                    }
                    keyInput = ""
                    baseUrl = HassnGateway.baseUrl(ctx)
                    masked = HassnGateway.maskedKey(ctx)
                    message = savedText
                    if (HassnGateway.hasApiKey(ctx)) refresh()
                }) { Text(stringResource(R.string.hassn_gateway_save)) }
                if (masked != null) {
                    OutlinedButton(onClick = {
                        HassnGateway.clearApiKey(ctx)
                        HassnGateway.clearCache(ctx)
                        masked = null
                        models = emptyList()
                        fetchedAt = 0L
                    }) { Text(stringResource(R.string.hassn_gateway_clear_key)) }
                }
            }

            message?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.hassn_gateway_groups),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    TextButton(onClick = { refresh() }) { Text(stringResource(R.string.hassn_gateway_refresh)) }
                }
            }
            if (fetchedAt > 0) {
                Text(
                    stringResource(
                        R.string.hassn_gateway_fetched_at,
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(fetchedAt)),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val groups = remember(models) { HassnGateway.group(models) }
            if (groups.isEmpty()) {
                Text(
                    stringResource(R.string.hassn_gateway_groups_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            groups.forEach { g ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expanded = if (g.name in expanded) expanded - g.name else expanded + g.name
                                },
                        ) {
                            Text(g.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(
                                stringResource(R.string.hassn_gateway_group_count, g.models.size),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (g.name in expanded) {
                            Spacer(Modifier.height(6.dp))
                            g.models.forEach { m ->
                                Text(m.id, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
