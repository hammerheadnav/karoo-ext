package io.hammerhead.sampleext

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.hammerhead.karooext.KarooSystemService

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideKarooSystem(@ApplicationContext context: Context): KarooSystemService {
        return KarooSystemService(context)
    }
}
