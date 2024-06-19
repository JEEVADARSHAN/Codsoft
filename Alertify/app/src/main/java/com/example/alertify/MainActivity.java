package com.example.alertify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int NOTIFICATION_PERMISSION_CODE = 2;
    private EditText editText;
    private Button viewBtn;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    Button selectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        selectBtn = findViewById(R.id.selectImageBtn);
        viewBtn = findViewById(R.id.showNotificationBtn);

        ImageButton clearImageBtn = findViewById(R.id.clearImageBtn);
        clearImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
            }
        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImages();
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkData()) {
                    if (checkPermission()) {
                        confirmDialog();
                    }
                } else {
                    displayAlert();
                }
            }
        });
    }

    private boolean checkData() {
        return !editText.getText().toString().isEmpty() && selectedImageBitmap != null;
    }

    private void displayAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Not Enough Data")
                .setMessage("Insufficient Data, Would you like to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNotification();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Notification")
                .setMessage("Set notification?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNotification();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification();
            }
        }
    }

    private void resetImage() {
        selectedImageBitmap = null;

        CircleImageView imagePreview = findViewById(R.id.imagePreview);
        imagePreview.setImageResource(R.drawable.images);
    }

    private void openImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                selectedImageBitmap = BitmapFactory.decodeFileDescriptor(
                        getContentResolver().openFileDescriptor(selectedImageUri, "r").getFileDescriptor());

                // Set the selected image to the ImageView for preview
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageBitmap(selectedImageBitmap);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showNotification() {
        String notificationText = editText.getText().toString();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        String channelId = "notification_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notify Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the intent to remove the notification
        Intent removeNotificationIntent;
        removeNotificationIntent = new Intent(this, Receiver.class);
        removeNotificationIntent.setAction("REMOVE_NOTIFICATION");
        PendingIntent removeNotificationPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                removeNotificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap notificationImage = selectedImageBitmap;
        if (notificationImage == null) {
            notificationImage = BitmapFactory.decodeResource(getResources(), R.drawable.images);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.alertify)
                .setContentTitle("Notify App Notification")
                .setContentText(notificationText)
                .setLargeIcon(notificationImage)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(notificationImage)
                        .bigLargeIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? null : (Bitmap) null))
                .addAction(R.drawable.ic_launcher_foreground, "Dismiss", removeNotificationPendingIntent);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

}