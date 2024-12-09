package io.hammerhead.sampleext.extension

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import io.hammerhead.karooext.KarooSystemService
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {
    @Provides
    @ServiceScoped
    fun provideKarooSystem(@ApplicationContext context: Context): KarooSystemService {
        return KarooSystemService(context)
    }
}
