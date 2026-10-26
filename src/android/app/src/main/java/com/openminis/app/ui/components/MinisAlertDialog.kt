package com.openminis.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openminis.app.R
import com.openminis.app.ui.components.MinisTextButton

/**
 * App-wide confirmation dialog. Tighter than the Material 3 default
 * (16 dp radius, titleLarge instead of headlineSmall) so small
 * confirmation prompts don't read as oversized cards. The destructive
 * variant tints the confirm button with `colorScheme.error`, matching
 * the pattern iOS uses for `UIAlertActionStyle.destructive`.
 */
@Composable
fun MinisAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    text: String? = null,
    dismissText: String = stringResource(R.string.cancel),
    isDestructive: Boolean = false,
    onDismiss: () -> Unit = onDismissRequest,
    /**
     * Optional third action, rendered between dismiss and confirm. When set,
     * the buttons stack vertically instead of sitting in a row: three labels
     * of real length (the pre-send compact prompt's "Compact & Enable
     * Auto-Compact" is 29 chars, and its zh translations are similar) do not
     * fit side by side on a phone, and a Row would either clip them or shrink
     * the text. Callers that pass nothing keep the original two-button row.
     */
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        // [Hassn] Hark dialog (spec §6.8): r24, regularSolid, padding 20, gap 20; buttons are the
        // Hark button recipe — primary ink pill (red for destructive), secondary ink @5 % pill.
        val p = com.openminis.app.hassn.hassn
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = p.solid,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = p.text,
                )
                if (text != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = p.text2,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                val confirmFill = if (isDestructive) p.red else p.ink
                val confirmText2 = if (isDestructive) androidx.compose.ui.graphics.Color.White else p.onInk
                @Composable
                fun Pill(label: String, fill: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color, onClick: () -> Unit, m: Modifier) {
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center,
                        modifier = m
                            .height(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(fill)
                            .clickable(onClick = onClick)
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                if (neutralText != null && onNeutral != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Pill(confirmText, confirmFill, confirmText2, onConfirm, Modifier.fillMaxWidth())
                        Pill(neutralText, p.soft, p.text, onNeutral, Modifier.fillMaxWidth())
                        Pill(dismissText, p.soft, p.text, onDismiss, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Pill(dismissText, p.soft, p.text, onDismiss, Modifier.weight(1f))
                        Pill(confirmText, confirmFill, confirmText2, onConfirm, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
