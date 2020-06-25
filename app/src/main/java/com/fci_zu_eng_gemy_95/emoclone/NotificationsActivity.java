package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityNotificationsBinding;
import com.fci_zu_eng_gemy_95.emoclone.databinding.FindFriendsItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.model.Contacts;
import com.fci_zu_eng_gemy_95.emoclone.viewHolder.NotificationsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private DatabaseReference friendRequestRef, contactsRef, userRef;
    private String currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friends Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        binding.recyclerViewNotification.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerViewNotification.setHasFixedSize(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef.child(currentUserId), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull Contacts model) {
                holder.findFriendsItemBinding.btnRequestAccept.setVisibility(View.VISIBLE);
                holder.findFriendsItemBinding.btnRequestDecline.setVisibility(View.VISIBLE);

                final String listUserId = getRef(position).getKey();
                DatabaseReference requestType = getRef(position).child("request_type").getRef();
                requestType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received")) {
                                holder.findFriendsItemBinding.cardView.setVisibility(View.VISIBLE);
                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("imgUri")) {
                                            final String imageUri = dataSnapshot.child("imgUri").getValue().toString();

                                            Picasso.get().load(imageUri).into(holder.findFriendsItemBinding.imgPeople);
                                        }
                                        final String name = dataSnapshot.child("username").getValue().toString();
                                        holder.findFriendsItemBinding.name.setText(name);

                                        holder.findFriendsItemBinding.btnRequestAccept
                                                .setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        acceptFriendRequest(listUserId);
                                                    }
                                                });
                                        holder.findFriendsItemBinding.btnRequestDecline
                                                .setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancelFriendRequest(listUserId);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                holder.findFriendsItemBinding.cardView.setVisibility(View.GONE);
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
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                FindFriendsItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.find_friends_item, parent, false);
                return new NotificationsViewHolder(binding);
            }
        };
        binding.recyclerViewNotification.setAdapter(adapter);
        adapter.startListening();
    }

    private void acceptFriendRequest(final String listUserId) {
        contactsRef.child(currentUserId).child(listUserId)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(listUserId).child(currentUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                friendRequestRef.child(currentUserId).child(listUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendRequestRef.child(listUserId).child(currentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(NotificationsActivity.this, "You are friends now", Toast.LENGTH_SHORT).show();
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

    private void cancelFriendRequest(final String listUserId) {
        friendRequestRef.child(currentUserId).child(listUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(listUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(NotificationsActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
