package com.fci_zu_eng_gemy_95.emoclone.viewHolder;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ContactsDesignItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.databinding.FindFriendsItemBinding;

public class NotificationsViewHolder extends RecyclerView.ViewHolder {

    public FindFriendsItemBinding findFriendsItemBinding;

    public NotificationsViewHolder(@NonNull FindFriendsItemBinding itemView) {
        super(itemView.getRoot());
        findFriendsItemBinding = itemView;
    }
}
