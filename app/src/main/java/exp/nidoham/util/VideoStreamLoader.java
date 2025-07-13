package exp.nidoham.util;

import android.util.Log;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A helper class to load and process detailed stream information for YouTube videos.
 * This class is designed to fetch and extract video qualities and find specific video streams.
 */
public class VideoStreamLoader {

    private static final String TAG = "VideoStreamLoader";

    /**
     * Fetches detailed stream information for a given StreamInfoItem.
     * This includes all available video and audio qualities.
     *
     * @param streamInfoItem The summary item of the video.
     * @return A Single emitting the detailed StreamInfo.
     */
    public Single<StreamInfo> loadStreamInfo(@Nullable final StreamInfoItem streamInfoItem) {
        if (streamInfoItem == null) {
            Log.e(TAG, "StreamInfoItem is null");
            return Single.error(new IllegalArgumentException("StreamInfoItem cannot be null"));
        }

        return Single.fromCallable(() -> {
            try {
                final StreamingService service = NewPipe.getService(streamInfoItem.getServiceId());
                return StreamInfo.getInfo(service, streamInfoItem.getUrl());
            } catch (Exception e) {
                Log.e(TAG, "Error loading StreamInfo: " + e.getMessage(), e);
                throw new ExtractionException("Failed to load stream info: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Extracts a list of human-readable quality strings from a StreamInfo object.
     * Example: "1080p (mp4)", "720p (webm)"
     *
     * @param streamInfo The detailed StreamInfo object.
     * @return A list of available video quality strings.
     */
    public List<String> extractVideoQualities(@Nullable StreamInfo streamInfo) {
        List<String> qualities = new ArrayList<>();

        if (streamInfo == null || streamInfo.getVideoStreams() == null) {
            Log.w(TAG, "No video streams found");
            return qualities;
        }

        for (VideoStream videoStream : streamInfo.getVideoStreams()) {
            if (videoStream == null || videoStream.getFormat() == null) {
                continue;
            }

            String resolution = videoStream.getResolution();
            String formatName = videoStream.getFormat().getName();
            String qualityString = resolution + " (" + formatName + ")";

            if (!qualities.contains(qualityString)) {
                qualities.add(qualityString);
            }
        }

        Log.d(TAG, "Extracted " + qualities.size() + " video qualities");
        return qualities;
    }

    /**
     * Finds a specific VideoStream object based on its quality string.
     * Useful after a user has selected a quality from the UI.
     *
     * @param streamInfo The detailed StreamInfo object.
     * @param qualityString The quality string selected by the user (e.g., "1080p (mp4)").
     * @return The matching VideoStream, or null if not found.
     */
    public VideoStream getVideoStreamByQualityString(@Nullable StreamInfo streamInfo,
                                                     @Nullable String qualityString) {
        if (streamInfo == null || streamInfo.getVideoStreams() == null || qualityString == null) {
            Log.w(TAG, "Invalid input for getVideoStreamByQualityString");
            return null;
        }

        for (VideoStream videoStream : streamInfo.getVideoStreams()) {
            if (videoStream == null || videoStream.getFormat() == null) {
                continue;
            }

            String resolution = videoStream.getResolution();
            String formatName = videoStream.getFormat().getName();
            String candidateString = resolution + " (" + formatName + ")";

            if (candidateString.equals(qualityString)) {
                Log.d(TAG, "Found matching video stream: " + qualityString);
                return videoStream;
            }
        }

        Log.w(TAG, "No matching video stream found for: " + qualityString);
        return null;
    }
}