package com.openminis.app.hassn

import android.content.Context
import com.openminis.app.agent.SoulFile
import com.openminis.app.agent.SoulMetadata
import com.openminis.app.agent.SoulStore

/**
 * [Hassn-P1] The user-editable name of the app / assistant.
 *
 * Single source of truth is SOUL.md's `name` (already what the chat header and the system-prompt
 * identity read), so renaming here renames the assistant everywhere inside the app.
 *
 * Android limit: the launcher label under the home-screen icon comes from the manifest
 * (`@string/app_name` = "Hassn") and cannot be set to arbitrary text at runtime. The only runtime
 * lever is switching between activity-aliases that are declared in advance with fixed labels, so a
 * free-text name can only ever change the in-app name, not the launcher label.
 */
object HassnBrand {
    const val DEFAULT_NAME = "Hassn"
    const val MAX_LEN = 24

    fun name(c: Context): String =
        SoulStore.cachedMetadata.value.name.trim().ifEmpty { DEFAULT_NAME }

    fun setName(c: Context, newName: String) {
        val v = newName.trim().replace("\n", " ").take(MAX_LEN).ifEmpty { DEFAULT_NAME }
        SoulStore.ensureExists(c)
        val cur = SoulStore.load(c) ?: SoulFile(SoulMetadata.DEFAULT, "")
        SoulStore.save(c, cur.copy(metadata = cur.metadata.copy(name = v)))
    }
}
