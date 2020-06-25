package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    private String receivedUserId, receivedUserName, receivedUserImage;
    private String senderUserId;
    private FirebaseAuth mAuth;
    private String currentState = "new";
    private DatabaseReference friendRequestRef , contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friends Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receivedUserId = getIntent().getExtras().get("visit_user_id").toString();
        receivedUserName = getIntent().getExtras().get("visit_user_name").toString();
        receivedUserImage = getIntent().getExtras().get("visit_user_image").toString();

        binding.nameProfile.setText(receivedUserName);
        Picasso.get().load(receivedUserImage).into(binding.backgroundProfileView);

        manageClickEvents();
    }

    private void manageClickEvents() {
        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receivedUserId)){
                            String requestType = dataSnapshot.child(receivedUserId).child("request_type").getValue().toString();
                            if (requestType.equals("sent")){
                                currentState = "request_sent";
                                binding.addFriendRequest.setText("Cancel Friend Request");
                            }else if (requestType.equals("received")){
                                currentState = "request_received";
                                binding.addFriendRequest.setText("Accept Friend Request");
                                binding.declineFriendRequest.setVisibility(View.VISIBLE);
                                binding.declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        }else {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receivedUserId))
                                            {
                                                currentState = "friends";
                                                binding.addFriendRequest.setText("Delete Contact");
                                            }else {
                                                currentState="new";
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
        if (senderUserId.equals(receivedUserId)) {
            binding.addFriendRequest.setVisibility(View.GONE);
        } else {
            binding.addFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")) {
                        sendFriendRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        cancelFriendRequest();
                    }
                    if (currentState.equals("request_received")) {
                        acceptFriendRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        cancelFriendRequest();
                    }
                }
            });
        }
    }

    private void acceptFriendRequest() {
        contactsRef.child(senderUserId).child(receivedUserId)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactsRef.child(receivedUserId).child(senderUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                friendRequestRef.child(senderUserId).child(receivedUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    friendRequestRef.child(receivedUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        binding.addFriendRequest.setText("Delete Contact");
                                                                                        currentState = "friends";
                                                                                        binding.declineFriendRequest.setVisibility(View.GONE);
                                                                                        Toast.makeText(ProfileActivity.this, "You are friends now", Toast.LENGTH_SHORT).show();
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

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receivedUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                binding.addFriendRequest.setText("Add Friend");
                                                currentState = "new";
                                                Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receivedUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receivedUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState = "request_sent";
                                                binding.addFriendRequest.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}