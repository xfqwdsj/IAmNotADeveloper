package top.ltfan.notdeveloper.xposed

import android.content.Context
import top.ltfan.notdeveloper.ModuleService

/**
 * Whether the module is activated in the framework, which signals this by
 * connecting to the module app through the module service.
 */
val statusIsModuleActivated get() = ModuleService.isActivated

/**
 * Whether the preferences shared with the hooked packages are available. The
 * module app stores the settings in them and the hooked packages read them
 * through the framework; while they are unavailable every hook uses its
 * default.
 */
val Context.statusIsPreferencesReady get() = ModuleService.preferences != null
