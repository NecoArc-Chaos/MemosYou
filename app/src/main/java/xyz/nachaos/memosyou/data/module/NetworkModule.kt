package xyz.nachaos.memosyou.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.JavaNetCookieJar
import okhttp3.LoggingInterceptor
import okhttp3.OkHttpClient
import xyz.nachaos.memosyou.BuildConfig
import java.net.CookieManager
import java.net.CookiePolicy

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        
        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
        
        // Only enable logging in debug builds
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = LoggingInterceptor().apply {
                level = LoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
}