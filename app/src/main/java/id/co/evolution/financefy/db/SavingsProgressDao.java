package id.co.evolution.financefy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavings;
import id.co.evolution.financefy.model.ModelSavingsProgress;

@Dao
public interface SavingsProgressDao {
    @Query("SELECT * FROM savings_progress")
    LiveData<List<ModelSavingsProgress>> getAll();

    @Query("SELECT * FROM savings_progress WHERE id_progress_savings IN(:savingsIds)")
    List<ModelSavingsProgress> loadAllbyIds(int[] savingsIds);

    @Query("SELECT * FROM savings_progress WHERE date_progress_savings LIKE:date")
    LiveData<List<ModelSavingsProgress>> findByDate(String date);

    @Query("SELECT * FROM savings_progress WHERE id_savings =:id_savings")
    LiveData<List<ModelSavingsProgress>> findByIdSavings(int id_savings);


    @Query("SELECT * FROM savings_progress WHERE id_progress_savings =:id LIMIT 1")
    LiveData<ModelSavingsProgress> findById(int id);

    @Query("SELECT * FROM savings_progress WHERE date_progress_savings IN(:date) AND id_savings IN(:id_savings)")
    LiveData<List<ModelSavingsProgress>> loadAllByWeek(List<String> date, int id_savings);

    @Query("SELECT * FROM savings_progress WHERE month IN(:month) AND id_savings IN(:id_savings)")
    LiveData<List<ModelSavingsProgress>> loadAllByMonth(String month, int id_savings);

    @Insert
    void insertAll(ModelSavingsProgress... savings);

    @Update
    void update(ModelSavingsProgress savings);

    @Delete
    void delete(ModelSavingsProgress savings);

    @Delete
    void deleteSavings(List<ModelSavingsProgress> savings);

    @Query("DELETE  FROM savings_progress WHERE id_savings =:id")
    void deleteSavingsByIdUser(int id);
}
