package top.ltfan.notdeveloper.service

import io.github.libxposed.service.XposedService
import top.ltfan.notdeveloper.ModuleService
import top.ltfan.notdeveloper.log.Log

/**
 * Asks the framework to apply the module to the packages it needs at run
 * time. The module app has no scope list of its own, so the requested set
 * follows what the user configured.
 */
object ScopeController {
    private val requested = linkedSetOf<String>()

    /** The packages the framework currently applies this module to. */
    val scope: Set<String> get() = requested.toSet()

    /**
     * Makes the framework's scope match [desired]: it requests the packages
     * that are missing and removes the ones that are no longer wanted. A
     * missing service means the module is not activated yet, so the request
     * is skipped and retried on the next [sync].
     */
    fun sync(desired: Set<String>) {
        val service = ModuleService.service ?: return

        val current = runCatching { service.scope.toSet() }.getOrElse { requested.toSet() }

        val toAdd = desired - current
        val toRemove = current - desired

        if (toAdd.isNotEmpty()) {
            try {
                service.requestScope(
                    toAdd.toList(),
                    object : XposedService.OnScopeEventListener {
                        override fun onScopeRequestApproved(approved: List<String>) {
                            requested += approved
                            Log.d("scope request approved: ${approved.joinToString()}")
                        }

                        override fun onScopeRequestFailed(message: String) {
                            Log.d("scope request failed: $message")
                        }
                    },
                )
            } catch (e: Throwable) {
                Log.w("failed to request scope ${toAdd.joinToString()}", e)
            }
        }

        if (toRemove.isNotEmpty()) {
            try {
                service.removeScope(toRemove.toList())
            } catch (e: Throwable) {
                Log.w("failed to remove scope ${toRemove.joinToString()}", e)
            }
        }
    }
}
