package com.example.chattingapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.chattingapplication.Activities.ChatActivity;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final Context context;
    private final ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.textView.setText(user.getName());

        // Load image properly from file storage
        loadImage(user.getProfileImage(), holder.circleImageView);

        holder.itemView.setOnClickListener(view -> {
            // Use the main thread for UI-related actions
            new Handler(Looper.getMainLooper()).post(() -> handleItemClick(user, holder));
        });
    }

    private void loadImage(String imagePath, CircleImageView imageView) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.avatar)
                        .skipMemoryCache(true) // Forces Glide to reload image
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Prevents old cache usage
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.avatar);
            }
        } else {
            imageView.setImageResource(R.drawable.avatar);
        }
    }

    private void handleItemClick(User user, UserViewHolder holder) {
        try {
            String base64Image = user.getProfileImage();
            Bitmap bitmap = null;
            String imagePath = null;

            if (base64Image != null && !base64Image.isEmpty()) {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            }

            if (bitmap != null) {
                imagePath = saveImageToCache(bitmap);
            }

            // Only use the cached image or original path if the cache saving fails
            String finalImagePath = imagePath != null ? imagePath : user.getProfileImage();

            // Use Intent to send data to ChatActivity
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            intent.putExtra("profileImage", finalImagePath); // Pass the final image path

            context.startActivity(intent);

            // Notify UI if any changes (if needed)
            int index = users.indexOf(user);
            if (index != -1) {
                notifyItemChanged(index);
            }
        } catch (Exception e) {
            Log.e("UsersAdapter", "Error handling item click", e);
        }
    }

    private String saveImageToCache(Bitmap bitmap) {
        if (context != null) {
            try {
                File cacheDir = context.getCacheDir();
                File file = new File(cacheDir, "profile_image.png");

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }

                return file.getAbsolutePath();
            } catch (IOException e) {
                Log.e("UsersAdapter", "Error saving image to cache", e);
                return null;
            }
        } else {
            Log.e("UsersAdapter", "Context is null, cannot save image to cache");
            return null;
        }
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CircleImageView circleImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.username);
            circleImageView = itemView.findViewById(R.id.profile);
        }
    }
}
