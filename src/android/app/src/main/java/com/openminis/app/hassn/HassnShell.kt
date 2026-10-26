package com.openminis.app.hassn

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.openminis.app.hassn.icons.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.openminis.app.R
import com.openminis.app.data.db.ChatSessionEntity
import com.openminis.app.data.db.ProjectEntity
import com.openminis.app.data.db.isSubagentSession
import com.openminis.app.data.repository.ChatRepository
import com.openminis.app.data.repository.MCPRepository
import com.openminis.app.data.repository.MemoryRepository
import com.openminis.app.data.repository.ProviderRepository
import com.openminis.app.data.repository.SkillRepository
import com.openminis.app.scheduled.ScheduledTask
import com.openminis.app.scheduled.ScheduledTaskStore
import com.openminis.app.ui.navigation.ChatSplitScaffoldRoute
import com.openminis.app.ui.navigation.Routes
import com.openminis.app.ui.navigation.newDraftSessionId
import com.openminis.app.ui.navigation.safeNavigate
import java.text.DateFormat
import java.util.Date

/**
 * [Hassn-P1] Top-level shell in the official-Hark layout (approved v2 mockups 01/02/03/07):
 * [glass menu/plus] [主页 · 聊天 · 动态 · 项目] [glass user → 设置] over the app-wide sky.
 * Every tab reads real app data. 聊天 shows the latest chat itself (as in mockup 02); the menu
 * button opens the full chat history list.
 *
 * Launch extras (used by the CI screenshot job, harmless otherwise): `hassn_tab` 0..3 picks a
 * tab, `hassn_open` = settings | ledger | ledger_box | providers | model_groups | skills | mcp | memory |
 * scheduled | chats | gateway opens that page.
 */
@Composable
fun HassnShell(
    navController: NavHostController,
    chatRepository: ChatRepository,
    providerRepository: ProviderRepository,
    memoryRepository: MemoryRepository?,
    skillRepository: SkillRepository?,
    mcpRepository: MCPRepository?,
) {
    var tab by HassnShellState.tab
    BackHandler(enabled = tab != 0) { tab = 0 }
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = (ctx as? android.app.Activity)?.intent ?: return@LaunchedEffect
        val t = intent.getIntExtra("hassn_tab", -1)
        if (t in 0..3) tab = t
        // CI shots: sample conversation in memory only (see ChatViewModel.hassnSeedDemoChat)
        if (intent.getBooleanExtra("hassn_demo_chat", false)) {
            HassnShellState.demoChatPending = true
            tab = 1
            intent.removeExtra("hassn_demo_chat")
        }
        // CI shots: sample ledger in memory (never saved) + open the domain dropdown
        if (intent.getBooleanExtra("hassn_demo_ledger", false)) {
            com.openminis.app.hassn.ledger.LedgerStore.seedDemo(ctx.applicationContext)
            intent.removeExtra("hassn_demo_ledger")
        }
        if (intent.getBooleanExtra("hassn_ledger_dropdown", false)) {
            HassnShellState.ledgerDropdownPending = true
            intent.removeExtra("hassn_ledger_dropdown")
        }
        val open = intent.getStringExtra("hassn_open")
        val route = when (open) {
            "settings" -> Routes.SETTINGS
            "ledger" -> Routes.HASSN_LEDGER
            // first saved mailbox; with none the detail page shows its empty state
            "ledger_box" -> Routes.hassnLedgerBox(
                com.openminis.app.hassn.ledger.LedgerStore.state.value.boxes.firstOrNull()?.id ?: "none",
            )
            "providers" -> Routes.PROVIDER_LIST
            "model_groups" -> Routes.MODEL_GROUPS
            "skills" -> Routes.SKILLS
            "mcp" -> Routes.MCP
            "memory" -> Routes.MEMORY
            "scheduled" -> Routes.SCHEDULED_TASKS
            "chats" -> Routes.HASSN_CHATS
            "gateway" -> Routes.HASSN_GATEWAY
            else -> null
        }
        // sub-pages open on top of 设置 so back behaves like a real visit
        if (route != null) {
            // wait until the start destination is RESUMED; safeNavigate drops calls before that
            kotlinx.coroutines.delay(1200)
            runCatching {
                if (route != Routes.SETTINGS && route != Routes.HASSN_CHATS) navController.navigate(Routes.SETTINGS)
                navController.navigate(route)
            }
        }
        intent.removeExtra("hassn_tab")
        intent.removeExtra("hassn_open")
    }

    val openChat: (String) -> Unit = { id -> navController.safeNavigate(Routes.chat(id)) }
    val openHistory: () -> Unit = { navController.safeNavigate(Routes.HASSN_CHATS) }
    val labels = listOf(
        stringResource(R.string.hassn_tab_home),
        stringResource(R.string.hassn_tab_chat),
        stringResource(R.string.hassn_tab_feed),
        stringResource(R.string.hassn_tab_projects),
    )

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        HassnTopBar(
            labels = labels,
            selected = tab,
            onSelect = { tab = it },
            leftIcon = if (tab == 3) "plus" else "menu",
            onLeft = { if (tab == 3) openChat(newDraftSessionId()) else openHistory() },
            onProfile = { navController.safeNavigate(Routes.SETTINGS) },
            profileDescription = stringResource(R.string.hassn_profile_settings),
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                1 -> HassnChatTab(navController, chatRepository, providerRepository, memoryRepository, skillRepository, mcpRepository) { tab = 0 }
                2 -> HassnFeedTab(chatRepository, openChat)
                3 -> HassnProjectsTab(chatRepository, openChat)
                else -> HassnHomeTab(
                    chatRepository = chatRepository,
                    openChat = openChat,
                    openGateway = { navController.safeNavigate(Routes.HASSN_GATEWAY) },
                    openScheduled = { navController.safeNavigate(Routes.SCHEDULED_TASKS) },
                    openLedger = { navController.safeNavigate(Routes.HASSN_LEDGER) },
                )
            }
        }
    }
}

