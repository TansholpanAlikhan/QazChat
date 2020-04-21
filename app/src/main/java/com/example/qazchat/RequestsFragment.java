package com.example.qazchat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View requestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference requestsRef,usersRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        mAuth = FirebaseAuth.getInstance();
        myRequestsList = requestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        requestsRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        currentUserID = mAuth.getCurrentUser().getUid();
        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(requestsRef.child(currentUserID),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull final Contacts model) {
                                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.VISIBLE);
                              final String list_user_id = getRef(position).getKey();
                              DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                              getTypeRef.addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      if(dataSnapshot.exists()){
                                          String type = dataSnapshot.getValue().toString();
                                          if(type.equals("received")){
                                              usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                      if(dataSnapshot.hasChild("image")){
                                                          String userImage = dataSnapshot.child("image").getValue().toString();
                                                          Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                      }
                                                      final String userName = dataSnapshot.child("name").getValue().toString();
                                                      String userStatus = dataSnapshot.child("status").getValue().toString();

                                                      holder.userName.setText(userName);
                                                      holder.userStatus.setText(userName + " wants to chat with you!");
                                                      holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View view) {
                                                              CharSequence options[] = new CharSequence[]{
                                                                      "Accept",
                                                                      "Cancel"
                                                              };
                                                              AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                              builder.setTitle(userName+" chat request");
                                                              builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                                        if(i==0){
                                                                            contactsRef.child(currentUserID).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        contactsRef.child(list_user_id).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    requestsRef.child(currentUserID).child(list_user_id)
                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                requestsRef.child(list_user_id).child(currentUserID)
                                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if(task.isSuccessful()){
                                                                                                                            Toast.makeText(getContext(),"Contact saved successfully!",Toast.LENGTH_SHORT).show();
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
                                                                            });
                                                                        }
                                                                        else if(i==1){
                                                                            requestsRef.child(currentUserID).child(list_user_id)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        requestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(),"Contact was deleted!",Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                  }
                                                              });
                                                              builder.show();
                                                          }
                                                      });
                                                  }

                                                  @Override
                                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                                  }
                                              });
                                          }
                                          else if(type.equals("sent")){
                                              Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                              request_sent_btn.setText("Request sent");
                                              holder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);
                                              usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                      if(dataSnapshot.hasChild("image")){
                                                          String userImage = dataSnapshot.child("image").getValue().toString();
                                                          Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                      }
                                                      final String userName = dataSnapshot.child("name").getValue().toString();
                                                      String userStatus = dataSnapshot.child("status").getValue().toString();

                                                      holder.userName.setText(userName);
                                                      holder.userStatus.setText("You've sent request to "+userName);
                                                      holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View view) {
                                                              CharSequence options[] = new CharSequence[]{
                                                                      "Cancel Chat request"
                                                              };
                                                              AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                              builder.setTitle("Already sent request");
                                                              builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                                      if(i==0){
                                                                          requestsRef.child(currentUserID).child(list_user_id)
                                                                                  .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                              @Override
                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                  if(task.isSuccessful()){
                                                                                      requestsRef.child(list_user_id).child(currentUserID)
                                                                                              .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                          @Override
                                                                                          public void onComplete(@NonNull Task<Void> task) {
                                                                                              if(task.isSuccessful()){
                                                                                                  Toast.makeText(getContext(),"You've cancelled your chat request!",Toast.LENGTH_SHORT).show();
                                                                                              }
                                                                                          }
                                                                                      });
                                                                                  }
                                                                              }
                                                                          });
                                                                      }
                                                                  }
                                                              });
                                                              builder.show();
                                                          }
                                                      });
                                                  }

                                                  @Override
                                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                                  }
                                              });
                                          }
                                      }
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                        return requestViewHolder;
                    }
                };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,declineButton;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            declineButton = itemView.findViewById(R.id.request_decline_btn);
        }
    }
}
