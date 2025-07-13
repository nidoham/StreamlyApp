package exp.nidoham.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import exp.nidoham.util.KioskContentLoader;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Manages the fetching, state, and pagination of trending videos.
 * This class handles all business logic and communicates results
 * via a listener to decouple it from the UI.
 */
public class TrendingVideosExecutor {

    private static final String TAG = "TrendingVideosExecutor";

    /**
     * Listener interface to update the UI based on executor results.
     */
    public interface Listener {
        void showLoading(boolean isLoading);
        void showInitialVideos(@NonNull List<StreamInfoItem> items);
        void showMoreVideos(@NonNull List<StreamInfoItem> items);
        void showEmptyState();
        void showError(@NonNull String message);
    }

    private final Listener listener;
    private final KioskContentLoader contentLoader;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Page nextPage;
    private boolean isLoading = false;

    /**
     * Creates a new TrendingVideosExecutor.
     *
     * @param context  Application context for context-sensitive operations.
     * @param listener Callback for UI updates.
     * @throws ExtractionException if the YouTube service or kiosk list cannot be initialized.
     */
    public TrendingVideosExecutor(@NonNull Context context, @NonNull Listener listener) throws ExtractionException {
        this.listener = listener;

        try {
            final int serviceId = ServiceList.YouTube.getServiceId();
            final StreamingService service = NewPipe.getService(serviceId);
            final KioskList kioskList = service.getKioskList();

            final String trendingKioskId = kioskList.getDefaultKioskId(); // e.g., "Trending"
            final ListLinkHandlerFactory factory = kioskList.getListLinkHandlerFactoryByType(trendingKioskId);

            if (factory == null) {
                throw new ExtractionException("Failed to get link handler factory for trending kiosk");
            }

            final String trendingUrl = factory.fromId(trendingKioskId).getUrl();
            Log.d(TAG, "Using trending URL: " + trendingUrl);

            this.contentLoader = new KioskContentLoader(serviceId, trendingUrl, context);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing TrendingVideosExecutor", e);
            throw new ExtractionException("Failed to initialize TrendingVideosExecutor: " + e.getMessage());
        }
    }

    /**
     * Starts the initial fetch for trending videos.
     *
     * @param forceReload if true, bypasses the cache.
     */
    public void fetchTrendingVideos(boolean forceReload) {
        if (isLoading) {
            Log.w(TAG, "Already loading. Ignoring duplicate request.");
            return;
        }
        setLoading(true);

        disposables.add(
            contentLoader.loadInitialInfo(forceReload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleInitialResult, this::handleError)
        );
    }

    /**
     * Fetches the next page of trending videos if available.
     */
    public void fetchMoreVideos() {
        if (isLoading || nextPage == null) {
            Log.w(TAG, "Cannot fetch more videos: loading=" + isLoading + ", nextPage=" + (nextPage != null));
            return;
        }
        setLoading(true);

        disposables.add(
            contentLoader.loadMoreItems(nextPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleMoreResults, this::handleError)
        );
    }

    private void handleInitialResult(@NonNull KioskInfo result) {
        setLoading(false);
        this.nextPage = result.getNextPage();

        List<StreamInfoItem> items = result.getRelatedItems();

        if (items == null || items.isEmpty()) {
            Log.d(TAG, "No trending videos found.");
            listener.showEmptyState();
        } else {
            Log.d(TAG, "Loaded " + items.size() + " initial trending videos.");
            listener.showInitialVideos(items);
        }
    }

    private void handleMoreResults(@NonNull ListExtractor.InfoItemsPage<StreamInfoItem> result) {
        setLoading(false);
        this.nextPage = result.getNextPage();

        List<StreamInfoItem> items = result.getItems();

        if (items == null || items.isEmpty()) {
            Log.d(TAG, "No additional trending videos found.");
            return;
        }

        Log.d(TAG, "Loaded " + items.size() + " additional trending videos.");
        listener.showMoreVideos(items);
    }

    private void handleError(@NonNull Throwable error) {
        setLoading(false);
        String errorMessage = "Failed to load trending videos: " + error.getMessage();

        Log.e(TAG, errorMessage, error);

        // Don't disable pagination on transient errors
        if (!(error instanceof ExtractionException)) {
            nextPage = null;
        }

        listener.showError(errorMessage);
    }

    private void setLoading(boolean loading) {
        this.isLoading = loading;
        listener.showLoading(loading);
    }

    /**
     * Cleans up resources and clears subscriptions to prevent memory leaks.
     * Should be called in the lifecycle's onDestroy() method.
     */
    public void dispose() {
        disposables.clear();
    }
}