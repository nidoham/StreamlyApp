package com.nidoham.streamly;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nidoham.streamly.databinding.ActivityMainBinding;

import exp.nidoham.util.BanglaWordDetector;
import exp.nidoham.util.TrendingVideosExecutor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TrendingVideosExecutor.Listener {
    private ActivityMainBinding binding;
    private TrendingVideosExecutor trendingVideosExecutor;

    private final List<StreamInfoItem> trendingVideos = new ArrayList<>();
    private final List<StreamInfoItem> filteredTrendingVideos = new ArrayList<>(); // Stores only Bangla items
    private ArrayAdapter<String> adapter;
    
    private boolean isLoadingMore = false;
    private boolean hasMorePages = true;
    private int maxRetries = 3;
    private int currentRetries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        loadTrendingVideos();
    }

    private void setupUI() {
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        binding.resultsList.setAdapter(adapter);
        binding.resultsList.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.videoInfoSection.setVisibility(View.GONE);
        binding.qualityHeader.setVisibility(View.GONE);

        binding.resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < filteredTrendingVideos.size()) {
                    StreamInfoItem item = filteredTrendingVideos.get(position);
                    Toast.makeText(MainActivity.this, "Playing: " + item.getName(), Toast.LENGTH_SHORT).show();
                    // Launch video player or details activity
                }
            }
        });
    }

    private void loadTrendingVideos() {
        try {
            trendingVideosExecutor = new TrendingVideosExecutor(this, this);
            trendingVideosExecutor.fetchTrendingVideos(true); // Initial load
        } catch (Exception e) {
            showError("Failed to initialize trending videos: " + e.getMessage());
        }
    }

    private void loadMoreVideosIfNeeded() {
        if (!isLoadingMore && hasMorePages && filteredTrendingVideos.size() < 50 && currentRetries < maxRetries) {
            isLoadingMore = true;
            currentRetries++;
            try {
                trendingVideosExecutor.fetchTrendingVideos(false); // Load more
            } catch (Exception e) {
                isLoadingMore = false;
                showError("Failed to load more videos: " + e.getMessage());
            }
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showInitialVideos(List<StreamInfoItem> items) {
        trendingVideos.clear();
        trendingVideos.addAll(items);
        filteredTrendingVideos.clear();
        currentRetries = 0;
        hasMorePages = true;
        isLoadingMore = false;

        List<String> titles = new ArrayList<>();
        for (StreamInfoItem item : items) {
            String name = item.getName();
            if (BanglaWordDetector.containsBangla(name)) {
                titles.add(name);
                filteredTrendingVideos.add(item);
            }
        }

        adapter.clear();
        adapter.addAll(titles);
        adapter.notifyDataSetChanged();

        // Load more if not enough Bangla videos and we have pages available
        if (titles.size() < 50 && items.size() > 0) {
            loadMoreVideosIfNeeded();
        } else if (items.size() == 0) {
            hasMorePages = false;
        }
    }

    @Override
    public void showMoreVideos(List<StreamInfoItem> items) {
        isLoadingMore = false;
        
        if (items == null || items.isEmpty()) {
            hasMorePages = false;
            return;
        }
        
        trendingVideos.addAll(items);

        List<String> newTitles = new ArrayList<>();
        for (StreamInfoItem item : items) {
            String name = item.getName();
            if (BanglaWordDetector.containsBangla(name)) {
                newTitles.add(name);
                filteredTrendingVideos.add(item);
            }
        }

        if (!newTitles.isEmpty()) {
            adapter.addAll(newTitles);
            adapter.notifyDataSetChanged();
        }

        // Continue loading if we still don't have enough Bangla videos
        if (filteredTrendingVideos.size() < 50 && items.size() > 0) {
            loadMoreVideosIfNeeded();
        } else if (items.size() == 0) {
            hasMorePages = false;
        }
    }

    @Override
    public void showEmptyState() {
        isLoadingMore = false;
        hasMorePages = false;
        adapter.clear();
        adapter.add("No trending videos found");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showError(String message) {
        isLoadingMore = false;
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        binding.progressBar.setVisibility(View.GONE);
        
        // Try to load more if we don't have enough videos yet
        if (filteredTrendingVideos.size() < 50 && currentRetries < maxRetries) {
            loadMoreVideosIfNeeded();
        }
    }

    @Override
    protected void onDestroy() {
        if (trendingVideosExecutor != null) {
            trendingVideosExecutor.dispose();
        }
        super.onDestroy();
    }
}