/** 聊天 tab: the most recent chat (or a fresh draft) rendered directly under the shell bar. */
@Composable
private fun HassnChatTab(
    navController: NavHostController,
    chatRepository: ChatRepository,
    providerRepository: ProviderRepository,
    memoryRepository: MemoryRepository?,
    skillRepository: SkillRepository?,
    mcpRepository: MCPRepository?,
    onLeave: () -> Unit,
) {
    val ctx = LocalContext.current
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = null)
    var sessionId by rememberSaveable { mutableStateOf<String?>(null) }
    val loaded = sessions ?: return
    if (sessionId == null) {
        sessionId = loaded.filter { !it.isSubagentSession }.maxByOrNull { it.updatedAt }?.id ?: newDraftSessionId()
    }
    val sid = sessionId ?: return
    val browseProject = { project: ProjectEntity ->
        val rootfs = com.openminis.app.sandbox.RootfsManager.getInstance(ctx.applicationContext)
        val hostDir = rootfs.getProjectDir(project.name, project.linuxPath, project.id)
        com.openminis.app.ui.navigation.FilePreviewHolder.fileBrowserViewModel = com.openminis.app.ui.sandbox.FileBrowserViewModel(
            rootPath = hostDir,
            rootLabel = project.name,
            displayLinuxPrefix = project.linuxPath ?: "/var/hark/projects/${project.name}",
        )
        navController.safeNavigate(Routes.FILE_BROWSER)
    }
    androidx.compose.runtime.key(sid) {
        com.openminis.app.ui.chat.ChatScreen(
            sessionId = sid,
            chatRepository = chatRepository,
            providerRepository = providerRepository,
            memoryRepository = memoryRepository,
            skillRepository = skillRepository,
            mcpRepository = mcpRepository,
            onBack = onLeave,
            isTwoPane = true, // no back arrow: the shell's tabs are the navigation here
            onNewChat = { sessionId = newDraftSessionId() },
            onOpenTerminal = { navController.safeNavigate(Routes.terminal(sessionId = sid)) },
            onOpenTerminalWithCommand = { cmd -> navController.safeNavigate(Routes.terminal(initCommand = cmd, sessionId = sid)) },
            onMoveToSession = { target -> sessionId = target },
            onBrowseChatFiles = { navController.safeNavigate(Routes.chatFiles(sid)) },
            onBrowseProjectFiles = browseProject,
            onPreviewAttachment = { item ->
                com.openminis.app.ui.navigation.FilePreviewHolder.currentItem = item
                navController.safeNavigate(Routes.FILE_PREVIEW)
            },
            onModelGroupsClick = { navController.safeNavigate(Routes.MODEL_GROUPS) },
            onOpenSubagentSettings = { navController.safeNavigate(Routes.SUBAGENTS) },
            hassnEmbedded = true,
        )
    }
}

