package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.spendwise.databinding.ActivityOnboardingBinding
import com.example.spendwise.databinding.ItemOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.ic_onboarding_track,
            "Track Your Expenses",
            "Easily record and categorize your daily expenses"
        ),
        OnboardingItem(
            R.drawable.ic_onboarding_budget,
            "Set Budgets",
            "Create budgets for different categories and track your spending"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnboarding()
        setupDots()
        setupButtons()
    }

    private fun setupOnboarding() {
        onboardingAdapter = OnboardingAdapter()
        binding.viewPager.adapter = onboardingAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButtons(position)
            }
        })
    }

    private fun setupDots() {
        val dots = arrayOfNulls<ImageView>(onboardingItems.size)
        for (i in dots.indices) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.dot_unselected
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.layoutDots.addView(dots[i], params)
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val imageView = binding.layoutDots.getChildAt(i) as ImageView
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == position) R.drawable.dot_selected else R.drawable.dot_unselected
                )
            )
        }
    }

    private fun setupButtons() {
        binding.btnSkip.setOnClickListener {
            navigateToPasscode()
        }

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < onboardingItems.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                navigateToPasscode()
            }
        }
    }

    private fun updateButtons(position: Int) {
        binding.btnNext.text = if (position == onboardingItems.size - 1) "Get Started" else "Next"
    }

    private fun navigateToPasscode() {
        startActivity(Intent(this, PasscodeActivity::class.java))
        finish()
    }

    inner class OnboardingAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            val binding = ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return OnboardingViewHolder(binding)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(onboardingItems[position])
        }

        override fun getItemCount() = onboardingItems.size

        inner class OnboardingViewHolder(private val binding: ItemOnboardingBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
            fun bind(item: OnboardingItem) {
                binding.ivOnboarding.setImageResource(item.imageRes)
                binding.tvTitle.text = item.title
                binding.tvDescription.text = item.description
            }
        }
    }

    data class OnboardingItem(
        val imageRes: Int,
        val title: String,
        val description: String
    )
} 