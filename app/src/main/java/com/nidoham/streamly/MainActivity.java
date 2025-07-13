package com.nidoham.streamly;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.nidoham.streamly.fragment.*;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nidoham.streamly.databinding.ActivityMainBinding;
import org.schabi.newpipe.extractor.timeago.patterns.fa;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final String[] tabTitles = {"Search", "YouTube", "Music", "More"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager(binding.contentContainer);
        setupTabLayout(binding.tabs, binding.contentContainer);
    }

    // Set up ViewPager with fragments
    private void setupViewPager(ViewPager2 viewPager) {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new SearchFragment();
                    case 1: return new YouTubeFragment();
                    case 2: return new MusicFragment();
                    default: return new MoreFragment();
                }
            }

            @Override
            public int getItemCount() {
                return tabTitles.length;
            }
        });
        
        viewPager.setUserInputEnabled(false);
    }

    // Link TabLayout with ViewPager
    private void setupTabLayout(TabLayout tabLayout, ViewPager2 viewPager) {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}