private fun fmtTime(ms: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ms))

private fun ChatSessionEntity.label(untitled: String): String = title?.takeIf { it.isNotBlank() } ?: untitled

private fun dayLabel(ms: Long, today: String, yesterday: String): String {
    val c = java.util.Calendar.getInstance()
    val now = java.util.Calendar.getInstance()
    c.timeInMillis = ms
    val sameYear = c.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
    val dDay = now.get(java.util.Calendar.DAY_OF_YEAR) - c.get(java.util.Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dDay == 0 -> today
        sameYear && dDay == 1 -> yesterday
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(ms))
    }
}

private fun shortTime(ms: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(ms))

@Composable
private fun HassnHomeTab(
    chatRepository: ChatRepository,
    openChat: (String) -> Unit,
    openGateway: () -> Unit,
    openScheduled: () -> Unit,
    openLedger: () -> Unit,
) {
    val ctx = LocalContext.current
    val p = hassn
    remember { com.openminis.app.hassn.ledger.LedgerStore.init(ctx.applicationContext) }
    val ledger by com.openminis.app.hassn.ledger.LedgerStore.state.collectAsState()
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val store = remember { ScheduledTaskStore(ctx.applicationContext) }
    val tasks by remember { store.observe() }.collectAsState(initial = emptyList())
    val hasKey = remember { HassnGateway.hasApiKey(ctx) }
    val modelCount = remember { HassnGateway.cachedModels(ctx).size }
    val untitled = stringResource(R.string.hassn_untitled_chat)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
    ) {
        // status line (mockup: "● 云端大脑在线 · …")
        item {
            val status = when {
                !hasKey -> stringResource(R.string.hassn_home_gateway_unset)
                modelCount > 0 -> stringResource(R.string.hassn_home_gateway_models, modelCount)
                else -> stringResource(R.string.hassn_home_gateway_set)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = openGateway)
                    .padding(start = 6.dp, end = 6.dp, bottom = 10.dp),
            ) {
                HassnDot(if (hasKey) p.green else p.idle)
                Spacer(Modifier.width(8.dp))
                Text(status, style = MaterialTheme.typography.bodySmall, color = p.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        val upcoming = tasks.mapNotNull { t -> t.nextTriggerMs()?.let { t to it } }.sortedBy { it.second }.take(5)
        item {
            GlassCard {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 2.dp)) {
                    Text(stringResource(R.string.hassn_home_scheduled), style = MaterialTheme.typography.titleLarge, color = p.text)
                }
                if (upcoming.isEmpty()) {
                    HassnRow(stringResource(R.string.hassn_home_scheduled_empty), icon = "clock", onClick = openScheduled)
                } else {
                    upcoming.forEachIndexed { i, (t, next) ->
                        if (i > 0) HassnDivider()
                        HassnRow(
                            title = t.label,
                            subtitle = t.lastResultPreview,
                            icon = "clock",
                            value = stringResource(R.string.hassn_next_at, fmtTime(next)),
                            chevron = false,
                            onClick = openScheduled,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item { HassnGroupHeader("临时邮箱台账") }
        item {
            val (nb, np) = com.openminis.app.hassn.ledger.LedgerCore.totals(ledger)
            GlassCard {
                HassnRow(
                    title = if (nb == 0) "还没有临时邮箱" else "$nb 个邮箱 · $np 个平台",
                    subtitle = "密码自动生成存入保管库 · 到期前自动延时",
                    icon = "key",
                    onClick = openLedger,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        val recent = sessions.filter { !it.isSubagentSession }.sortedByDescending { it.updatedAt }.take(5)
        item { HassnGroupHeader(stringResource(R.string.hassn_home_recent)) }
        item {
            GlassCard {
                if (recent.isEmpty()) {
                    HassnRow(stringResource(R.string.hassn_home_recent_empty), icon = "chat", onClick = { openChat(newDraftSessionId()) })
                } else {
                    recent.forEachIndexed { i, s ->
                        if (i > 0) HassnDivider()
                        val name = s.label(untitled)
                        HassnRow(
                            title = name,
                            subtitle = s.lastMessage,
                            value = shortTime(s.updatedAt),
                            chevron = false,
                            leading = { HassnLetterAvatar(name) },
                            onClick = { openChat(s.id) },
                        )
                    }
                }
            }
        }
    }
}

private data class FeedItem(val at: Long, val title: String, val detail: String?, val sessionId: String?, val ok: Boolean?)

@Composable
private fun HassnFeedTab(chatRepository: ChatRepository, openChat: (String) -> Unit) {
    val ctx = LocalContext.current
    val p = hassn
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val store = remember { ScheduledTaskStore(ctx.applicationContext) }
    val tasks by remember { store.observe() }.collectAsState(initial = emptyList<ScheduledTask>())
    val untitled = stringResource(R.string.hassn_untitled_chat)
    val failed = stringResource(R.string.hassn_feed_failed)
    val today = "今天"
    val yesterday = "昨天"

    val feedItems = remember(sessions, tasks) {
        val runs = tasks.flatMap { t -> t.runHistory.map { r -> FeedItem(r.firedAt, t.label, r.preview, r.sessionId, r.ok) } }
        val chats = sessions.filter { !it.isSubagentSession }.map { s -> FeedItem(s.updatedAt, s.label(untitled), s.lastMessage, s.id, null) }
        (runs + chats).sortedByDescending { it.at }.take(60)
    }

    if (feedItems.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(top = 20.dp)) {
            HassnEmptyText(stringResource(R.string.hassn_feed_empty))
        }
        return
    }
    val grouped = feedItems.groupBy { dayLabel(it.at, today, yesterday) }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
    ) {
        grouped.forEach { (day, list) ->
            item(key = "h_$day") {
                Text(day, style = MaterialTheme.typography.titleLarge, color = p.text, modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 12.dp))
            }
            items(list) { f ->
                val tag = when (f.ok) {
                    null -> stringResource(R.string.hassn_feed_chat)
                    true -> stringResource(R.string.hassn_feed_scheduled)
                    false -> failed
                }
                GlassCard(Modifier.padding(bottom = 12.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .let { m -> f.sessionId?.let { id -> m.clickable { openChat(id) } } ?: m }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        FeedOrb(if (f.ok == null) "chat" else if (f.ok) "check" else "alert", f.ok)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(f.title, style = MaterialTheme.typography.titleMedium, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (!f.detail.isNullOrBlank()) {
                                Text(f.detail, style = MaterialTheme.typography.bodyLarge, color = p.text2, maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                                Text(tag, style = MaterialTheme.typography.bodySmall, color = p.text2)
                                Spacer(Modifier.weight(1f))
                                Text(shortTime(f.at), style = MaterialTheme.typography.bodySmall, color = p.text2)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Hark status glyph (spec §8 feed): 16dp line icon in a soft disc; ok = systemGreen, fail = systemRed. */
@Composable
private fun FeedOrb(icon: String, ok: Boolean?) {
    val p = hassn
    val tint = when (ok) { null -> p.text; true -> p.green; false -> p.red }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(top = 1.dp).size(28.dp).clip(CircleShape).background(p.soft),
    ) { HassnIcon(icon, 16.dp, tint = tint) }
}

@Composable
private fun HassnProjectsTab(chatRepository: ChatRepository, openChat: (String) -> Unit) {
    val p = hassn
    val projects by remember { chatRepository.observeProjects() }.collectAsState(initial = emptyList<ProjectEntity>())
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val counts = remember(sessions) { sessions.filter { it.projectId != null }.groupingBy { it.projectId!! }.eachCount() }

    if (projects.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(top = 20.dp)) {
            HassnEmptyText(stringResource(R.string.hassn_projects_empty))
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp), modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        items(projects.sortedWith(compareByDescending<ProjectEntity> { it.pinnedAt ?: 0L }.thenByDescending { it.updatedAt }), key = { it.id }) { pr ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { openChat(newDraftSessionId(pr.id)) }
                    .padding(bottom = 28.dp),
            ) {
                Text(pr.name, style = MaterialTheme.typography.titleLarge, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HassnStatusPill(stringResource(R.string.hassn_projects_chats, counts[pr.id] ?: 0), running = false)
                    val d = pr.description?.takeIf { it.isNotBlank() }
                    if (d != null) {
                        Spacer(Modifier.width(10.dp))
                        Text(d, style = MaterialTheme.typography.bodySmall, color = p.text2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
