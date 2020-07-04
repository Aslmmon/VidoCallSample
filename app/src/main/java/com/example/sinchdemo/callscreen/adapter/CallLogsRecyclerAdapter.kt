package com.example.sinchdemo.callscreen.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.example.sinchdemo.R
import com.example.sinchdemo.model.VidoeChatDetails
import kotlinx.android.synthetic.main.call_log.view.*

class CallLogsRecyclerAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VidoeChatDetails>() {

        override fun areItemsTheSame(
            oldItem: VidoeChatDetails,
            newItem: VidoeChatDetails
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: VidoeChatDetails,
            newItem: VidoeChatDetails
        ): Boolean {
            return oldItem == newItem

        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return VideoChatDetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.call_log,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoChatDetailsViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<VidoeChatDetails>) {
        differ.submitList(list)
    }

    class VideoChatDetailsViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: VidoeChatDetails) = with(itemView) {
            itemView.tv_call_made_date.text = "Call ${adapterPosition+1} made on date : ${item.callStartTime}"
            itemView.tv_duration_call.text =  "Call duration : ${item.duration}"
            itemView.tv_to_user.text =  "Call to user : ${item.userRecieveing}"

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: VidoeChatDetails)
    }
}
