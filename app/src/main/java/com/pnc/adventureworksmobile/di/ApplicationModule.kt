package com.pnc.adventureworksmobile.di

import android.content.Context
import androidx.room.Room
import com.pnc.adventureworksmobile.BuildConfig
import com.pnc.adventureworksmobile.data.local.AdventureWorksDatabase
import com.pnc.adventureworksmobile.data.local.MIGRATION_1_2
import com.pnc.adventureworksmobile.data.local.MIGRATION_2_3
import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.SavedProductDao
import com.pnc.adventureworksmobile.data.remote.OrderApi
import com.pnc.adventureworksmobile.data.remote.ProductApi
import com.pnc.adventureworksmobile.data.repository.DefaultOrderRepository
import com.pnc.adventureworksmobile.data.repository.DefaultProductRepository
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(NetworkTimeouts.CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkTimeouts.READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkTimeouts.WRITE_SECONDS, TimeUnit.SECONDS)
            .callTimeout(NetworkTimeouts.CALL_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        client: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun provideOrderApi(retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)

    @Provides
    @Singleton
    fun provideProductRepository(
        api: ProductApi,
        savedProductDao: SavedProductDao,
        recentlyViewedProductDao: RecentlyViewedProductDao,
    ): ProductRepository = DefaultProductRepository(api, savedProductDao, recentlyViewedProductDao)

    @Provides
    @Singleton
    fun provideOrderRepository(
        api: OrderApi,
    ): OrderRepository = DefaultOrderRepository(api)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AdventureWorksDatabase = Room.databaseBuilder(
        context,
        AdventureWorksDatabase::class.java,
        "adventure-works.db",
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()

    @Provides
    fun provideSavedProductDao(database: AdventureWorksDatabase): SavedProductDao =
        database.savedProductDao()

    @Provides
    fun provideRecentlyViewedProductDao(
        database: AdventureWorksDatabase,
    ): RecentlyViewedProductDao = database.recentlyViewedProductDao()
}

// How long the app waits on the network before giving up.
// A timeout throws an InterruptedIOException, which ErrorMapper turns into AppError.Timeout.
internal object NetworkTimeouts {
    // Time to open a connection to the server.
    const val CONNECT_SECONDS = 10L

    // Longest gap between chunks of data from the server. Not the total time.
    const val READ_SECONDS = 20L

    // Longest gap between chunks of data the app sends.
    const val WRITE_SECONDS = 20L

    // The whole call, start to finish. This is the cap that stops an endless spinner.
    // The product list sends photos inside the JSON, so it gets room on a slow connection.
    const val CALL_SECONDS = 30L
}
