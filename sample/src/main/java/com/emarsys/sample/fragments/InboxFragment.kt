package com.emarsys.sample.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.adapters.NotificationsAdapter
import com.emarsys.sample.extensions.showSnackBar
import kotlinx.android.synthetic.main.fragment_inbox.*

class InboxFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private companion object {
        val TAG: String = InboxFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationRecycleView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        notificationRecycleView.adapter = NotificationsAdapter()

        loadNotifications()

        swipeRefreshLayout.setOnRefreshListener {
            onRefresh()
        }
    }

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = true

        loadNotifications()
    }

    private fun loadNotifications() {
        Emarsys.Inbox.fetchNotifications {
            it.result?.let { notificationStatus ->
                Log.i(TAG, "Badge count: ${notificationStatus.badgeCount}")

                notificationStatus.notifications.forEach { notification ->
                    Log.i(TAG, "Notification: ${notification.title}")
                }

                (notificationRecycleView.adapter as NotificationsAdapter).addItems(notificationStatus.notifications)
            }

            it.errorCause?.let { cause ->
                inboxView.showSnackBar("Error fetching notifications: ${cause.message}")
            }
        }
        swipeRefreshLayout.isRefreshing = false
    }
}
