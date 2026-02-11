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
        
        // Show last refresh time
        val currentTime = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                    withContext(Dispatchers.Main) {
                        _binding?.progressBar?.visibility = View.GONE
                        _binding?.tvTotalScreenTime?.text = "Permission Required"
                        _binding?.tvSocialMediaUsage?.text = "Grant Permission"
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "⚠️ Usage Access Required\n\nGo to: Settings → Apps → Special Access → Usage Access → MindApp → Enable\n\nThen restart the app.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Open settings
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("UsageStatsFragment", "Can't open settings", e)
                            }
                        }
                    }
                    return@launch
                }

                val totalScreenTime = UsageStatsHelper.getTotalScreenTime(ctx)
                val socialMediaUsage = UsageStatsHelper.getSocialMediaUsage(ctx)
                val topApps = UsageStatsHelper.getTopApps(ctx, 5)

                withContext(Dispatchers.Main) {
                    val b = _binding ?: return@withContext
                    
                    // Show last update time
                    val updateText = "Last updated: $currentTime"
                    b.tvTotalScreenTime.text = "${UsageStatsHelper.formatTime(totalScreenTime)}\n$updateText"
                    b.tvSocialMediaUsage.text = UsageStatsHelper.formatTime(socialMediaUsage)
                    topAppsAdapter.submitList(topApps)
                    
                    // Show helpful message if no apps found
                    if (topApps.isEmpty()) {
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "📊 No usage data found\n\nAndroid needs 5-10 minutes of app usage to collect data.\n\nUse some apps, then tap Refresh.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        b.tvTotalScreenTime.text = "0m\n$updateText"
                    } else {
                        // Show count of tracked apps
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "✅ Tracking ${topApps.size} apps (${UsageStatsHelper.formatTime(totalScreenTime)} total)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    
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
