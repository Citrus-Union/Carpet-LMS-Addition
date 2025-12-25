package cn.nm.lms.carpetlmsaddition

import cn.nm.lms.carpetlmsaddition.rules.commandLMS.CommandLMS
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorController

object CarpetLMSAdditionInit {
    fun initAll() {
        CarpetLMSAdditionRecipes.register()
        CommandLMS.register()
        LowHealthSpectatorController.init()
    }
}
