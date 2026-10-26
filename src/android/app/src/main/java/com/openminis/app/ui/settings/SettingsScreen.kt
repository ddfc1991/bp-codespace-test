package com.openminis.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.openminis.app.hassn.icons.ArrowBack
import com.openminis.app.hassn.icons.KeyboardArrowRight
import com.openminis.app.hassn.icons.Article
import com.openminis.app.hassn.icons.AutoAwesome
import com.openminis.app.hassn.icons.BarChart
import com.openminis.app.hassn.icons.BatteryFull
import com.openminis.app.hassn.icons.Dashboard
import com.openminis.app.hassn.icons.Description
import com.openminis.app.hassn.icons.Extension
import com.openminis.app.hassn.icons.Backup
import com.openminis.app.hassn.icons.Folder
import com.openminis.app.hassn.icons.FolderShared
import com.openminis.app.hassn.icons.Info
import com.openminis.app.hassn.icons.Inventory2
import com.openminis.app.hassn.icons.Lock
import com.openminis.app.hassn.icons.Palette
import com.openminis.app.hassn.icons.Psychology
import com.openminis.app.hassn.icons.Settings
import com.openminis.app.hassn.icons.Shield
import com.openminis.app.hassn.icons.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.openminis.app.hassn.harkGlass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.openminis.app.R
import com.openminis.app.i18n.uppercaseForDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProvidersClick: () -> Unit,
    onModelGroupsClick: () -> Unit,
    onRootfsClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onEnvVarsClick: () -> Unit = {},
    onSkillsClick: () -> Unit = {},
    onTerminalClick: () -> Unit = {},
    onMemoryClick: () -> Unit = {},
    // [T-mcp-integration-android] MCP Integrations page, listed directly below
    // Memory. Default no-op for callers that haven't wired the route yet.
    onMcpClick: () -> Unit = {},
    // [T-soul-md] Soul settings page lives between Skills and Memory in the
    // Agent Runtime section; default no-op for callers that haven't wired
    // the route yet.
    onSoulClick: () -> Unit = {},
    // [T-system-md] Highest-priority custom system prompt (SYSTEM.md), listed
    // directly below Soul so the identity/prompt pair reads as a unit.
    onSystemPromptClick: () -> Unit = {},
    onPermissionsClick: () -> Unit = {},
    onUsageClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onLogsClick: () -> Unit = {},
    // T219-2: Mount External Folders entry. Default no-op for any caller
    // that hasn't wired the route yet.
    onMountedFoldersClick: () -> Unit = {},
    // T235: Shared Folders entry (Shared / Skills / Memory). Default no-op
    // for back-compat with callers wired before T235.
    onSharedFoldersClick: () -> Unit = {},
    // T50: Background & Notifications screen (battery optimisation +
    // OEM autostart guidance). Default no-op so older callers/tests
    // don't need to be retrofitted.
    onBackgroundClick: () -> Unit = {},
    onSubagentsClick: () -> Unit = {},
    // Hook accepted for forward-compat with AppNavigation's About route. The
    // About row below still has a TODO onClick in HEAD; future settings-bucket
    // work will wire this through.
    onAboutClick: () -> Unit = {},
    // [Hassn-P1] Settings → 智能网关
    onHassnGatewayClick: () -> Unit = {},
    // [Hassn-P1] Settings → 临时邮箱台账
    onHassnLedgerClick: () -> Unit = {},
) {
    val hassnCtx = LocalContext.current
    var showRename by remember { mutableStateOf(false) }
    val soulMeta by com.openminis.app.agent.SoulStore.cachedMetadata.collectAsState()
    val hassnName = soulMeta.name.trim().ifEmpty { com.openminis.app.hassn.HassnBrand.DEFAULT_NAME }
    if (showRename) {
        var draft by remember { mutableStateOf(hassnName) }
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(stringResource(R.string.hassn_name_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(com.openminis.app.hassn.HassnBrand.MAX_LEN) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.hassn_name_launcher_note),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    com.openminis.app.hassn.HassnBrand.setName(hassnCtx, draft)
                    showRename = false
                }) { Text(stringResource(R.string.hassn_gateway_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
    // [Hassn-P1] Hark settings page (official screenshot / mockup 08): the shell's glass tab bar
    // stays on top (no tab selected); tapping a tab returns to that tab. No 退出登录 row.
    val hassnTabs = listOf(
        stringResource(R.string.hassn_tab_home),
        stringResource(R.string.hassn_tab_chat),
        stringResource(R.string.hassn_tab_feed),
        stringResource(R.string.hassn_tab_projects),
    )
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            androidx.compose.foundation.layout.Box(Modifier.statusBarsPadding()) {
                com.openminis.app.hassn.HassnTopBar(
                    labels = hassnTabs,
                    selected = -1,
                    onSelect = { i -> com.openminis.app.hassn.HassnShellState.tab.intValue = i; onBack() },
                    leftIcon = "back",
                    onLeft = onBack,
                    onProfile = {},
                    profileDescription = stringResource(R.string.settings_title),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // -- Hassn [Hassn-P1] --
            SettingsSection(title = stringResource(R.string.hassn_section)) {
                SettingsItem(
                    icon = Icons.Outlined.AutoAwesome,
                    iconColor = Color(0xFF00A665),
                    title = stringResource(R.string.hassn_name_title),
                    subtitle = hassnName,
                    onClick = { showRename = true },
                )
                SettingsItem(
                    icon = Icons.Outlined.Dashboard,
                    iconColor = Color(0xFF00A665),
                    title = stringResource(R.string.hassn_gateway_title),
                    subtitle = stringResource(R.string.hassn_gateway_subtitle),
                    onClick = onHassnGatewayClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    iconColor = Color(0xFF00A665),
                    title = "临时邮箱台账",
                    subtitle = "临时邮箱 · 注册过的平台 · 验证码",
                    onClick = onHassnLedgerClick,
                    showDivider = false,
                )
            }

            // -- LLM Providers --
            SettingsSection(
                title = stringResource(R.string.settings_section_llm_providers),
                footer = stringResource(R.string.settings_section_llm_providers_footer),
            ) {
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_manage_providers),
                    subtitle = stringResource(R.string.settings_manage_providers_subtitle),
                    onClick = onProvidersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_model_groups),
                    subtitle = stringResource(R.string.settings_model_groups_subtitle),
                    onClick = onModelGroupsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.BarChart,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_token_usage),
                    subtitle = stringResource(R.string.settings_token_usage_subtitle),
                    onClick = onUsageClick,
                    showDivider = false,
                )
            }

            // -- Appearance --
            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_section_appearance),
                    subtitle = stringResource(R.string.settings_appearance_subtitle),
                    onClick = onAppearanceClick,
                    showDivider = false,
                )
            }

            // -- Agent Runtime --
            SettingsSection(title = stringResource(R.string.settings_section_agent_runtime)) {
                SettingsItem(
                    icon = Icons.Outlined.Psychology,
                    iconColor = Color(0xFF8E8E93),
                    title = "子代理协同 (Subagents)",
                    subtitle = "多智能体团队、MoA混合专家与模型调度白名单",
                    onClick = onSubagentsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Extension,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_skills),
                    subtitle = stringResource(R.string.settings_skills_subtitle),
                    onClick = onSkillsClick,
                )
                // [T-soul-md] insertion between Skills and Memory per spec.
                SettingsItem(
                    icon = Icons.Outlined.AutoAwesome,
                    iconColor = Color(0xFFFF4D00),
                    title = stringResource(R.string.settings_soul),
                    subtitle = stringResource(R.string.settings_soul_subtitle),
                    onClick = onSoulClick,
                )
                // [T-system-md] Highest-priority custom system prompt (SYSTEM.md) —
                // directly below Soul. Article icon (document with text lines)
                // reads as "an editable prompt document"; blue keeps the
                // Agent Runtime section's dominant accent.
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.Article,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_sysprompt),
                    subtitle = stringResource(R.string.settings_sysprompt_subtitle),
                    onClick = onSystemPromptClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Psychology,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_memory),
                    subtitle = stringResource(R.string.settings_memory_subtitle),
                    onClick = onMemoryClick,
                )
                // [T-mcp-integration-android] MCP Integrations — directly below Memory.
                // [T-android-mcp-icon-distinct] Dashboard (2x2 block grid) instead of
                // Extension so MCP no longer shares the Skills row's puzzle-piece icon —
                // the grid reads as "multiple composed blocks/servers". teal unchanged.
                SettingsItem(
                    icon = Icons.Outlined.Dashboard,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_mcp),
                    subtitle = stringResource(R.string.settings_mcp_subtitle),
                    onClick = onMcpClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Terminal,
                    iconColor = Color(0xFF00A665),
                    title = stringResource(R.string.settings_env_vars),
                    subtitle = stringResource(R.string.settings_env_vars_subtitle),
                    onClick = onEnvVarsClick,
                    showDivider = false,
                )
            }

            // -- Storage --
            SettingsSection(title = stringResource(R.string.settings_section_storage)) {
                SettingsItem(
                    icon = Icons.Outlined.Inventory2,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_section_storage),
                    subtitle = stringResource(R.string.settings_storage_subtitle),
                    onClick = onRootfsClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Folder,
                    iconColor = Color(0xFF00A665),
                    title = stringResource(R.string.settings_shared_folders),
                    subtitle = stringResource(R.string.settings_shared_folders_subtitle),
                    onClick = onSharedFoldersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.FolderShared,
                    iconColor = Color(0xFFFF4D00),
                    title = stringResource(R.string.settings_mount_external_folders),
                    subtitle = stringResource(R.string.settings_mount_external_folders_subtitle),
                    onClick = onMountedFoldersClick,
                )
                SettingsItem(
                    icon = Icons.Outlined.Backup,
                    iconColor = Color(0xFF00A665),
                    title = stringResource(R.string.settings_backup_restore),
                    subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                    onClick = onBackupClick,
                    showDivider = false,
                )
            }

            // -- Permissions --
            SettingsSection(title = stringResource(R.string.settings_section_permissions)) {
                SettingsItem(
                    icon = Icons.Outlined.Shield,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_section_permissions),
                    subtitle = stringResource(R.string.settings_permissions_subtitle),
                    onClick = onPermissionsClick,
                    showDivider = false,
                )
            }

            // -- Background & Notifications (T50) --
            SettingsSection(
                title = stringResource(R.string.bg_section_header),
                footer = stringResource(R.string.bg_section_footer),
            ) {
                SettingsItem(
                    icon = Icons.Outlined.BatteryFull,
                    iconColor = Color(0xFFFF4D00),
                    title = stringResource(R.string.bg_section_header),
                    subtitle = stringResource(R.string.bg_section_subtitle),
                    onClick = onBackgroundClick,
                    showDivider = false,
                )
            }

            // -- Logs --
            SettingsSection(title = stringResource(R.string.settings_section_logs)) {
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_section_logs),
                    subtitle = stringResource(R.string.settings_logs_subtitle),
                    onClick = onLogsClick,
                    showDivider = false,
                )
            }

            // -- About --
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    iconColor = Color(0xFF8E8E93),
                    title = stringResource(R.string.settings_about_minis),
                    subtitle = stringResource(R.string.settings_about_subtitle),
                    onClick = onAboutClick,
                    showDivider = false,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * [Hassn] Hark settings group (spec §6.5): optional quiet header, r24 glass card (blur 32 +
 * regularSurface), 12dp between groups.
 */
