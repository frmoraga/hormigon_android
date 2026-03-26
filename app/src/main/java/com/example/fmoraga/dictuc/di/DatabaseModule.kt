package com.example.fmoraga.dictuc.di

import android.content.Context
import androidx.room.Room
import com.example.fmoraga.dictuc.data.local.db.DictucDatabase
import com.example.fmoraga.dictuc.data.local.db.dao.MuestraDao
import com.example.fmoraga.dictuc.data.local.db.dao.MuestreadorDao
import com.example.fmoraga.dictuc.data.local.db.dao.SyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): DictucDatabase =
        Room.databaseBuilder(context, DictucDatabase::class.java, "DB_Hormigon_v2")
            .addMigrations(DictucDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMuestreadorDao(db: DictucDatabase): MuestreadorDao = db.muestreadorDao()

    @Provides
    fun provideMuestraDao(db: DictucDatabase): MuestraDao = db.muestraDao()

    @Provides
    fun provideSyncDao(db: DictucDatabase): SyncDao = db.syncDao()
}
