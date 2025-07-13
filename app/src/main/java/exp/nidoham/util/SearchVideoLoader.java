package exp.nidoham.util;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.search.SearchInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import org.schabi.newpipe.util.ExtractorHelper;

public class SearchVideoLoader {
    private final int serviceId;
    private final String query;
    private final List<String> contentFilters;
    private final String sortFilter;

    private Page nextPage;
    private boolean isInitialized = false;

    public SearchVideoLoader(int serviceId, String query, List<String> contentFilters, String sortFilter) {
        this.serviceId = serviceId;
        this.query = query;
        this.contentFilters = contentFilters;
        this.sortFilter = sortFilter;
    }

    public Single<InfoItemsPage<InfoItem>> loadInitial() {
        return ExtractorHelper.searchFor(serviceId, query, contentFilters, sortFilter)
                .flatMap(searchInfo -> {
                    this.nextPage = searchInfo.getNextPage();
                    this.isInitialized = true;

                    return ExtractorHelper.getMoreSearchItems(
                            serviceId, query, contentFilters, sortFilter, searchInfo.getNextPage()
                    );
                });
    }

    public Single<InfoItemsPage<InfoItem>> loadMore() {
        if (!isInitialized) {
            throw new IllegalStateException("Loader not initialized. Call loadInitial() first.");
        }
        if (nextPage == null) {
            return Single.error(new NoMorePagesException());
        }

        return ExtractorHelper.getMoreSearchItems(serviceId, query, contentFilters, sortFilter, nextPage)
                .map(itemsPage -> {
                    this.nextPage = itemsPage.getNextPage();
                    return itemsPage;
                });
    }

    public boolean hasMorePages() {
        return nextPage != null;
    }

    public static class NoMorePagesException extends Exception {
        public NoMorePagesException() {
            super("No more pages available");
        }
    }
}