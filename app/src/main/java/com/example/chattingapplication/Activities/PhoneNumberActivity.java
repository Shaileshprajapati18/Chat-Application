package com.example.chattingapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chattingapplication.R;
import com.example.chattingapplication.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        binding.phoneBox.requestFocus();
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
        Intent intent=new Intent(PhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.continueBtn.setOnClickListener(view -> {
            if(binding.phoneBox.getText().toString().isEmpty()){
                binding.phoneBox.setError("Phone number is required");
            }
            else if(binding.phoneBox.getText().toString().length()<13 || binding.phoneBox.getText().toString().length()>13){
                binding.phoneBox.setError("Please enter a valid phone number");
            }
            else if(!binding.phoneBox.getText().toString().startsWith("+91")){
                binding.phoneBox.setError("Phone number must start with +91");
            }
            else if(!binding.phoneBox.getText().toString().substring(1).matches("\\d+")){
                binding.phoneBox.setError("Please enter a valid phone number");
            }
            else {
                String phoneNumber = binding.phoneBox.getText().toString();
                Intent intent=new Intent(PhoneNumberActivity.this,OTPActivity.class);
                intent.putExtra("phoneNumber",phoneNumber);
                startActivity(intent);
            }
        });
    }
}