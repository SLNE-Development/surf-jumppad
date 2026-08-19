package dev.slne.surf.jumppad.permissions

import dev.slne.surf.api.paper.permission.PermissionRegistry
import dev.slne.surf.jumppad.core.client.permission.JumpPadPermissions

object Permissions : PermissionRegistry() {
    val COMMAND_JUMP_PAD_GENERIC = create(JumpPadPermissions.COMMAND_JUMP_PAD_GENERIC)
}
