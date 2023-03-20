package id.co.evolution.financefy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelSavings;

@Dao
public interface SavingsDao {
    @Query("SELECT * FROM savings")
    LiveData<List<ModelSavings>> getAll();

    @Query("SELECT * FROM savings WHERE id_savings IN(:savingsIds)")
    List<ModelSavings> loadAllbyIds(int[] savingsIds);

    @Query("SELECT * FROM savings WHERE date_target LIKE:date")
    LiveData<List<ModelSavings>> findByDate(String date);

    @Query("SELECT * FROM savings WHERE id_savings_user =:id_user")
    LiveData<List<ModelSavings>> findByIdUser(int id_user);


    @Query("SELECT * FROM savings WHERE id_savings =:id LIMIT 1")
    LiveData<ModelSavings> findById(int id);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ModelSavings... savingss);

    @Update
    void update(ModelSavings savings);

    @Delete
    void delete(ModelSavings savings);

    @Delete
    void deleteSavings(List<ModelSavings> savings);

    @Query("DELETE  FROM savings WHERE id_savings_user =:id")
    void deleteSavingsByIdUser(int id);
}
