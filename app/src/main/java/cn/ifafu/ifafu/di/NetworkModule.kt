package cn.ifafu.ifafu.di

import android.content.Context
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.service.common.PersistentCookieJar
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.interceptor.CookieInterceptor
import cn.ifafu.ifafu.service.interceptor.JWCookieInterceptor
import cn.ifafu.ifafu.service.interceptor.TokenInterceptor
import cn.ifafu.ifafu.service.interceptor.XfbHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIME_OUT = 20L
    private const val WRITE_TIME_OUT = 20L
    private const val READ_TIME_OUT = 20L

    @Provides
    @Singleton
    @Jiaowu
    fun provideJwOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor { Timber.tag("API").d(it) }
        logger.level = HttpLoggingInterceptor.Level.BASIC
        val sharedPreferences =
            context.getSharedPreferences(Constants.SP_COOKIE, Context.MODE_PRIVATE)
        return OkHttpClient.Builder()
            .addInterceptor(JWCookieInterceptor(sharedPreferences))
            .addInterceptor(logger)
            //            .cookieJar(PersistentCookieJar())
            .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideJwHttpClient(
        @Jiaowu client: OkHttpClient,
        database: JiaowuDatabase,
        @ApplicationContext context: Context,
    ): ZFHttpClient {
        return ZFHttpClient(client, database.userDao, context)
    }

    @Provides
    @Singleton
    @Ifafu
    fun provideIFAFUOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor { Timber.tag("API").d(it) }
        logger.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(TokenInterceptor())
            .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT * 2, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Ifafu
    fun provideIFAFUApiRetrofit(
        @Ifafu client: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(Constants.IFAFU_API_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    @Xfb
    fun provideXfbApiRetrofit(
        jiaowuDatabase: JiaowuDatabase,
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(XfbHeaderInterceptor())
            .addInterceptor(CookieInterceptor(jiaowuDatabase.elecCookieDao))
            .cookieJar(PersistentCookieJar())
            .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT * 2, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .client(client)
            .baseUrl("http://cardapp.fafu.edu.cn:8088")
            .build()
    }

}