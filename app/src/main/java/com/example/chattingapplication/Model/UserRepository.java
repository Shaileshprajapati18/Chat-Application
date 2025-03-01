package com.example.chattingapplication.Model;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;

    public UserRepository(Context context) {
        UserDatabase database = UserDatabase.getInstance(context);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public void updateUsers(List<User> users) {
        executorService.execute(() -> {

            userDao.deleteAllUsers();
            userDao.insertUsers(users);
        });
    }
    public void updateUserImage(String uid, String imagePath) {
        executorService.execute(() -> userDao.updateUserImage(uid, imagePath));
    }

}
