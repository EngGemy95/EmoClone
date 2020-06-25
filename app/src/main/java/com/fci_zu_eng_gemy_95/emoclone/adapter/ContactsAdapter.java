package com.fci_zu_eng_gemy_95.emoclone.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fci_zu_eng_gemy_95.emoclone.R;
import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.viewHolder.ContactsViewHolder;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsViewHolder> {

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactsDesignItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.contacts_design_item, parent, false);
        return new ContactsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

}

