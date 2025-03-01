package com.example.chattingapplication.Fragment;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chattingapplication.Model.Base64ToImageConverter;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.Model.UserRepository;
import com.example.chattingapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.List;

public class ProfileFragment extends Fragment {

    DatabaseReference reference;
    TextView name, phone;
    UserRepository userRepository;
    de.hdodenhof.circleimageview.CircleImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userRepository = new UserRepository(getContext());
        name = view.findViewById(R.id.name);
        phone = view.findViewById(R.id.Phone);
        imageView = view.findViewById(R.id.imageView);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserData();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        toolbar.setTitle("Profile");

        userRepository.getAllUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    User currentUser = users.get(0);
                    name.setText(currentUser.getName());
                    phone.setText(currentUser.getPhoneNumber());
                    String profileImagePath = currentUser.getProfileImage();

                    if (profileImagePath != null && !profileImagePath.isEmpty()) {
                        File imageFile = new File(profileImagePath);
                        if (imageFile.exists()) {
                            imageView.setImageURI(Uri.fromFile(imageFile));
                        } else {
                            imageView.setImageResource(R.drawable.avatar);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.avatar);
                    }
                }
            }
        });

        return view;
    }

    private void getUserData() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userPhone = snapshot.child("phoneNumber").getValue(String.class);
                    String profileImageBase64 = snapshot.child("profileImage").getValue(String.class);

                    name.setText(userName);
                    phone.setText(userPhone);
                    File imageFile = Base64ToImageConverter.convertBase64ToImage(getActivity(), profileImageBase64, "profile_image.jpg");
                    if (imageFile != null && imageFile.exists()) {
                        imageView.setImageURI(Uri.fromFile(imageFile));
                    } else {
                        imageView.setImageResource(R.drawable.avatar);
                    }
                    } else {
                        Toast.makeText(getContext(), "User data not found!", Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve data!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
