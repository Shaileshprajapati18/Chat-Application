package com.example.chattingapplication.Activities;

import static java.security.AccessController.getContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chattingapplication.Adapter.MessagesAdapter;
import com.example.chattingapplication.Model.Base64ToImageConverter;
import com.example.chattingapplication.Model.Message;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.R;
import com.example.chattingapplication.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    DatabaseReference databaseReference;
    ArrayList<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("chats");

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.main_color));

        String username = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String profileImagePath = getIntent().getStringExtra("profileImage");

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            File imageFile = new File(profileImagePath);
            if (imageFile.exists()) {
                binding.profile.setImageURI(Uri.fromFile(imageFile));
            } else {
                binding.profile.setImageResource(R.drawable.avatar); // Default image
            }
        } else {
            binding.profile.setImageResource(R.drawable.avatar);
        }

        String senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        databaseReference.child(senderRoom)
                        .child("messages")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        messages.clear();
                                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                                            Message message= snapshot1.getValue(Message.class);
                                            message.setMessageId(snapshot1.getKey());
                                            messages.add(message);
                                        }
                                        adapter.notifyDataSetChanged();
                                        binding.recyclerView.scrollToPosition(messages.size() - 1);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String messageTxt = binding.messageBox.getText().toString();

                Date date= new Date();
                Message message= new Message(messageTxt ,senderUid ,date.getTime());
                binding.messageBox.setText("");

                String messagePushId = databaseReference.push().getKey();

                databaseReference.child(senderRoom)
                        .child("messages")
                        .child(messagePushId)
                        .setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                databaseReference.child(receiverRoom)
                                        .child("messages")
                                        .child(messagePushId)
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                            }
                        });
            }
        });

        binding.name.setText(username);
    }
}