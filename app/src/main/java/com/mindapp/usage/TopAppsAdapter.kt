package com.mindapp.usage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mindapp.R

/**
 * RecyclerView Adapter for displaying top used apps
 */
class TopAppsAdapter : ListAdapter<AppUsageInfo, TopAppsAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameText: TextView = itemView.findViewById(R.id.tv_app_name)
        private val appTimeText: TextView = itemView.findViewById(R.id.tv_app_time)
        private val appCategoryText: TextView = itemView.findViewById(R.id.tv_app_category)

        fun bind(appUsage: AppUsageInfo) {
            appNameText.text = appUsage.appName
            appTimeText.text = UsageStatsHelper.formatTime(appUsage.totalTime)
            appCategoryText.text = appUsage.category.name.replace("_", " ")
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppUsageInfo>() {
        override fun areItemsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo): Boolean {
            return oldItem == newItem
        }
    }
}
