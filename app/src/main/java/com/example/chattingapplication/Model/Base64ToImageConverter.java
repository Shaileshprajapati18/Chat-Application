package com.example.chattingapplication.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Base64ToImageConverter {
    public static File convertBase64ToImage(Context context, String base64String, String fileName) {
        // Return null early if the base64 string is invalid
        if (base64String == null || base64String.isEmpty()) {
            Log.e("Base64ToImage", "Base64 string is empty or null");
            return null;
        }

        try {
            // Decode Base64 string to byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            if (decodedBytes.length == 0) {
                Log.e("Base64ToImage", "Decoded byte array is empty");
                return null;
            }

            // Convert byte array to bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap == null) {
                Log.e("Base64ToImage", "Failed to decode Base64 to bitmap");
                return null;
            }

            // Save image in internal storage (app-private storage)
            File imageFile = new File(context.getCacheDir(), fileName); // Use cache directory
            FileOutputStream fos = new FileOutputStream(imageFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // Recycle the bitmap to free up memory
            bitmap.recycle();

            if (!success) {
                Log.e("Base64ToImage", "Failed to compress and save bitmap");
                return null;
            }

            Log.d("Base64ToImage", "Image successfully saved at: " + imageFile.getAbsolutePath());
            return imageFile;

        } catch (IOException | IllegalArgumentException e) {
            Log.e("Base64ToImage", "Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
