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

    @Query("SELECT * FROM savings WHERE date_target LIKE:date AND type_currency IN(:type_currency)")
    LiveData<List<ModelSavings>> findByDate(String date,String type_currency);

    @Query("SELECT * FROM savings WHERE id_savings_user =:id_user AND type_currency IN(:type_currency)")
    LiveData<List<ModelSavings>> findByIdUser(int id_user,String type_currency);


    @Query("SELECT * FROM savings WHERE id_savings =:id AND type_currency IN(:type_currency) ")
    LiveData<ModelSavings> findById(int id,String type_currency);


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
