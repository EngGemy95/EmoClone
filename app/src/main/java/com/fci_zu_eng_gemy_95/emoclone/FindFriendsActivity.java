package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityFindPeopleBinding;
import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.model.Contacts;
import com.fci_zu_eng_gemy_95.emoclone.viewHolder.FindPeopleViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private ActivityFindPeopleBinding binding;
    DatabaseReference userRef;
    private String str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_people);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        binding.recyclerViewFindFriends.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerViewFindFriends.setHasFixedSize(true);

        binding.searchUserTextFindFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSeq, int start, int before, int count) {
                if (binding.searchUserTextFindFriends.getText().toString().equals("")) {
                    Toast.makeText(FindFriendsActivity.this, "Please write name to search ", Toast.LENGTH_SHORT).show();
                } else {
                    str = charSeq.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options;
        if (str.equals("")) {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef, Contacts.class).build();
        } else {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef.orderByChild("username")
                                    .startAt(str)
                                    .endAt(str + "\uf8ff")
                            , Contacts.class)
                    .build();
        }
        FirebaseRecyclerAdapter<Contacts, FindPeopleViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, FindPeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindPeopleViewHolder holder, final int position, @NonNull final Contacts model) {
                holder.contactsDesignItemBinding.nameContact.setText(model.getUsername());
                Picasso.get().load(model.getImgUri()).into(holder.contactsDesignItemBinding.imgContact);

                holder.contactsDesignItemBinding.layoutPersonDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent intent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        intent.putExtra("visit_user_name",model.getUsername());
                        intent.putExtra("visit_user_image",model.getImgUri());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ContactsDesignItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.contacts_design_item,
                        parent, false);
                return new FindPeopleViewHolder(binding);
            }
        };
        binding.recyclerViewFindFriends.setAdapter(adapter);
        adapter.startListening();
    }
}
