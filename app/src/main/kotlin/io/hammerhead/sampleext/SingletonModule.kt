package io.hammerhead.sampleext

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.hammerhead.karooext.KarooSystemService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideKarooSystem(@ApplicationContext context: Context): KarooSystemService {
        return KarooSystemService(context)
    }
}
