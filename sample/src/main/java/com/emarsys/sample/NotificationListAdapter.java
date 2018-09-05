package com.emarsys.sample;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.emarsys.mobileengage.api.inbox.Notification;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public TextView title;
        public TextView body;
        public TextView receivedAt;
        public ImageView imageView;

        public ViewHolder(ConstraintLayout root) {
            super(root);
            context = root.getContext();
            title = root.findViewById(R.id.adapterNotificationTitle);
            body = root.findViewById(R.id.adapterNotificationBody);
            receivedAt = root.findViewById(R.id.receivedAt);
            imageView = root.findViewById(R.id.imageview);
        }
    }

    List<Notification> notifications;
    DateFormat dateFormat;

    public NotificationListAdapter(List<Notification> notifications) {
        this.notifications = notifications;
        this.dateFormat = DateFormat.getDateTimeInstance();
    }

    @Override
    public NotificationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout root = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_notification_list_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(NotificationListAdapter.ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.title.setText(notification.getTitle());
        holder.body.setText(notification.getBody());
        String dateString = dateFormat.format(new Date(notification.getReceivedAt()));
        holder.receivedAt.setText("Received at " + dateString);

        String imageUrl = notification.getCustomData().get("image");
        if (imageUrl != null) {
            Picasso.with(holder.context).load(imageUrl).into(holder.imageView);
        } else {
            Picasso.with(holder.context).load(R.drawable.ic_image_black).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
