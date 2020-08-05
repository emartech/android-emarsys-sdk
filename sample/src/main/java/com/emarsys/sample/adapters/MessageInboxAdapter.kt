package com.emarsys.sample.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.sample.R
import com.emarsys.sample.TagChangeListener
import kotlinx.android.synthetic.main.notification_view_with_labels.view.*
import java.net.URL


class MessageInboxAdapter(private val tagChangeListener: TagChangeListener, private val handler: Handler) : RecyclerView.Adapter<MessageInboxAdapter.NotificationViewHolder>() {
    private var notifications = mutableListOf<Message>()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notification_view_with_labels, parent, false))
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.title.text = notifications[position].title
        holder.body.text = notifications[position].body

        val imageUrl = notifications[position].imageUrl
        if (imageUrl != null) {
            loadImageToImageViewFromUrl(imageUrl, holder.image)
        } else {
            holder.image.setImageDrawable(ContextCompat.getDrawable(holder.image.context, R.drawable.placeholder))
        }

        holder.addButton.setOnClickListener {
            tagChangeListener.addTagClicked(notifications[position].id)
        }
        holder.removeButton.setOnClickListener {
            tagChangeListener.removeTagClicked(notifications[position].id)
        }
    }

    fun addItems(notifications: List<Message>) {
        this.notifications = notifications as MutableList<Message>
        notifyDataSetChanged()

    }

    private fun loadImageToImageViewFromUrl(urlString: String, imageView: ImageView) {
        handler.post {
            val url = URL(urlString)
            val bmp: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            mainHandler.post {
                imageView.setImageBitmap(bmp)
            }
        }
    }

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.notification_title
        val body: TextView = view.notification_body
        val image: ImageView = view.notification_image
        val addButton = view.add_label_button
        val removeButton = view.remove_label_button
    }


}
