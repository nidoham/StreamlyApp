package exp.nidoham.example;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nidoham.streamly.databinding.ActivityMainBinding;

import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import exp.nidoham.model.VideoItem;
import exp.nidoham.util.CombinedVideoLoader;
import exp.nidoham.util.YouTubeVideosLoader;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CompositeDisposable disposables = new CompositeDisposable();

    // Stores stream resolutions for UI display and URLs for playback/download
    private List<String> streamResolutions = new ArrayList<>();
    private List<String> streamUrls = new ArrayList<>();
    private String currentVideoTitle = "";

    private static final String DEFAULT_VIDEO_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
    }

    private void setupUI() {
        // Set default URL
        binding.urlInput.setText(DEFAULT_VIDEO_URL);

        // Set click listener for fetch button
        binding.fetchButton.setOnClickListener(v -> fetchStreamUrls());

        // Initialize UI states
        binding.progressBar.setVisibility(View.GONE);
        binding.videoInfoSection.setVisibility(View.GONE);
        binding.qualityHeader.setVisibility(View.GONE);
        binding.resultsList.setVisibility(View.GONE);

        // Handle item clicks
        binding.resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!streamUrls.isEmpty() && position < streamUrls.size()) {
                    String selectedUrl = streamUrls.get(position);
                    Toast.makeText(MainActivity.this, "Selected: " + selectedUrl, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchStreamUrls() {
        String videoUrl = binding.urlInput.getText().toString().trim();
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingState();
        clearStreamData(); // Clear previous results

        // Load video info using YouTubeVideosLoader
        YouTubeVideosLoader.loadVideos(videoUrl, new YouTubeVideosLoader.VideoLoadCallback() {
            @Override
            public void onVideoLoaded(VideoItem videoItem) {
                currentVideoTitle = videoItem.getTitleOrEmpty();

                // Use CombinedVideoLoader to process streams
                CombinedVideoLoader.combineStreams(videoItem, new CombinedVideoLoader.StreamCombinationCallback() {
                    @Override
                    public void onStreamsCombined(@NonNull List<String> resolutions, @NonNull List<String> urls) {
                        streamResolutions.addAll(resolutions);
                        streamUrls.addAll(urls);
                        displayResults();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        handleError(new Exception(errorMessage));
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                handleError(throwable);
            }
        });
    }

    private void clearStreamData() {
        streamResolutions.clear();
        streamUrls.clear();
    }

    private void showLoadingState() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.resultsList.setVisibility(View.GONE);
        binding.videoInfoSection.setVisibility(View.GONE);
        binding.qualityHeader.setVisibility(View.GONE);
    }

    private void displayResults() {
        binding.progressBar.setVisibility(View.GONE);

        if (streamResolutions.isEmpty()) {
            Toast.makeText(this, "No streams found", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                streamResolutions
        );

        binding.resultsList.setAdapter(adapter);
        binding.resultsList.setVisibility(View.VISIBLE);
        binding.qualityHeader.setVisibility(View.VISIBLE);
        binding.videoInfoSection.setVisibility(View.VISIBLE);
        binding.videoTitle.setText(currentVideoTitle);
    }

    private void handleError(Throwable error) {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear(); // Prevent memory leaks
        YouTubeVideosLoader.clear(); // Clear RxJava disposables in loader
    }
}