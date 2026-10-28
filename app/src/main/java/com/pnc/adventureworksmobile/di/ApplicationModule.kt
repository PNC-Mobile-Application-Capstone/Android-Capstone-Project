package com.pnc.adventureworksmobile.di

import android.content.Context
import androidx.room.Room
import com.pnc.adventureworksmobile.BuildConfig
import com.pnc.adventureworksmobile.data.local.AdventureWorksDatabase
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
    ).build()

    @Provides
    fun provideSavedProductDao(database: AdventureWorksDatabase): SavedProductDao =
        database.savedProductDao()

    @Provides
    fun provideRecentlyViewedProductDao(
        database: AdventureWorksDatabase,
    ): RecentlyViewedProductDao = database.recentlyViewedProductDao()
}
