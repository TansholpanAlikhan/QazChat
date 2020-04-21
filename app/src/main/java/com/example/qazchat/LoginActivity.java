package com.example.qazchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Button mLoginButton,mPhoneLoginButton;
    private EditText mUserEmail,mUserPassword;
    private TextView mRegisterLink,mForgotPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitializeFields();
        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });
        mForgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });
        mPhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPhoneLoginActivity();
            }
        });
    }

    private void SendUserToPhoneLoginActivity() {
        Intent phoneLoginIntent = new Intent(this,PhoneLoginActivity.class);
        startActivity(phoneLoginIntent);
    }

    private void AllowUserToLogin() {
        String email = mUserEmail.getText().toString();
        String password = mUserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password...",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Signing in...");
            loadingBar.setMessage("Please wait, while we are checking...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        final String currentUserID = mAuth.getCurrentUser().getUid();
                        mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if(task.isSuccessful()){
                                    String deviceToken = task.getResult().getToken();
                                    usersRef.child(currentUserID).child("device_token")
                                            .setValue(deviceToken)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        SendUserToMainActivity();
                                                        Toast.makeText(LoginActivity.this,"Logged in successfully!",Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error : " + message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void InitializeFields() {
        mLoginButton = findViewById(R.id.login_button);
        mPhoneLoginButton = findViewById(R.id.phone_login_button);
        mUserEmail = findViewById(R.id.login_email);
        mUserPassword = findViewById(R.id.login_password);
        mRegisterLink = findViewById(R.id.register_link);
        mForgotPasswordLink = findViewById(R.id.forget_password_link);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}
