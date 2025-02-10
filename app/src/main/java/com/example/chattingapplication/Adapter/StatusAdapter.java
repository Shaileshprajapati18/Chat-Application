package com.example.chattingapplication.Adapter;

import static com.example.chattingapplication.Model.TimeConverter.convertMillisToTime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devlomi.circularstatusview.CircularStatusView;
import com.example.chattingapplication.Activities.MainActivity;
import com.example.chattingapplication.Model.User;
import com.example.chattingapplication.Model.status;
import com.example.chattingapplication.Model.statusModel;
import com.example.chattingapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    private final Context context;
    private final ArrayList<statusModel> statusArrayList;
    private User user;

    public StatusAdapter(Context context, ArrayList<statusModel> statusArrayList) {
        this.context = context;
        this.statusArrayList = statusArrayList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_item, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        statusModel statusModel = statusArrayList.get(position);

        String profileImage = statusModel.getProfileImage();
        loadProfileImage(holder.circleImageView, profileImage);

        holder.circularStatusView.setPortionsCount(statusModel.getStatuses().size());

        holder.circularStatusView.setOnClickListener(view -> showStoryView(holder, statusModel));
        holder.name.setText(statusModel.getName());

        long timestamp= statusModel.getLastUpdated();
        String time = convertMillisToTime(timestamp);
        holder.lastUpdated.setText(time);

    }

    @Override
    public int getItemCount() {
        return statusArrayList.size();
    }

    private void loadProfileImage(CircleImageView imageView, String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Glide.with(context).load(bitmap).into(imageView);
            } catch (IllegalArgumentException e) {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void showStoryView(StatusViewHolder holder, statusModel statusModel) {
        ArrayList<MyStory> myStories = new ArrayList<>();

        for (status status : statusModel.getStatuses()) {
            String imageUrl = status.getImageUrl();
            if (imageUrl.startsWith("http")) {
                myStories.add(new MyStory(imageUrl));
            } else {
                Bitmap bitmap = decodeBase64ToBitmap(imageUrl);
                if (bitmap != null) {
                    Uri imageUri = saveBitmapToCache(context, bitmap);
                    if (imageUri != null) {
                        myStories.add(new MyStory(imageUri.toString()));
                    }
                }
            }
        }
        fetchUserDetails(() -> {
            String profileImage = user != null ? user.getProfileImage() : null;
            String titleLogoUri = getProfileImageUri(profileImage);

            new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                    .setStoriesList(myStories)
                    .setStoryDuration(5000)
                    .setTitleText(statusModel.getName())
                    .setSubtitleText("")
                    .setTitleLogoUrl(titleLogoUri)
                    .setStoryClickListeners(new StoryClickListeners() {
                        @Override
                        public void onDescriptionClickListener(int position) {

                        }

                        @Override
                        public void onTitleIconClickListener(int position) {
                        }
                    })
                    .build()
                    .show();
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        try {
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getProfileImageUri(String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = decodeBase64ToBitmap(base64Image);
            Uri imageUri = saveBitmapToCache(context, bitmap);
            if (imageUri != null) {
                return imageUri.toString();
            }
        }
        return null;
    }

    private Uri saveBitmapToCache(Context context, Bitmap bitmap) {
        try {
            File cacheDir = new File(context.getCacheDir(), "images");
            cacheDir.mkdirs(); // Create directory if not exists
            File file = new File(cacheDir, "story_image_" + System.currentTimeMillis() + ".png");

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchUserDetails(Runnable callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);
                    callback.run();
                } else {
                    Log.d("StatusAdapter", "User details not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StatusAdapter", "Failed to fetch user details", error.toException());
            }
        });
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        CircularStatusView circularStatusView;
        TextView name,lastUpdated;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.circleImageView);
            circularStatusView = itemView.findViewById(R.id.circular_status_view);
            name = itemView.findViewById(R.id.name);
            lastUpdated = itemView.findViewById(R.id.time);
        }
    }
}
