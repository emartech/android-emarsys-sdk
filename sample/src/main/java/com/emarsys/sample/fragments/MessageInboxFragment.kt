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
import com.emarsys.inbox.InboxTag
import com.emarsys.mobileengage.api.inbox.Message
import com.emarsys.sample.TagChangeListener
import com.emarsys.sample.adapters.MessageInboxAdapter
import com.emarsys.sample.databinding.FragmentMessageInboxBinding
import com.emarsys.sample.extensions.showSnackBar

class MessageInboxFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, TagChangeListener {
    private companion object {
        val TAG: String = MessageInboxFragment::class.java.simpleName
    }
    private var _binding : FragmentMessageInboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messageInboxRecycleView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.messageInboxRecycleView.adapter = MessageInboxAdapter(this)

        loadMessages()

        binding.swipeRefreshLayout.setOnRefreshListener {
            onRefresh()
            binding.refreshHint.visibility = View.GONE
        }
    }

    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = true
        loadMessages()
    }

    private fun loadMessages() {
        Emarsys.messageInbox.fetchMessages {
            it.result?.let { notificationStatus ->

                notificationStatus.messages.forEach { notification ->
                    Log.i(TAG, "Messages: ${notification.title}")
                }
                markAsSeen(notificationStatus.messages)
                (binding.messageInboxRecycleView.adapter as MessageInboxAdapter).addItems(notificationStatus.messages)
            }

            it.errorCause?.let { cause ->
                binding.inboxView.showSnackBar("Error fetching messages: ${cause.message}")
            }
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun markAsSeen(messages: List<Message>) {
        messages.forEach {
            if (it.tags.isNullOrEmpty()) {
                Emarsys.messageInbox.addTag(InboxTag.SEEN, it.id)
            }
        }
    }

    override fun addTagClicked(messageId: String) {
        val tag = binding.tagEditText.text.toString()
        if (tag.isNotEmpty()) {
            Emarsys.messageInbox.addTag(tag, messageId)
            loadMessages()
        }
    }

    override fun removeTagClicked(messageId: String) {
        val tag = binding.tagEditText.text.toString()
        if (tag.isNotEmpty()) {
            Emarsys.messageInbox.removeTag(tag, messageId)
            loadMessages()
        }
    }
}
