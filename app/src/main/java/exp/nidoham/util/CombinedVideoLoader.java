package exp.nidoham.util;

import androidx.annotation.NonNull;
import exp.nidoham.model.VideoItem;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class CombinedVideoLoader {

    /**
     * Callback interface to handle the result of combining video streams.
     */
    public interface StreamCombinationCallback {
        void onStreamsCombined(@NonNull List<String> resolutions, @NonNull List<String> urls);
        void onError(@NonNull String errorMessage);
    }

    /**
     * Combines regular video streams and video-only streams from the given VideoItem.
     * Provides the combined stream resolutions and URLs via the callback.
     *
     * @param videoItem  The VideoItem containing video and video-only streams
     * @param callback   Callback to deliver results or errors
     */
    public static void combineStreams(@Nullable VideoItem videoItem,
                                      @NonNull StreamCombinationCallback callback) {
        if (videoItem == null) {
            callback.onError("VideoItem is null");
            return;
        }

        List<VideoStream> videoStreams = videoItem.getVideoStreamsOrEmpty();
        List<VideoStream> videoOnlyStreams = videoItem.getVideoOnlyStreamsOrEmpty();

        if (videoStreams.isEmpty() && videoOnlyStreams.isEmpty()) {
            callback.onError("No streams found in the video item");
            return;
        }

        List<String> resolutions = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        // Combine all valid streams
        for (VideoStream stream : videoStreams) {
            addStream(stream, resolutions, urls);
        }

        for (VideoStream stream : videoOnlyStreams) {
            addStream(stream, resolutions, urls);
        }

        if (resolutions.isEmpty()) {
            callback.onError("No valid URLs found in streams");
        } else {
            callback.onStreamsCombined(resolutions, urls);
        }
    }

    private static void addStream(@Nullable VideoStream stream,
                                 @NonNull List<String> resolutions,
                                 @NonNull List<String> urls) {
        if (stream != null && stream.getUrl() != null) {
            resolutions.add(stream.getResolution());
            urls.add(stream.getUrl());
        }
    }
}