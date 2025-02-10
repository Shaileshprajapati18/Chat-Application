package com.example.chattingapplication.Fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingapplication.Adapter.UsersAdapter;
import com.example.chattingapplication.Model.Base64ToImageConverter;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.Model.UserDatabase;
import com.example.chattingapplication.Model.UserRepository;
import com.example.chattingapplication.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView userRecycler;
    private DatabaseReference databaseReference;
    private ShimmerFrameLayout shimmerChat;
    private UsersAdapter usersAdapter;
    private ArrayList<User> usersList;
    private SimpleSearchView searchView;
    private UserRepository userRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        shimmerChat = view.findViewById(R.id.shimmer_chat);
        searchView = view.findViewById(R.id.searchView);
        searchView.setVisibility(GONE);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        userRecycler = view.findViewById(R.id.user_recycler);
        userRecycler.setHasFixedSize(true);
        userRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRepository = new UserRepository(getContext());

        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), usersList);
        userRecycler.setAdapter(usersAdapter);

        loadUsersFromRoom();
        loadChatFromFirebase();

        return view;
    }

    private void loadUsersFromRoom() {
        shimmerChat.setVisibility(VISIBLE);

        userRepository.getAllUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    usersList.clear();
                    usersList.addAll(users);
                    usersAdapter.notifyDataSetChanged();
                    shimmerChat.setVisibility(GONE);
                    userRecycler.setVisibility(VISIBLE);
                    Log.d("ChatFragment", "Users loaded from Room: " + users.size());
                } else {
                    shimmerChat.setVisibility(GONE);
                    Log.d("ChatFragment", "No users found in Room");
                }
            }
        });
    }

    private void loadChatFromFirebase() {
        shimmerChat.setVisibility(VISIBLE);
        databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    usersList.clear();
                    List<User> newUsers = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String imageFilePath = null;
                            if (user.getProfileImage() != null) {
                                File imageFile = Base64ToImageConverter.convertBase64ToImage(
                                        getContext(), user.getProfileImage(), "profile_" + user.getUid() + ".jpg");

                                if (imageFile != null && imageFile.exists()) {
                                    imageFilePath = imageFile.getAbsolutePath();
                                    Log.d("myimage", "Image saved at: " + imageFilePath);
                                } else {
                                    Log.e("myimage", "Failed to save image");
                                }
                            }

                            User newUser = new User(user.getUid(), user.getName(), user.getPhoneNumber(), imageFilePath);
                            newUsers.add(newUser);
                            usersList.add(newUser);
                        }
                    }

                    usersAdapter.notifyDataSetChanged();
                    userRepository.insertUsers(newUsers);

                    shimmerChat.setVisibility(GONE);
                    userRecycler.setVisibility(VISIBLE);
                } else {
                    shimmerChat.setVisibility(GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.top_navigation_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            searchView.showSearch(true);
            searchView.setHint("Search");
            searchView.setVisibility(VISIBLE);
            return true;
        } else if (id == R.id.group) {
            Toast.makeText(getActivity(), "Group clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.invite) {
            Toast.makeText(getActivity(), "Invite clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.settings) {
            Toast.makeText(getActivity(), "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
