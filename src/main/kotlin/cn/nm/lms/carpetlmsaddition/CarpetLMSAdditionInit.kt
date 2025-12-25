package cn.nm.lms.carpetlmsaddition

import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorController

object CarpetLMSAdditionInit {
    fun initAll() {
        CarpetLMSAdditionRecipes.register()
        LowHealthSpectatorController.init()
    }
}
