package xyz.nachaos.memosyou

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import xyz.nachaos.memosyou.ui.security.AppLockSession

@HiltAndroidApp
class MemosYouApp: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var CONTEXT: Context
    }

    override fun attachBaseContext(base: Context?) {
        CONTEXT = this
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                AppLockSession.markAppForegrounded()
            }

            override fun onStop(owner: LifecycleOwner) {
                AppLockSession.markAppBackgrounded()
            }
        })
    }
}