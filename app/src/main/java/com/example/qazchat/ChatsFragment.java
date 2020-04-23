package com.example.qazchat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private Toolbar toolbar;
    private View privateChatsView;
    private RecyclerView chatList;
    private DatabaseReference chatsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        chatList = privateChatsView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        if(mAuth.getCurrentUser()!=null){
            currentUserID = mAuth.getCurrentUser().getUid();
            chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        }
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("image")) {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);
                                    holder.userStatus.setText("Last seen: \n" + "Date " + "Time");

                                    if(dataSnapshot.child("userState").hasChild("state")){
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                        if(state.equals("online")){
                                            holder.userStatus.setText("Online");
                                        }
                                        else if(state.equals("offline")){
                                            holder.userStatus.setText("Last seen: " + date +" "+ time);
                                        }
                                    }
                                    else{
                                        holder.userStatus.setText("Offline");
                                    }
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",usersIDs);
                                            chatIntent.putExtra("visit_user_name",retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName;
        TextView userStatus;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}
