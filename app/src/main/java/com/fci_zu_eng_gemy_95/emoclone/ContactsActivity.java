package com.fci_zu_eng_gemy_95.emoclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityContactsBinding;
import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.model.Contacts;
import com.fci_zu_eng_gemy_95.emoclone.viewHolder.ContactsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ContactsActivity extends AppCompatActivity {

    ActivityContactsBinding binding;
    private DatabaseReference contactsRef, userRef;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private String username = "", profileImage = "";
    private String calledBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        binding.navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        binding.findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactsActivity.this, FindFriendsActivity.class));
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            startActivity(new Intent(ContactsActivity.this, ContactsActivity.class));
                            break;

                        case R.id.navigation_settings:
                            startActivity(new Intent(ContactsActivity.this, SettingsActivity.class));
                            break;

                        case R.id.navigation_notifications:
                            startActivity(new Intent(ContactsActivity.this, NotificationsActivity.class));
                            break;

                        case R.id.navigation_logout:
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(ContactsActivity.this, RegisterActivity.class));
                            finish();
                            break;
                    }
                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId), Contacts.class).build();

        final FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                final String listUerId = getRef(position).getKey();

                userRef.child(listUerId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            username = dataSnapshot.child("username").getValue().toString();
                            profileImage = dataSnapshot.child("imgUri").getValue().toString();

                            holder.binding.nameContact.setText(username);
                            Picasso.get().load(profileImage).into(holder.binding.imgContact);
                        }
                        holder.binding.btnCallContact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                                callIntent.putExtra("visit_user_id",listUerId);
                                startActivity(callIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ContactsActivity.this, "Error : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ContactsDesignItemBinding binding
                        = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.contacts_design_item
                        , parent, false);
                return new ContactsViewHolder(binding);
            }
        };
        binding.recyclerViewContacts.setAdapter(adapter);
        adapter.startListening();
    }

    private void checkForReceivingCall() {
        userRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("ringing")){
                            calledBy = dataSnapshot.child("ringing").getValue().toString();

                            Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                            callingIntent.putExtra("visit_user_id",calledBy);
                            startActivity(callingIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void validateUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    startActivity(new Intent(ContactsActivity.this,SettingsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
