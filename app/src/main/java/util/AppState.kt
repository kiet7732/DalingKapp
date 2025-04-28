package util

object AppState {
    private var currentChatScreenMatchId: String? = null
    private var isAppInForeground: Boolean = false

    fun setCurrentChatScreen(matchId: String?) {
        currentChatScreenMatchId = matchId
    }

    fun isChatScreenOpen(matchId: String): Boolean {
        val isOpen = currentChatScreenMatchId == matchId
        return isOpen
    }

    fun setAppForeground(isForeground: Boolean) {
        isAppInForeground = isForeground
    }

    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }
}