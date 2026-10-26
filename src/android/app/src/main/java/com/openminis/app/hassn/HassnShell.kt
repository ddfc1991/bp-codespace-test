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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * [Hassn-P1] Top-level shell: pill tabs 主页 / 聊天 / 动态 / 项目 with the profile button
 * (→ Settings) on the right, as in the approved v2 mockups. Every tab reads real app data
 * (sessions, projects, scheduled tasks, gateway state). Features the mockups show that do not
 * exist yet (目标看板, 专家, 等你确认) are not drawn with sample data; the home tab says they come later.
 *
 * 聊天 embeds the upstream Hark session list + chat unchanged (same look), below the tabs.
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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = tab != 0) { tab = 0 }

    val openChat: (String) -> Unit = { id -> navController.safeNavigate(Routes.chat(id)) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (tab != 1) HassnWeatherLayer(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            HassnTopBar(
                selected = tab,
                onSelect = { tab = it },
                onProfile = { navController.safeNavigate(Routes.SETTINGS) },
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    1 -> ChatSplitScaffoldRoute(
                        initialSessionId = null,
                        navController = navController,
                        chatRepository = chatRepository,
                        providerRepository = providerRepository,
                        memoryRepository = memoryRepository,
                        skillRepository = skillRepository,
                        mcpRepository = mcpRepository,
                    )
                    2 -> HassnFeedTab(chatRepository, openChat)
                    3 -> HassnProjectsTab(chatRepository, openChat)
                    else -> HassnHomeTab(
                        chatRepository = chatRepository,
                        openChat = openChat,
                        openGateway = { navController.safeNavigate(Routes.HASSN_GATEWAY) },
                        openScheduled = { navController.safeNavigate(Routes.SCHEDULED_TASKS) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HassnTopBar(selected: Int, onSelect: (Int) -> Unit, onProfile: () -> Unit) {
    val labels = listOf(
        stringResource(R.string.hassn_tab_home),
        stringResource(R.string.hassn_tab_chat),
        stringResource(R.string.hassn_tab_feed),
        stringResource(R.string.hassn_tab_projects),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f),
        ) {
            Row(Modifier.padding(4.dp)) {
                labels.forEachIndexed { i, label ->
                    val on = i == selected
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (on) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelect(i) }
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            label,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            color = if (on) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.size(8.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        ) {
            IconButton(onClick = onProfile) {
                Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.hassn_profile_settings))
            }
        }
    }
}

private fun fmtTime(ms: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(ms))

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) { Column(Modifier.padding(vertical = 6.dp)) { content() } }
}

@Composable
private fun Line(title: String, subtitle: String?, trailing: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.size(8.dp))
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun ChatSessionEntity.label(untitled: String): String = title?.takeIf { it.isNotBlank() } ?: untitled

@Composable
private fun HassnHomeTab(
    chatRepository: ChatRepository,
    openChat: (String) -> Unit,
    openGateway: () -> Unit,
    openScheduled: () -> Unit,
) {
    val ctx = LocalContext.current
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val store = remember { ScheduledTaskStore(ctx.applicationContext) }
    val tasks by remember { store.observe() }.collectAsState(initial = emptyList())
    val hasKey = remember { HassnGateway.hasApiKey(ctx) }
    val modelCount = remember { HassnGateway.cachedModels(ctx).size }
    val untitled = stringResource(R.string.hassn_untitled_chat)

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
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
                    .padding(horizontal = 20.dp, vertical = 6.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (hasKey) androidx.compose.ui.graphics.Color(0xFF34C759) else MaterialTheme.colorScheme.outline),
                )
                Spacer(Modifier.size(8.dp))
                Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val upcoming = tasks.mapNotNull { t -> t.nextTriggerMs()?.let { t to it } }.sortedBy { it.second }.take(5)
        item { SectionTitle(stringResource(R.string.hassn_home_scheduled)) }
        item {
            Card {
                if (upcoming.isEmpty()) {
                    Line(stringResource(R.string.hassn_home_scheduled_empty), null, onClick = openScheduled)
                } else {
                    upcoming.forEach { (t, next) ->
                        Line(t.label, t.lastResultPreview, stringResource(R.string.hassn_next_at, fmtTime(next)), onClick = openScheduled)
                    }
                }
            }
        }

        val recent = sessions.filter { !it.isSubagentSession }.sortedByDescending { it.updatedAt }.take(5)
        item { SectionTitle(stringResource(R.string.hassn_home_recent)) }
        item {
            Card {
                if (recent.isEmpty()) {
                    Line(stringResource(R.string.hassn_home_recent_empty), null, onClick = { openChat(newDraftSessionId()) })
                } else {
                    recent.forEach { s -> Line(s.label(untitled), s.lastMessage, fmtTime(s.updatedAt), onClick = { openChat(s.id) }) }
                }
            }
        }

        item {
            Text(
                stringResource(R.string.hassn_home_later_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

private data class FeedItem(val at: Long, val title: String, val detail: String?, val sessionId: String?, val ok: Boolean?)

@Composable
private fun HassnFeedTab(chatRepository: ChatRepository, openChat: (String) -> Unit) {
    val ctx = LocalContext.current
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val store = remember { ScheduledTaskStore(ctx.applicationContext) }
    val tasks by remember { store.observe() }.collectAsState(initial = emptyList<ScheduledTask>())
    val untitled = stringResource(R.string.hassn_untitled_chat)
    val failed = stringResource(R.string.hassn_feed_failed)

    val feedItems = remember(sessions, tasks) {
        val runs = tasks.flatMap { t -> t.runHistory.map { r -> FeedItem(r.firedAt, t.label, r.preview, r.sessionId, r.ok) } }
        val chats = sessions.filter { !it.isSubagentSession }.map { s -> FeedItem(s.updatedAt, s.label(untitled), s.lastMessage, s.id, null) }
        (runs + chats).sortedByDescending { it.at }.take(60)
    }

    if (feedItems.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.hassn_feed_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp), modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        items(feedItems) { f ->
            val tag = when (f.ok) {
                null -> stringResource(R.string.hassn_feed_chat)
                true -> stringResource(R.string.hassn_feed_scheduled)
                false -> failed
            }
            Card {
                Line(
                    title = f.title,
                    subtitle = f.detail,
                    trailing = "$tag · ${fmtTime(f.at)}",
                    onClick = f.sessionId?.let { id -> { openChat(id) } },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HassnProjectsTab(chatRepository: ChatRepository, openChat: (String) -> Unit) {
    val projects by remember { chatRepository.observeProjects() }.collectAsState(initial = emptyList<ProjectEntity>())
    val sessions by remember { chatRepository.observeSessions() }.collectAsState(initial = emptyList())
    val counts = remember(sessions) { sessions.filter { it.projectId != null }.groupingBy { it.projectId!! }.eachCount() }

    if (projects.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.hassn_projects_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp), modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        items(projects.sortedWith(compareByDescending<ProjectEntity> { it.pinnedAt ?: 0L }.thenByDescending { it.updatedAt }), key = { it.id }) { p ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { openChat(newDraftSessionId(p.id)) }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            ) {
                Text(p.name, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                val sub = listOfNotNull(
                    stringResource(R.string.hassn_projects_chats, counts[p.id] ?: 0),
                    p.description?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

