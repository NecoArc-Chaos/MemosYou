package xyz.nachaos.memosyou.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import xyz.nachaos.memosyou.data.model.Settings
import xyz.nachaos.memosyou.util.SettingsSerializer

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings_v3.json",
    serializer = SettingsSerializer
)
