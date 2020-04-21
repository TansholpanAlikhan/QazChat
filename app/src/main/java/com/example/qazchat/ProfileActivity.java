package com.example.qazchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, currentUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;
    private DatabaseReference userRef, chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUserID);
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);
        currentState = "new";
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("image")) {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRequestRef.child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.child(receiverUserID).child("request_type").exists()){
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                currentState = "request_sent";
                                sendMessageRequestButton.setText("Cancel chat request");
                            }
                            else if(request_type.equals("received")){
                                currentState = "request_received";
                                sendMessageRequestButton.setText("Accept chat request");
                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);
                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            contactsRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserID)){
                                        currentState = "friends";
                                        sendMessageRequestButton.setText("Remove from contacts");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if(!currentUserID.equals(receiverUserID)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);
                    if(currentState.equals("new")){
                        SendChatRequest();
                    }
                    if(currentState.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else{
            sendMessageRequestButton.setVisibility(View.GONE);
        }
    }

    private void RemoveSpecificContact() {
        contactsRef.child(currentUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequestButton.setEnabled(true);
                                            sendMessageRequestButton.setText("Send message");
                                            currentState = "new";
                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            declineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        contactsRef.child(currentUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        contactsRef.child(receiverUserID).child(currentUserID)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            chatRequestRef.child(currentUserID).child(receiverUserID).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                chatRequestRef.child(receiverUserID).child(currentUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                sendMessageRequestButton.setEnabled(true);
                                                                                currentState = "friends";
                                                                                sendMessageRequestButton.setText("Remove from contacts");
                                                                                declineMessageRequestButton.setVisibility(View.GONE);
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequestButton.setEnabled(true);
                                            sendMessageRequestButton.setText("Send message");
                                            currentState = "new";
                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            declineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        chatRequestRef.child(currentUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(currentUserID).child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from",currentUserID);
                                                chatNotification.put("type","request");
                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    sendMessageRequestButton.setText("Cancel chat request");
                                                                    currentState = "request_sent";
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
