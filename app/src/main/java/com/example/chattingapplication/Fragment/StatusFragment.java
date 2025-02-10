package com.example.chattingapplication.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chattingapplication.Adapter.StatusAdapter;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.Model.status;
import com.example.chattingapplication.Model.statusModel;
import com.example.chattingapplication.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class StatusFragment extends Fragment {

    RecyclerView recyclerView;
    StatusAdapter statusAdapter;
    ArrayList<statusModel> statusArrayList;
    ShimmerFrameLayout shmmerview;
    ProgressDialog progressDialog;

    DatabaseReference databaseReference;
    private static final int PICK_IMAGE = 1;
    Bitmap selectedBitmap;
    ConstraintLayout myStatus;
    Date date;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        Toolbar toolbar=view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        shmmerview=view.findViewById(R.id.shmmerview);
        shmmerview.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        statusArrayList = new ArrayList<>();

        statusAdapter = new StatusAdapter(getContext(), statusArrayList);
        recyclerView.setAdapter(statusAdapter);

        loadStatusFromFirebase();

        myStatus = view.findViewById(R.id.myStatus);
        myStatus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchUserDetails();
                                        }
                                    });
        date = new Date();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(FirebaseAuth.getInstance().getUid());

        return view;
    }

    private void loadStatusFromFirebase() {

        databaseReference.child("status").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    statusArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        statusModel statusModel = new statusModel();

                        statusModel.setName(dataSnapshot.child("name").getValue(String.class));
                        statusModel.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                        Long lastUpdated = dataSnapshot.child("lastUpdated").getValue(Long.class);
                        if (lastUpdated != null) {
                            statusModel.setLastUpdated(lastUpdated);
                        } else {
                            statusModel.setLastUpdated(System.currentTimeMillis());
                        }
                        ArrayList<status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : dataSnapshot.child("statuses").getChildren()) {
                            status sampleStatus = statusSnapshot.getValue(status.class);
                            statuses.add(sampleStatus);
                        }
                        statusModel.setStatuses(statuses);
                        statusArrayList.add(statusModel);

                        Log.d("statusModel", "Added: " + statusModel.getName());
                    }
                    Log.d("StatusArrayList", "Total items: " + statusArrayList.size());
                    statusAdapter.notifyDataSetChanged();
                    shmmerview.setVisibility(View.GONE);
                } else {
                    shmmerview.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                shmmerview.setVisibility(View.GONE);
            }
        });
    }
    private void fetchUserDetails() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);
                    if (user != null) {
                        openGallery();

                    }
                } else {
                    Toast.makeText(getActivity(), "User details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
   }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                uploadImageToFirebase(selectedBitmap);
                progressDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void uploadImageToFirebase(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        statusModel statusModel = new statusModel();
        statusModel.setName(user.getName());
        statusModel.setProfileImage(encodedImage);
        statusModel.setLastUpdated(date.getTime());

        HashMap<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("name", statusModel.getName());
        objectHashMap.put("profileImage", statusModel.getProfileImage());
        objectHashMap.put("lastUpdated", statusModel.getLastUpdated());

        status status=new status(statusModel.getProfileImage(),statusModel.getLastUpdated());

        databaseReference
                .updateChildren(objectHashMap);

        databaseReference
                .child("statuses")
                .push()
                .setValue(status)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
}