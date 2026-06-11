package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AurumDao {
    @Query("SELECT * FROM holdings ORDER BY timestamp DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Delete
    suspend fun deleteHolding(holding: HoldingEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM auto_invest_plans ORDER BY timestamp DESC")
    fun getAutoInvestPlans(): Flow<List<AutoInvestPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoInvestPlan(plan: AutoInvestPlanEntity)

    @Delete
    suspend fun deleteAutoInvestPlan(plan: AutoInvestPlanEntity)

    @Query("SELECT * FROM user_account WHERE id = :id LIMIT 1")
    fun getUserAccount(id: Int = 1): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity)
}
