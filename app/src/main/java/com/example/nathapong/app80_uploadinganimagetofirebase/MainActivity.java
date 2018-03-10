package com.example.nathapong.app80_uploadinganimagetofirebase;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button btnUploadImage;
    private ImageView imgDownload;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ProgressDialog downloadProgressDialog;
    private Uri downloadUri;

    private MediaRecorder mediaRecorder;
    private String audioFile;

    private Button btnRecordAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUploadImage =(Button)findViewById(R.id.btnUploadImage);
        imgDownload = (ImageView)findViewById(R.id.imgDownload);
        btnRecordAudio = (Button)findViewById(R.id.btnRecordAudio);

        downloadProgressDialog = new ProgressDialog(MainActivity.this);


        int permissionResult = ContextCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionResult2 = ContextCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.RECORD_AUDIO);

        if (permissionResult == PackageManager.PERMISSION_GRANTED
                && permissionResult2 == PackageManager.PERMISSION_GRANTED){

            Toast.makeText(MainActivity.this,
                    "Thank You for giving the Permission to Access External Storage " +
                            "and Audio Recorder!", Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},2);
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage
                .getReferenceFromUrl("gs://app80uploadinganimagetofirebas.appspot.com");



        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Upload Image to the server

                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                imageIntent.setType("image/");
                startActivityForResult(imageIntent, 1);
            }
        });


        audioFile = Environment.getExternalStorageDirectory().getAbsolutePath();
        audioFile = audioFile + "/audioFile.3gp";


        btnRecordAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    startRecordingAudio();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){

                    stopRecordingAudio();
                }

                return false;
            }
        });



    }   //  onCreate Method




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case 1 :

                if (resultCode == RESULT_OK){

                    downloadProgressDialog.setMessage("Downloading Image from Firebase...");
                    downloadProgressDialog.show();

                    Uri imageURI = data.getData();

                    StorageReference imageAddressInFirebase =
                            storageReference.child("Images").child(imageURI.getLastPathSegment());


                    imageAddressInFirebase.putFile(imageURI)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(MainActivity.this,
                                    "Uploading was successful!", Toast.LENGTH_SHORT).show();

                            downloadUri = taskSnapshot.getDownloadUrl();
                            Picasso.with(MainActivity.this).load(downloadUri).fit().centerCrop()
                                    .into(imgDownload);
                            downloadProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this,
                                    "Uploading was not successful!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    }


    private void startRecordingAudio(){

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();

        }catch (Exception e){}

        mediaRecorder.start();
    }


    private void stopRecordingAudio(){

        if (mediaRecorder != null){

            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            uploadTheAudioToFirebase();
        }
    }

    private void uploadTheAudioToFirebase(){


        downloadProgressDialog.setMessage("Uploading the audio to Firebase...");
        downloadProgressDialog.show();

        StorageReference audioFileAddress =
                storageReference.child("Audio Files").child("MyAudioFile.3gp");

        Uri audioFileUri = Uri.fromFile(new File(audioFile));


        audioFileAddress.putFile(audioFileUri).addOnSuccessListener
                (new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                downloadProgressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


}
