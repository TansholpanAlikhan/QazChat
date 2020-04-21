package com.example.qazchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private EditText mUserMessageInput;
    private ScrollView mScrollView;
    private TextView mDisplayTextMessage;
    private String currentGroupName,mCurrentUserID,mCurrentUserName,mCurrentDate,mCurrentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef,mGroupsRef,mGroupMessageKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        mAuth = FirebaseAuth.getInstance();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        GetUserInfo();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        mGroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                mUserMessageInput.setText("");
            }
        });
        InitializeFields();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGroupMessageKeyRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            mDisplayTextMessage.append(chatName+":\n"+chatMessage+"\n"+chatTime+"   "+chatDate+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMessageInfoToDatabase() {
        String message = mUserMessageInput.getText().toString();
        String messageKey = mGroupsRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"Please write message first...",Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            mCurrentDate = currentDateFormat.format(calForDate.getTime());
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            mCurrentDate = currentTimeFormat.format(calForTime.getTime());
            HashMap<String, Object> groupMessageKey = new HashMap<>();
            mGroupsRef.updateChildren(groupMessageKey);
            mGroupMessageKeyRef = mGroupsRef.child(messageKey);
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",mCurrentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",mCurrentDate);
            messageInfoMap.put("time",mCurrentTime);
            mGroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void GetUserInfo() {
        mUsersRef.child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mCurrentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        mToolbar = findViewById(R.id.appBarLayout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        mSendMessageButton = findViewById(R.id.send_message_button);
        mUserMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
        mDisplayTextMessage = findViewById(R.id.group_chat_text_display);
    }
}
