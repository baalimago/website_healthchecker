package com.imago.websitehealthcheck

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.imago.websitehealthcheck.data.State
import com.imago.websitehealthcheck.data.WebsiteWithLastResult


class RVAdapter(
    private val deleteItemListener: OnClickListener,
    private val editItemListener: OnClickListener,
    private val getHistoryListener: OnClickListener
) : RecyclerView.Adapter<RVAdapter.WebsiteViewHolder>() {
    private var websites: List<LiveData<WebsiteWithLastResult?>> = ArrayList()

    class WebsiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.websiteName)
        val lastCheck: TextView = itemView.findViewById(R.id.lastCheckField)
        val healthIcon: ImageView = itemView.findViewById(R.id.healthIcon)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val hiddenGroup: Group = itemView.findViewById(R.id.cardGroup)
        val foldButton: ImageButton = itemView.findViewById(R.id.unfoldCardBtn)
        val removeButton: Button = itemView.findViewById(R.id.removeHealthcheckBtn)
        val editButton: Button = itemView.findViewById(R.id.editHealthcheckBtn)
        val historyButton: Button = itemView.findViewById(R.id.downloadHistoryBtn)

        var observer: Observer<WebsiteWithLastResult?>? = null
        var websiteLd: LiveData<WebsiteWithLastResult?>? = null

        fun removeObserver() {
            observer?.let { obs ->
                {
                    websiteLd?.removeObserver(obs)
                }
            }
            this.observer = null
            this.websiteLd = null
        }

        fun setObserver(obs: Observer<WebsiteWithLastResult?>, websiteLd: LiveData<WebsiteWithLastResult?>) {
            this.observer = obs
            this.websiteLd = websiteLd
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebsiteViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.health_item, parent, false)
        return WebsiteViewHolder(v)
    }

    override fun getItemCount(): Int {
        return websites.count()
    }

    fun setWebsites(websites: List<LiveData<WebsiteWithLastResult?>>) {
        this.websites = websites
    }

    private fun updateDynamicState(holder: WebsiteViewHolder, wswr: WebsiteWithLastResult) {
        val lastCheck = wswr.lastResult
        val wsInst = wswr.website
        holder.name.text = wsInst.name
        holder.lastCheck.text = lastCheck?.checkDate.toString()
        when (lastCheck?.healthState) {
            State.HEALTHY -> {
                holder.healthIcon.visibility = View.VISIBLE
                holder.progressBar.visibility = View.INVISIBLE
                holder.healthIcon.setImageResource(R.drawable.baseline_check_24)
            }

            State.UNHEALTHY -> {
                holder.healthIcon.visibility = View.VISIBLE
                holder.progressBar.visibility = View.INVISIBLE
                holder.healthIcon.setImageResource(R.drawable.baseline_close_24)
            }

            else -> {
                holder.progressBar.visibility = View.VISIBLE
                holder.healthIcon.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBindViewHolder(holder: WebsiteViewHolder, pos: Int) {
        holder.removeObserver()

        val wsObs = Observer<WebsiteWithLastResult?> { websiteUpd ->
            if(websiteUpd != null) {
                updateDynamicState(holder, websiteUpd)
            }
        }
        val ws = this.websites[pos]
        ws.observeForever(wsObs)
        holder.setObserver(wsObs, ws)

        holder.foldButton.setOnClickListener {
            if (holder.hiddenGroup.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.foldButton.animate().rotation(0F)
                holder.hiddenGroup.visibility = View.GONE
            } else {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.foldButton.animate().rotation(180F)
                holder.hiddenGroup.visibility = View.VISIBLE
            }
        }

        holder.removeButton.setOnClickListener { deleteItemListener.onClick(pos, ws) }
        holder.editButton.setOnClickListener { editItemListener.onClick(pos, ws) }
        holder.historyButton.setOnClickListener { getHistoryListener.onClick(pos, ws) }
    }

    interface OnClickListener {
        fun onClick(position: Int, model: LiveData<WebsiteWithLastResult?>)
    }

}