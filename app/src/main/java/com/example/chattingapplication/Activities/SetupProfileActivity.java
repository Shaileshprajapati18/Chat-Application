package com.example.chattingapplication.Activities;

import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.Model.UserDao;
import com.example.chattingapplication.Model.UserDatabase;
import com.example.chattingapplication.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    public static final int PICK_IMAGE = 1;
    Bitmap selectedBitmap;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        auth=FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();

        binding.continueBtn.setOnClickListener(view -> {

            String name=binding.nameBox.getText().toString();
            if (name.isEmpty()){
                binding.nameBox.setError("Please type your name");
                return;
            }
            if (selectedBitmap==null){
                Toast.makeText(SetupProfileActivity.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImageToFirebase(selectedBitmap);

        });
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            binding.imageView.setImageURI(imageUri);
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(SetupProfileActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void uploadImageToFirebase(Bitmap selectedBitmap) {
        progressDialog.show();

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imageBytes=byteArrayOutputStream.toByteArray();
        String encodedImage= Base64.encodeToString(imageBytes,Base64.DEFAULT);

        String uid=auth.getUid();
        String name=binding.nameBox.getText().toString();
        String phoneNumber=auth.getCurrentUser().getPhoneNumber();

        User user=new User(uid,name,phoneNumber,encodedImage);

        reference.child("Users").child(uid).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                            Intent intent=new Intent(SetupProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();
                            finish();
                    }
                });
    }
}