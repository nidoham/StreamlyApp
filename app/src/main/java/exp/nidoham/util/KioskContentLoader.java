package exp.nidoham.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.util.ExtractorHelper;

import io.reactivex.rxjava3.core.Single;

/**
 * A helper class to load Kiosk-like content (e.g., Trending) from a service using the NewPipe Extractor.
 * This class abstracts the logic for loading initial kiosk info and paginated items.
 *
 * <p>This class is not thread-safe but is intended for use with RxJava to handle asynchronous loading.</p>
 */
public class KioskContentLoader {

    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "KioskContentLoader";

    /**
     * The service ID of the streaming service (e.g., YouTube).
     */
    private final int serviceId;

    /**
     * The base URL for the kiosk/trending page.
     */
    private final String url;

    /**
     * Application context to avoid memory leaks.
     */
    private final Context context;

    /**
     * Constructs a new KioskContentLoader.
     *
     * @param serviceId The ID of the streaming service (e.g., ServiceList.YouTube.getServiceId()).
     * @param url       The URL of the kiosk/trending page. Must not be null or empty.
     * @param context   The application context. Must not be null.
     * @throws IllegalArgumentException if the URL is null or empty.
     */
    public KioskContentLoader(final int serviceId, @NonNull final String url, @NonNull final Context context) {
        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "Kiosk URL cannot be null or empty.");
            throw new IllegalArgumentException("Kiosk URL cannot be null or empty.");
        }

        this.serviceId = serviceId;
        this.url = url;
        // Use application context to avoid memory leaks
        this.context = context.getApplicationContext();

        Log.d(TAG, "KioskContentLoader initialized for serviceId: " + serviceId + ", URL: " + url);
    }

    /**
     * Loads the initial kiosk information asynchronously.
     *
     * <p>This method wraps the call to NewPipe's ExtractorHelper to fetch the initial kiosk info.
     * It returns an RxJava Single that emits the KioskInfo or an error.</p>
     *
     * @param forceReload whether to force a network reload of the content.
     * @return a Single emitting the KioskInfo.
     */
    public Single<KioskInfo> loadInitialInfo(final boolean forceReload) {
        Log.d(TAG, "loadInitialInfo: Loading initial kiosk info for URL: " + url + ", forceReload: " + forceReload);

        return Single.fromCallable(() -> {
            try {
                return ExtractorHelper.getKioskInfo(serviceId, url, forceReload).blockingGet();
            } catch (Exception e) {
                Log.e(TAG, "Failed to load initial kiosk info: " + e.getMessage(), e);
                throw e;
            }
        }).subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io());
    }

    /**
     * Loads the next page of items from a kiosk.
     *
     * <p>Use this method after calling {@link #loadInitialInfo(boolean)} to fetch additional pages
     * of content. It wraps the call to ExtractorHelper's getMoreKioskItems method.</p>
     *
     * @param nextPage the Page object returned from a previous load operation. Must not be null.
     * @return a Single emitting a page of StreamInfoItems.
     */
    public Single<ListExtractor.InfoItemsPage<StreamInfoItem>> loadMoreItems(@NonNull final Page nextPage) {
        Log.d(TAG, "loadMoreItems: Loading more kiosk items for URL: " + url + ", next page: " + nextPage.getUrl());

        if (nextPage == null) {
            Log.e(TAG, "Next page must not be null.");
            throw new IllegalArgumentException("Next page must not be null.");
        }

        return Single.fromCallable(() -> {
            try {
                return ExtractorHelper.getMoreKioskItems(serviceId, url, nextPage).blockingGet();
            } catch (Exception e) {
                Log.e(TAG, "Failed to load more kiosk items: " + e.getMessage(), e);
                throw e;
            }
        }).subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io());
    }
}