@Composable
private fun SettingsSection(
    title: String,
    footer: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        com.openminis.app.hassn.HassnGroupHeader(title, Modifier.padding(start = 16.dp, top = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .then(
                    with(com.openminis.app.hassn.HarkBlur) {
                        Modifier.harkGlass(RoundedCornerShape(24.dp), com.openminis.app.hassn.hassn.card, Card)
                    },
                ),
        ) {
            content()
        }
        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = com.openminis.app.hassn.hassn.text2,
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 8.dp),
            )
        }
    }
}

/**
 * [Hassn] Hark settings row (spec §6.5): 56dp, padding 20 / 18, 16dp line icon, gap 12,
 * titleMedium 15/18 w500, 16dp chevron, inset hairline. Like Hark's own settings page the hub
 * shows titles only (a short `subtitle` becomes a trailing value; `iconColor` kept for call-site compatibility, not drawn).
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    @Suppress("UNUSED_PARAMETER") iconColor: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    val p = com.openminis.app.hassn.hassn
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = p.text, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = p.text, modifier = Modifier.weight(1f))
            // a short subtitle is a value (e.g. the assistant's name) → trailing textSecondary
            if (subtitle != null && subtitle.length <= 12) {
                Spacer(Modifier.width(12.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = p.text2, maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            com.openminis.app.hassn.HassnIcon("chev", 16.dp, tint = p.text)
        }
        if (showDivider) com.openminis.app.hassn.HassnDivider()
    }
}
