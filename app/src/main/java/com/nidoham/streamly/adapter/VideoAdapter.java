package com.nidoham.streamly.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.nidoham.streamly.R; // Ensure this is correctly imported for your drawables
import com.nidoham.streamly.databinding.ItemVideosBinding;
import com.nidoham.streamly.util.DurationFormatter;
import com.nidoham.streamly.util.TimeAgoFormatter;
import com.nidoham.streamly.util.Utils;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;
import java.util.Objects;

/**
 * RecyclerView Adapter for displaying a list of StreamInfoItem objects.
 * Utilizes ListAdapter with DiffUtil for efficient list updates and animations.
 */
public class VideoAdapter extends ListAdapter<StreamInfoItem, VideoAdapter.VideoViewHolder> {

    private final OnVideoClickListener listener;

    /**
     * Interface for handling click events on video items and their download buttons.
     */
    public interface OnVideoClickListener {
        /**
         * Called when the main video item is clicked.
         * @param video The StreamInfoItem associated with the clicked item.
         */
        void onVideoClick(StreamInfoItem video);

        /**
         * Called when the download button/icon within a video item is clicked.
         * @param video The StreamInfoItem associated with the item whose download button was clicked.
         */
        void onDownloadClick(StreamInfoItem video);
    }

    /**
     * Constructor for the VideoAdapter.
     * @param listener The click listener for video items and download actions.
     */
    public VideoAdapter(OnVideoClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVideosBinding binding = ItemVideosBinding.inflate(inflater, parent, false);
        return new VideoViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        StreamInfoItem video = getItem(position);
        holder.bind(video);
    }

    /**
     * ViewHolder for individual video items.
     */
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemVideosBinding binding;
        private final OnVideoClickListener listener;

        public VideoViewHolder(@NonNull ItemVideosBinding binding, OnVideoClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        /**
         * Binds the StreamInfoItem data to the views in the item layout.
         * @param video The StreamInfoItem to bind.
         */
        public void bind(StreamInfoItem video) {
            // Set Video Title
            binding.tvTitle.setText(video.getName());

            // Load Video Thumbnail using Glide
            // NewPipe's StreamInfoItem provides a list of ThumbnailInfo objects.
            // ThumbnailInfo directly provides the URL as a String.
            String thumbnailUrl = null;
            List<Image> thumbnails = video.getThumbnails();
            if (thumbnails != null && !thumbnails.isEmpty()) {
                thumbnailUrl = thumbnails.get(0).getUrl(); // Get the URL string from ThumbnailInfo
            }

            Glide.with(binding.ivThumbnail.getContext())
                    .load(thumbnailUrl) // Load the URL, can be null if no thumbnail
                    .placeholder(R.drawable.placeholder_thumbnail) // Provide a placeholder drawable
                    .error(R.drawable.error_thumbnail) // Provide an error drawable
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivThumbnail);

            // Set Video Duration
            binding.tvDuration.setText(DurationFormatter.formatSeconds(video.getDuration()));

            // Load Channel Logo using Glide
            // StreamInfoItem does not directly provide uploader avatar URL.
            // You might need to fetch ChannelInfo separately for the actual avatar.
            // For now, we'll use a generic placeholder.
            Glide.with(binding.ivChannelLogo.getContext())
                    .load(R.drawable.placeholder_channel_logo) // Use a static placeholder
                    .circleCrop() // Apply circular crop for channel logo
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivChannelLogo);

            // Set Channel Name and Views/Upload Date
            String channelName = video.getUploaderName();
            long viewCount = video.getViewCount();
            
            String uploadTime = TimeAgoFormatter.format(video.getUploadDate().date().getTime());
            
            StringBuilder builder = new StringBuilder();
            builder.append(Utils.formatViewCount(viewCount));
            builder.append(" • ");
            builder.append(uploadTime);

            String channelInfo = builder.toString();
            binding.tvChannelInfo.setText(channelInfo);

            // Click listener for the whole item
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(video);
                }
            });

            // Click listener for the download icon
            binding.ivDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(video);
                }
            });
        }
    }

    /**
     * DiffUtil.ItemCallback for calculating differences between old and new lists.
     * This is crucial for efficient RecyclerView updates.
     */
    private static final DiffUtil.ItemCallback<StreamInfoItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StreamInfoItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull StreamInfoItem oldItem, @NonNull StreamInfoItem newItem) {
                    // Assuming StreamInfoItem has a unique URL or ID that identifies it.
                    // This is critical for DiffUtil to correctly identify moved or changed items.
                    return Objects.equals(oldItem.getUrl(), newItem.getUrl());
                }

                @Override
                public boolean areContentsTheSame(@NonNull StreamInfoItem oldItem, @NonNull StreamInfoItem newItem) {
                    // Return true if the contents of the items are the same.
                    // This determines if the item's views need to be updated (re-bound).
                    // This requires StreamInfoItem to properly override equals() and hashCode().
                    // If StreamInfoItem doesn't override equals/hashCode, this might always return false,
                    // leading to unnecessary re-binds.
                    return oldItem.equals(newItem);
                }
            };
}
