package com.example.chattingapplication.Model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<User> users);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("UPDATE users SET profile_image = :imagePath WHERE uid = :uid")
    void updateUserImage(String uid, String imagePath);
}
