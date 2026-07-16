package me.mudkip.moememos.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class DarkMode {
    SYSTEM, LIGHT, DARK
}

@Serializable
enum class SortOrder {
    NEWEST, OLDEST
}

@Serializable
data class Settings(
    val usersList: List<UserData> = emptyList(),
    val currentUser: String = "",
    val appLockEnabled: Boolean = false,
    val presetThemeId: String = "",
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val sortOrder: SortOrder = SortOrder.NEWEST,
)
