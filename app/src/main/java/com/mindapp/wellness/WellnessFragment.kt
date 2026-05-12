package com.mindapp.wellness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mindapp.R
import com.mindapp.databinding.FragmentWellnessBinding
import com.mindapp.prefs.MindAppPrefs
import com.mindapp.usage.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WellnessFragment : Fragment() {

    private var _binding: FragmentWellnessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThresholdChips()
        binding.btnRefreshWellness.setOnClickListener { loadContent() }
        loadContent()
    }

    private fun setupThresholdChips() {
        val ctx = requireContext()
        val current = MindAppPrefs.getUsageThresholdMs(ctx)
        val group = binding.chipGroupThreshold
        group.setOnCheckedStateChangeListener(null)
        when (current) {
            90L * 60L * 1000L -> binding.chip90m.isChecked = true
            120L * 60L * 1000L -> binding.chip2h.isChecked = true
            else -> binding.chip1h.isChecked = true
        }
        group.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val ms = when (checkedIds.first()) {
                R.id.chip_2h -> 120L * 60L * 1000L
                R.id.chip_90m -> 90L * 60L * 1000L
                else -> MindAppPrefs.DEFAULT_THRESHOLD_MS
            }
            MindAppPrefs.setUsageThresholdMs(ctx, ms)
            Toast.makeText(ctx, "Reminder threshold updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContent() {
        val ctx = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val total: Long
                val social: Long
                val topName: String?
                withContext(Dispatchers.IO) {
                    if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                        total = 0L
                        social = 0L
                        topName = null
                    } else {
                        total = UsageStatsHelper.getTotalScreenTime(ctx)
                        social = UsageStatsHelper.getSocialMediaUsage(ctx)
                        topName = UsageStatsHelper.getTopApps(ctx, 1).firstOrNull()?.appName
                    }
                }

                val snapshot = WellnessTips.mentalLoadFromUsage(total, social)
                binding.tvMentalHeadline.text = snapshot.headline
                binding.tvMentalDetail.text = snapshot.detail
                binding.tvMentalSuggestion.text = snapshot.suggestion

                if (!UsageStatsHelper.hasUsageStatsPermission(ctx)) {
                    binding.tvSnapshotTotal.text = "Other apps: enable Usage access in Settings"
                    binding.tvSnapshotSocial.text = "Social / feeds: —"
                    binding.tvSnapshotTop.text = "Top app: —"
                } else {
                    binding.tvSnapshotTotal.text =
                        "Other apps today: ${UsageStatsHelper.formatTime(total)}"
                    binding.tvSnapshotSocial.text =
                        "Social / feeds: ${UsageStatsHelper.formatTime(social)}"
                    binding.tvSnapshotTop.text =
                        topName?.let { "Top app: $it" } ?: "Top app: —"
                }

                val daySeed = System.currentTimeMillis() / 86_400_000L
                binding.tvExercise.text = WellnessTips.exerciseForDay(daySeed)
                binding.tvMeditation.text = WellnessTips.meditationForDay(daySeed)
            } catch (e: Exception) {
                Toast.makeText(ctx, "Could not load wellness data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
