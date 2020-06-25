package com.fci_zu_eng_gemy_95.emoclone.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fci_zu_eng_gemy_95.emoclone.R;
import com.fci_zu_eng_gemy_95.emoclone.databinding.FindFriendsItemBinding;
import com.fci_zu_eng_gemy_95.emoclone.viewHolder.NotificationsViewHolder;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationsViewHolder> {

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FindFriendsItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.find_friends_item, parent, false);
        return new NotificationsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

