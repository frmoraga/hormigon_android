package com.example.fmoraga.dictuc.data.local.db.dao

import androidx.room.*
import com.example.fmoraga.dictuc.data.local.db.entity.MuestreadorEntity

@Dao
interface MuestreadorDao {

    @Query("SELECT * FROM muestreador WHERE run = :run AND password = :password AND activo = 1 LIMIT 1")
    suspend fun getByCredentials(run: Long, password: String): MuestreadorEntity?

    @Query("SELECT * FROM muestreador")
    suspend fun getAll(): List<MuestreadorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MuestreadorEntity>)

    @Query("DELETE FROM muestreador")
    suspend fun deleteAll()
}
