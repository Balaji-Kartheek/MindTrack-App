package com.mindapp.usage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindapp.R
import com.mindapp.databinding.FragmentUsageStatsBinding
import com.mindapp.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment displaying app usage statistics
 * 
 * Shows:
 * - Total screen time
 * - Social media usage
 * - Top 5 most used apps
 * - Alerts when social media usage exceeds 3 hours
 */
class UsageStatsFragment : Fragment() {

    private var _binding: FragmentUsageStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var topAppsAdapter: TopAppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsageStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupRecyclerView()
            loadUsageStats()
            binding.btnRefresh.setOnClickListener { loadUsageStats() }
        } catch (e: Exception) {
            android.util.Log.e("UsageStatsFragment", "onViewCreated", e)
        }
    }

    /**
     * Sets up the RecyclerView for top apps
     */
    private fun setupRecyclerView() {
        topAppsAdapter = TopAppsAdapter()
        binding.recyclerViewTopApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topAppsAdapter
        }
    }

    /**
     * Loads and displays usage statistics
     */
    private fun loadUsageStats() {
        _binding?.progressBar?.visibility = View.VISIBLE
        val ctx = context ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                    withContext(Dispatchers.Main) {
                        _binding?.progressBar?.visibility = View.GONE
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Please grant Usage Stats permission in Settings",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    return@launch
                }

                val totalScreenTime = UsageStatsHelper.getTotalScreenTime(ctx)
                val socialMediaUsage = UsageStatsHelper.getSocialMediaUsage(ctx)
                val topApps = UsageStatsHelper.getTopApps(ctx, 5)

                withContext(Dispatchers.Main) {
                    val b = _binding ?: return@withContext
                    b.tvTotalScreenTime.text = UsageStatsHelper.formatTime(totalScreenTime)
                    b.tvSocialMediaUsage.text = UsageStatsHelper.formatTime(socialMediaUsage)
                    topAppsAdapter.submitList(topApps)
                    
                    val threeHoursInMillis = 3 * 60 * 60 * 1000L
                    if (socialMediaUsage > threeHoursInMillis) {
                        b.tvAlert.visibility = View.VISIBLE
                        b.tvAlert.text = "⚠️ Alert: You've used social media for more than 3 hours today!"
                        try {
                            NotificationHelper.sendSocialMediaAlert(
                                requireContext(),
                                UsageStatsHelper.formatTime(socialMediaUsage)
                            )
                        } catch (_: Exception) { }
                    } else {
                        b.tvAlert.visibility = View.GONE
                    }
                    b.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _binding?.progressBar?.visibility = View.GONE
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Error loading usage stats: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
