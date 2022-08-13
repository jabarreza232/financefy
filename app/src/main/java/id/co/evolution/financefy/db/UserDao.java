package id.co.evolution.financefy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.model.ModelUserWithFinance;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    LiveData<List<ModelUser>> getAll();

    @Query("SELECT * FROM user WHERE id_user IN(:userIds)")
    List<ModelUser> loadAllbyIds(int[] userIds);

    @Query("SELECT * FROM user WHERE type LIKE:type")
    LiveData<List<ModelUser>> findByType(String type);


    @Query("SELECT * FROM user WHERE id_user =:id LIMIT 1")
    LiveData<ModelUser> findById(int id);


    @Transaction
    @Query("SELECT * FROM user WHERE id_user =:id")
    LiveData<ModelUserWithFinance> findFinanceByUserId(int id);

    @Transaction
    @Query("SELECT * FROM user")
    LiveData<List<ModelUserWithFinance>> getAllFinanceAndUser();

    @Insert
    void insertAll(ModelUser... users);

    @Update
    void update(ModelUser user);

    @Delete
    void delete(ModelUser user);
}
