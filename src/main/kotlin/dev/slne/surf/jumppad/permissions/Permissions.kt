package dev.slne.surf.jumppad.permissions

import dev.slne.surf.api.paper.permission.PermissionRegistry

object Permissions : PermissionRegistry() {
    const val PREFIX = "surf.jump-pad"
    const val COMMAND_PREFIX = "$PREFIX.command"

    val COMMAND_JUMP_PAD_GENERIC = create("$COMMAND_PREFIX.jump-pad")
}