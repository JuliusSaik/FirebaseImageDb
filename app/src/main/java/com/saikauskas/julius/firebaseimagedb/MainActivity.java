package com.saikauskas.julius.firebaseimagedb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Button bttnGetImg, bttnSendImg;
    ImageView pic;
    TextView progress;
    public static final int IMAGE_RQST_CODE = 1;

    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bttnGetImg = findViewById(R.id.bttnGetImages);
        bttnSendImg = findViewById(R.id.bttnSendImages);
        progress = findViewById(R.id.progress);

        pic = findViewById(R.id.ivPic);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();



    }


    public void loadImageAndSend(View view){
        Intent getImageIntent = new Intent(Intent.ACTION_PICK);
        getImageIntent.setType("image/*");
        startActivityForResult(getImageIntent, IMAGE_RQST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_RQST_CODE && resultCode == RESULT_OK && data != null){
            Uri selectImage = data.getData();
            InputStream inputStream = null;
            try {
                assert selectImage != null;
                inputStream = getContentResolver().openInputStream(selectImage);
                //Toast.makeText(this, "selected", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BitmapFactory.decodeStream(inputStream);

            sendImgToDatabase(selectImage);

        }
        else{
            Toast.makeText(this, "No image sent, please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendImgToDatabase(Uri imageToSend){

        StorageReference imageRef = storageRef.child("images/" + imageToSend.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(imageToSend);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                long progressPerc = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progress.setText("Upload is " + progressPerc + "% done");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata();
                Toast.makeText(MainActivity.this, "Image has been successfully sent!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}