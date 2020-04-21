package com.example.qazchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mUpdateSettings;
    private EditText mUserName,mUserStatus;
    private CircleImageView mUserProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference rootRef;
    private static final int galleryPick = 1;
    private StorageReference mUserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InitializeFields();
        mUpdateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
        RetrieveUserData();
        mUserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mUserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                loadingBar.setTitle("Uploading profile image");
                loadingBar.setMessage("Please wait,while we are uploading your profile image...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                final StorageReference filePath = mUserProfileImagesRef.child(mUser.getUid()+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile image uploaded successfully!",Toast.LENGTH_SHORT).show();
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    rootRef.child("Users").child(mUser.getUid()).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                loadingBar.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Image saved successfully!",Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                loadingBar.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Error:"+task.getException().toString(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else{
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    private void RetrieveUserData() {
        rootRef.child("Users").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image") && dataSnapshot.hasChild("status")){
                    String retrievedName = (String) dataSnapshot.child("name").getValue().toString();
                    String retrievedStatus = dataSnapshot.child("status").getValue().toString();
                    String retrievedProfileImage = (String) dataSnapshot.child("image").getValue().toString();
                    mUserName.setText(retrievedName);
                    mUserStatus.setText(retrievedStatus);
                    Picasso.get().load(retrievedProfileImage).into(mUserProfileImage);
                }
                else if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")){
                    String retrievedName = (String) dataSnapshot.child("name").getValue().toString();
                    String retrievedStatus = dataSnapshot.child("status").getValue().toString();
                    mUserName.setText(retrievedName);
                    mUserStatus.setText(retrievedStatus);
                }
                else{
                    Toast.makeText(SettingsActivity.this,"Please set your profile information...",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UpdateSettings() {
        String setUserName = mUserName.getText().toString();
        String setStatus = mUserStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Please write your Username...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"Please write your status...",Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid",mUser.getUid());
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            rootRef.child("Users").child(mUser.getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this,"Profile updated successfully!",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this,"Error:" + message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this,MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeFields() {
        mUpdateSettings = findViewById(R.id.update_settings_button);
        mUserName = findViewById(R.id.set_user_name);
        mUserStatus = findViewById(R.id.set_profile_status);
        mUserProfileImage = findViewById(R.id.set_profile_image);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(SettingsActivity.this);
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }
}
