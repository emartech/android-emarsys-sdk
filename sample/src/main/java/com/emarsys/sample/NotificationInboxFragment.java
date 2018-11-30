package com.emarsys.sample;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.emarsys.Emarsys;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

import java.util.ArrayList;
import java.util.List;


public class NotificationInboxFragment extends BaseFragment {
    private static final String TAG = "InboxFragment";
    private List<Notification> notifications = new ArrayList<>();
    private NotificationListAdapter notificationListAdapter;
    private Context context;
    private TextView statusLabel;

    @Override
    public String getName() {
        return "Inbox";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        View view = inflater.inflate(R.layout.fragment_notification_inbox, container, false);
        statusLabel = view.findViewById(R.id.notificationInboxStatusLabel);

        Button refreshButton = view.findViewById(R.id.refreshButton);
        RecyclerView notificationList = view.findViewById(R.id.notificationList);

        initRecyclerView(notificationList);

        initButton(refreshButton);

        return view;
    }

    public void updateList(List<Notification> notifications) {
        this.notifications.clear();
        this.notifications.addAll(notifications);
        notificationListAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView(RecyclerView notificationList) {
        RecyclerView.LayoutManager notificationListManager = new LinearLayoutManager(context);
        notificationList.setLayoutManager(notificationListManager);

        notificationListAdapter = new NotificationListAdapter(notifications);
        notificationList.setAdapter(notificationListAdapter);
    }

    private void initButton(Button refreshButton) {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNotifications();
            }
        });
    }

    private void loadNotifications() {
        statusLabel.setText("Loading notifications: ");

        Emarsys.Inbox.fetchNotifications(new ResultListener<Try<NotificationInboxStatus>>() {
            @Override
            public void onResult(@NonNull Try<NotificationInboxStatus> result) {

                if (result.getResult() != null) {
                    NotificationInboxStatus inboxStatus = result.getResult();
                    Log.i(TAG, "Badge count: " + inboxStatus.getBadgeCount());

                    for (Notification notification : inboxStatus.getNotifications()) {
                        Log.i(TAG, "Notification: " + notification.getTitle());
                    }

                    updateList(inboxStatus.getNotifications());
                    statusLabel.append("Success");
                }
                if (result.getErrorCause() != null) {
                    Throwable cause = result.getErrorCause();
                    Log.e(TAG, "Error happened: " + cause.getMessage());
                    statusLabel.append(cause.getMessage());
                }
            }
        });
    }
}