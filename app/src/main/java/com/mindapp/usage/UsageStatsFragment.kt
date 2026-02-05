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

        setupRecyclerView()
        loadUsageStats()
        
        // Set up refresh button
        binding.btnRefresh.setOnClickListener {
            loadUsageStats()
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
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check permission first
                if (!UsageStatsHelper.hasUsageStatsPermission(requireContext())) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Please grant Usage Stats permission in Settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Get usage stats
                val totalScreenTime = UsageStatsHelper.getTotalScreenTime(requireContext())
                val socialMediaUsage = UsageStatsHelper.getSocialMediaUsage(requireContext())
                val topApps = UsageStatsHelper.getTopApps(requireContext(), 5)

                withContext(Dispatchers.Main) {
                    // Update UI
                    binding.tvTotalScreenTime.text = UsageStatsHelper.formatTime(totalScreenTime)
                    binding.tvSocialMediaUsage.text = UsageStatsHelper.formatTime(socialMediaUsage)
                    
                    // Update top apps list
                    topAppsAdapter.submitList(topApps)
                    
                    // Check for social media alert (3 hours = 10800000 ms)
                    val threeHoursInMillis = 3 * 60 * 60 * 1000L
                    if (socialMediaUsage > threeHoursInMillis) {
                        binding.tvAlert.visibility = View.VISIBLE
                        binding.tvAlert.text = "⚠️ Alert: You've used social media for more than 3 hours today!"
                        
                        // Send notification
                        NotificationHelper.sendSocialMediaAlert(
                            requireContext(),
                            UsageStatsHelper.formatTime(socialMediaUsage)
                        )
                    } else {
                        binding.tvAlert.visibility = View.GONE
                    }
                    
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error loading usage stats: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
