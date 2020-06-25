package com.fci_zu_eng_gemy_95.emoclone.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.databinding.FindFriendsItemBinding;

public class FindPeopleViewHolder extends RecyclerView.ViewHolder {

    public ContactsDesignItemBinding contactsDesignItemBinding ;

    public FindPeopleViewHolder(@NonNull ContactsDesignItemBinding itemView) {
        super(itemView.getRoot());
        contactsDesignItemBinding = itemView;

        contactsDesignItemBinding.btnCallContact.setVisibility(View.GONE);
    }
}
