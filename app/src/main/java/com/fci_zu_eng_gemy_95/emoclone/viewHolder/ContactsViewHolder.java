package com.fci_zu_eng_gemy_95.emoclone.viewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;

public class ContactsViewHolder extends RecyclerView.ViewHolder {

    public ContactsDesignItemBinding binding ;

    public ContactsViewHolder(@NonNull ContactsDesignItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
