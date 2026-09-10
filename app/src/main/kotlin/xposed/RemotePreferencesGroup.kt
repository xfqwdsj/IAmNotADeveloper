package top.ltfan.notdeveloper.xposed

import top.ltfan.notdeveloper.BuildConfig

/**
 * Name of the remote preferences group shared between the module app and
 * the hooked packages.
 *
 * Both sides have to agree on it: the module app writes the group through
 * the framework service (see [top.ltfan.notdeveloper.ModuleService])
 * and the hooked packages read it with [Module.preferences].
 */
const val RemotePreferencesGroup: String = BuildConfig.APPLICATION_ID
