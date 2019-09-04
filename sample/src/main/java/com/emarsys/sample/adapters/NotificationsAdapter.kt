package com.emarsys.sample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.sample.R
import kotlinx.android.synthetic.main.notification_view.view.*

class NotificationsAdapter : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {
    private var notifications = mutableListOf<Notification>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notification_view, parent, false))
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.title.text = notifications[position].title
        holder.body.text = notifications[position].body
        holder.image.text = notifications[position].customData["image_url"] ?: "no image"
    }

    fun addItems(notifications: List<Notification>) {
        this.notifications = notifications as MutableList<Notification>
        notifyDataSetChanged()

    }

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.notification_title
        val body: TextView = view.notification_body
        val image: TextView = view.notification_image
    }

}
