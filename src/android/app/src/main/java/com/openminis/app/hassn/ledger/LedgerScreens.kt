package com.openminis.app.hassn.ledger

import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openminis.app.hassn.ledger.LedgerCore.PStatus
import com.openminis.app.hassn.ledger.LedgerCore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId

/**
 * [Hassn-P1 临时邮箱台账] List (mockup 14 / 14c) and mailbox detail (14b). Plain Material 3 pieces
 * in the same rounded-card style as the other Hassn screens. Only real ledger data is drawn: an
 * empty ledger says so.
 */

private val Green = Color(0xFF2E7D32)
private val Amber = Color(0xFFB26A00)
private val Red = Color(0xFFC62828)
private val Blue = Color(0xFF1A56C4)

@Composable
private fun LedgerCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) { Column(Modifier.padding(vertical = 4.dp)) { content() } }
}

@Composable
private fun SectionHeader(text: String, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Chip(text: String, fg: Color, bg: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun SourceChip(source: Source) {
    if (source == Source.GMAIL_ALIAS) Chip(source.tag, Blue, Blue.copy(alpha = 0.12f))
    else Chip(source.tag, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
}

@Composable
private fun StatusPill(st: PStatus) {
    val c = when (st) {
        PStatus.VERIFIED -> Green
        PStatus.PENDING, PStatus.NEEDS_USER -> Amber
        PStatus.FAILED -> Red
    }
    Chip(st.zh, c, c.copy(alpha = 0.12f))
}

private fun copy(c: Context, text: String, sensitive: Boolean, toast: String) {
    val cm = c.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText("Hassn", text)
    if (sensitive) {
        // Android 13+: keep the copied password out of the clipboard preview
        clip.description.extras = PersistableBundle().apply { putBoolean("android.content.extra.IS_SENSITIVE", true) }
    }
    cm.setPrimaryClip(clip)
    Toast.makeText(c, toast, Toast.LENGTH_SHORT).show()
}

private fun zone(): ZoneId = ZoneId.systemDefault()

// ───────────────────────── list (14 / 14c) ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(onBack: () -> Unit, onOpenBox: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    remember { LedgerStore.init(ctx.applicationContext) }
    val state by LedgerStore.state.collectAsState()

    var query by remember { mutableStateOf("") }
    var source by remember { mutableStateOf(Source.MOAKT) }
    var domain by remember { mutableStateOf(LedgerCore.MOAKT_DOMAINS.first()) }
    var domains by remember { mutableStateOf(LedgerCore.MOAKT_DOMAINS) }
    var srcMenu by remember { mutableStateOf(false) }
    var domMenu by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var askGmail by remember { mutableStateOf(false) }

    LaunchedEffect(source) {
        domains = when (source) {
            Source.MAILTM -> withContext(Dispatchers.IO) { MailTmSource.domains() }
            Source.MOAKT -> LedgerCore.MOAKT_DOMAINS
            Source.GMAIL_ALIAS -> listOf("gmail.com")
            Source.SHARED_GMAIL -> emptyList()
        }
        domain = domains.firstOrNull().orEmpty()
    }

    fun register() {
        if (source == Source.GMAIL_ALIAS && LedgerStore.gmailBase(ctx) == null) { askGmail = true; return }
        busy = true; message = null
        scope.launch {
            val r = LedgerStore.newBox(ctx.applicationContext, source, domain.ifBlank { null })
            busy = false
            r.onSuccess { onOpenBox(it.id) }.onFailure { message = it.message ?: "注册没成功" }
        }
    }

    if (askGmail) {
        var draft by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { askGmail = false },
            title = { Text("你的 Gmail 地址") },
            text = {
                Column {
                    OutlinedTextField(value = draft, onValueChange = { draft = it.trim() }, singleLine = true, placeholder = { Text("name@gmail.com") })
                    Spacer(Modifier.height(6.dp))
                    Text("别名会是 name+标签@gmail.com，邮件仍进你的 Gmail。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (LedgerCore.gmailAlias(draft, "x") == null) {
                        Toast.makeText(ctx, "请填 Gmail 地址", Toast.LENGTH_SHORT).show()
                    } else {
                        LedgerStore.setGmailBase(ctx, draft); askGmail = false; register()
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { askGmail = false }) { Text("取消") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("临时邮箱台账") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
            )
        },
    ) { padding ->
        val rows = LedgerCore.rows(state, query)
        val (nb, np) = LedgerCore.totals(state)
        val now = System.currentTimeMillis()
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize().padding(padding).navigationBarsPadding(),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("搜索邮箱或平台") },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1.1f)) {
                            SelectorPill("来源", source.label) { srcMenu = true }
                            DropdownMenu(expanded = srcMenu, onDismissRequest = { srcMenu = false }) {
                                Source.entries.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(if (s.ready) s.label else s.label + "（即将支持）") },
                                        enabled = s.ready,
                                        onClick = { source = s; srcMenu = false },
                                        trailingIcon = { if (s == source) Icon(Icons.Filled.Check, contentDescription = null) },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.weight(1f)) {
                            SelectorPill("域名", if (domain.isBlank()) "—" else "@$domain") { if (domains.size > 1) domMenu = true }
                            DropdownMenu(expanded = domMenu, onDismissRequest = { domMenu = false }) {
                                Text(
                                    "${source.label.substringAfter("· ")} 域名 · ${domains.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                )
                                domains.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text("@$d", fontWeight = if (d == domain) FontWeight.SemiBold else FontWeight.Normal) },
                                        onClick = { domain = d; domMenu = false },
                                        trailingIcon = { if (d == domain) Icon(Icons.Filled.Check, contentDescription = null) },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = { register() },
                            enabled = !busy && source.ready,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            else Text("注册", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text("密码自动生成存入保管库 · 到期前自动延时", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            message?.let { m ->
                item { Text(m, color = Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) }
            }
            item { SectionHeader("我的邮箱", if (nb > 0) "$nb 个邮箱 · $np 个平台" else null) }
            item {
                LedgerCard {
                    if (rows.isEmpty()) {
                        Text(
                            if (state.boxes.isEmpty()) "还没有邮箱。选好来源点「注册」，或在聊天里说「用临时邮箱注册 …」。" else "没有匹配的邮箱",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    rows.forEachIndexed { i, row ->
                        BoxRowItem(row, LedgerCore.whenLabel(row.lastAt, now, zone())) { onOpenBox(row.box.id) }
                        if (i < rows.size - 1) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorPill(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun BoxRowItem(row: LedgerCore.BoxRow, whenText: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon: ImageVector = if (row.box.source == Source.GMAIL_ALIAS) Icons.Outlined.AlternateEmail else Icons.Outlined.MailOutline
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(LedgerCore.maskBox(row.box.address), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SourceChip(row.box.source)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${row.platforms} 个平台" + (if (whenText.isNotBlank()) " · $whenText" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ───────────────────────── detail (14b) ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerBoxScreen(boxId: String, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    remember { LedgerStore.init(ctx.applicationContext) }
    val state by LedgerStore.state.collectAsState()
    val box = state.boxes.firstOrNull { it.id == boxId }

    var refreshing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var revealed by remember { mutableStateOf<String?>(null) }  // signup id whose password is shown
    var pendingReveal by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var addPlatform by remember { mutableStateOf(false) }

    // hide a revealed password again after 30 s
    LaunchedEffect(revealed) { if (revealed != null) { delay(30_000L); revealed = null } }

    val unlock = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) revealed = pendingReveal
        pendingReveal = null
    }

    fun askReveal(signupId: String) {
        if (revealed == signupId) { revealed = null; return }
        val km = ctx.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        @Suppress("DEPRECATION")
        val intent = if (km != null && km.isDeviceSecure) km.createConfirmDeviceCredentialIntent("查看密码", "用指纹或锁屏密码确认") else null
        if (intent == null) {
            // no screen lock set: same as anyone holding the unlocked phone
            revealed = signupId
        } else {
            pendingReveal = signupId
            runCatching { unlock.launch(intent) }.onFailure { pendingReveal = null }
        }
    }

    if (box == null) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("邮箱详情") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } })
        }) { p -> Text("这个邮箱已经不在台账里", modifier = Modifier.padding(p).padding(20.dp)) }
        return
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这个邮箱？") },
            text = { Text("会从台账删掉这个邮箱、它注册过的平台记录和保存的密码。平台上的账号本身不会被删除。") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; LedgerStore.deleteBox(ctx, box.id); onBack() }) { Text("删除", color = Red) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } },
        )
    }

    if (addPlatform) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addPlatform = false },
            title = { Text("新注册平台") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("平台名称") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = url, onValueChange = { url = it.trim() }, singleLine = true, label = { Text("网址") })
                    Spacer(Modifier.height(8.dp))
                    Text("会生成一个强密码存进保管库。也可以在聊天里说「用这个临时邮箱注册 …」，Hassn 会自己填写。",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(enabled = url.isNotBlank(), onClick = {
                    LedgerStore.recordSignup(ctx, box.id, name, url); addPlatform = false
                }) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { addPlatform = false }) { Text("取消") } },
        )
    }

    val now = System.currentTimeMillis()
    val signups = LedgerCore.signupsOf(state, box.id)
    val received = LedgerCore.receivedOf(state, box.id)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("邮箱详情") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } })
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Red),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("删除", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { addPlatform = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111), contentColor = Color.White),
                    modifier = Modifier.weight(2f).height(52.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp)); Text("新注册平台", fontWeight = FontWeight.SemiBold)
                }
            }
        },
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), modifier = Modifier.fillMaxSize().padding(padding)) {
            // box card
            item {
                LedgerCard {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (box.source == Source.GMAIL_ALIAS) Icons.Outlined.AlternateEmail else Icons.Outlined.MailOutline, contentDescription = null)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SourceChip(box.source)
                                val exp = LedgerCore.expiryLine(box.expiresAt, now)
                                if (exp.isNotBlank()) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(exp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(LedgerCore.maskBox(box.address), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (box.autoExtend && box.source.extendable) {
                            Text(if (box.lastNote.isBlank()) "保活中" else "保活异常", style = MaterialTheme.typography.bodySmall,
                                color = if (box.lastNote.isBlank()) Green else Amber)
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        SmallAction(Icons.Outlined.ContentCopy, "复制") { copy(ctx, box.address, false, "已复制邮箱地址") }
                        Spacer(Modifier.width(8.dp))
                        SmallAction(Icons.Outlined.Refresh, if (refreshing) "收码中…" else "重新收码") {
                            if (!refreshing) {
                                refreshing = true; message = null
                                scope.launch {
                                    val r = LedgerStore.refreshCodes(ctx.applicationContext, box.id)
                                    refreshing = false
                                    message = r.fold({ if (it.isEmpty()) "没有新的验证码或链接" else null }, { it.message ?: "收码失败" })
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text("创建于 " + LedgerCore.whenLabel(box.createdAt, now, zone()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (box.lastNote.isNotBlank()) {
                        Text(box.lastNote, style = MaterialTheme.typography.bodySmall, color = Amber, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
                    }
                }
            }
            message?.let { m -> item { Text(m, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) } }

            // platforms
            item { SectionHeader("已注册平台 · ${signups.size}") }
            item {
                LedgerCard {
                    if (signups.isEmpty()) {
                        Text("还没有平台。点下面「新注册平台」，或让 Hassn 用这个邮箱去注册。", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                    }
                    signups.forEachIndexed { i, su ->
                        val title = LedgerCore.platformTitle(su.platform, su.url)
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tileColor(title)),
                                contentAlignment = Alignment.Center,
                            ) { Text(LedgerCore.letter(title), color = Color.White, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    Spacer(Modifier.width(6.dp))
                                    StatusPill(su.status)
                                }
                                Text(
                                    listOf(su.host, LedgerCore.whenLabel(su.createdAt, now, zone()) + "注册").filter { it.isNotBlank() }.joinToString(" · ") +
                                        (if (su.note.isNotBlank()) " · " + su.note else ""),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                                if (revealed == su.id) {
                                    val pw = LedgerStore.passwordOf(ctx, su.id).orEmpty()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pw, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f, fill = false))
                                        TextButton(onClick = { copy(ctx, pw, true, "已复制密码") }) { Text("复制") }
                                    }
                                }
                            }
                            if (su.hasPassword) {
                                if (revealed != su.id) Text(LedgerCore.PASSWORD_DOTS, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { askReveal(su.id) }) {
                                    Icon(Icons.Outlined.Fingerprint, contentDescription = "查看密码")
                                }
                            }
                        }
                        if (i < signups.size - 1) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            // codes / links
            if (received.isNotEmpty()) {
                item { SectionHeader("最近收到") }
                item {
                    LedgerCard {
                        received.forEachIndexed { i, r ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (r.kind == LedgerCore.Kind.CODE) Icons.Outlined.Shield else Icons.Outlined.Link, contentDescription = null)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (r.kind == LedgerCore.Kind.CODE) "验证码 ${r.value}" else "邮箱验证链接",
                                        style = MaterialTheme.typography.bodyLarge, fontWeight = if (r.kind == LedgerCore.Kind.CODE) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                    Text(
                                        listOf(LedgerCore.whenLabel(r.at, now, zone()), r.from.substringBefore('<').trim().take(24)).filter { it.isNotBlank() }.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                    )
                                }
                                when {
                                    r.used -> Chip("已使用", Green, Green.copy(alpha = 0.12f))
                                    r.kind == LedgerCore.Kind.CODE -> SmallAction(Icons.Outlined.ContentCopy, "复制") { copy(ctx, r.value, false, "已复制验证码") }
                                    else -> SmallAction(Icons.Outlined.Link, "打开") {
                                        runCatching {
                                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.value)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                            LedgerStore.markUsed(ctx, r)
                                        }
                                    }
                                }
                            }
                            if (i < received.size - 1) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // keep-alive
            item { SectionHeader("保活") }
            item {
                LedgerCard {
                    SwitchRow(
                        icon = Icons.Outlined.Schedule,
                        title = "自动延时",
                        subtitle = when {
                            !box.source.extendable -> "这个来源不需要延时"
                            box.source == Source.MOAKT -> "到期前 20 分钟自动续期（Moakt 每次 1 小时）"
                            else -> "每天重新登录一次，保持邮箱不被回收"
                        },
                        checked = box.autoExtend && box.source.extendable,
                        enabled = box.source.extendable,
                    ) { LedgerStore.setKeepAlive(ctx, box.id, autoExtend = it) }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SwitchRow(
                        icon = Icons.Outlined.Refresh,
                        title = "探针保活",
                        subtitle = if (box.source.readable) "定时打开收件箱，防止被回收" else "这个来源还不能自动读取",
                        checked = box.probe && box.source.readable,
                        enabled = box.source.readable,
                    ) { LedgerStore.setKeepAlive(ctx, box.id, probe = it) }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable(enabled = box.source.readable) { LedgerStore.setKeepAlive(ctx, box.id, probeEveryH = LedgerCore.cycleProbeHours(box.probeEveryH)) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("间隔", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (box.lastProbeAt == 0L) "还没有探活" else "上次 " + LedgerCore.whenLabel(box.lastProbeAt, now, zone()) + " · " + (if (box.lastProbeOk) "正常" else "失败"),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text("每 ${box.probeEveryH} 小时", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

private val TILE_COLORS = listOf(Color(0xFF5B5BD6), Color(0xFFF2542D), Color(0xFF111111), Color(0xFFF38020), Color(0xFF2E7D32), Color(0xFF1A73E8), Color(0xFF8E24AA))

private fun tileColor(title: String): Color = TILE_COLORS[Math.floorMod(title.lowercase().hashCode(), TILE_COLORS.size)]
