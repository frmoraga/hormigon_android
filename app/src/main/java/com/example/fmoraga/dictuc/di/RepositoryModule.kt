package com.example.fmoraga.dictuc.di

import com.example.fmoraga.dictuc.data.repository.DictucRepositoryImpl
import com.example.fmoraga.dictuc.domain.repository.DictucRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDictucRepository(impl: DictucRepositoryImpl): DictucRepository
}
