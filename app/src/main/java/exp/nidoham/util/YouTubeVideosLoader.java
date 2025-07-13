package exp.nidoham.util;

import android.widget.Toast;

import com.nidoham.streamly.StreamlyApp;

import exp.nidoham.model.VideoItem;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.List;

public class YouTubeVideosLoader {

    private static final CompositeDisposable disposables = new CompositeDisposable();

    public interface VideoLoadCallback {
        void onVideoLoaded(VideoItem videoItem);
        void onError(Throwable throwable);
    }

    public static void loadVideos(final String videoUrl, final VideoLoadCallback callback) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(StreamlyApp.getAppContext(), "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            callback.onError(new IllegalArgumentException("URL is empty"));
            return;
        }

        Single<StreamInfo> streamInfoSingle = Single.fromCallable(() ->
                StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
        );

        Single<VideoItem> videoItemSingle = streamInfoSingle.map(streamInfo -> {
            VideoItem videoItem = new VideoItem();

            videoItem.setTitle(streamInfo.getName());
            videoItem.setUploader(streamInfo.getUploaderName());
            videoItem.setDescription(streamInfo.getDescription().toString());
            videoItem.setUploadDate(streamInfo.getUploadDate().toString());
            videoItem.setThumbnails(streamInfo.getThumbnails());
            videoItem.setDuration(streamInfo.getDuration());
            videoItem.setViewCount(streamInfo.getViewCount());
            videoItem.setVideoStreams(streamInfo.getVideoStreams());
            videoItem.setVideoOnlyStreams(streamInfo.getVideoOnlyStreams()); // Reintroduced
            videoItem.setAudioStreams(streamInfo.getAudioStreams());

            return videoItem;
        });

        disposables.add(
            videoItemSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    videoItem -> callback.onVideoLoaded(videoItem),
                    throwable -> callback.onError(throwable)
                )
        );
    }

    public static void clear() {
        disposables.clear();
    }
}