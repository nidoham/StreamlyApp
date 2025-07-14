package com.nidoham.streamly.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.nidoham.streamly.R;
import com.nidoham.streamly.databinding.ItemVideosBinding;
import com.nidoham.streamly.util.DurationFormatter;
import com.nidoham.streamly.util.TimeAgoFormatter;
import com.nidoham.streamly.util.Utils;
import exp.nidoham.image.ImageStrategy;
import java.util.Collections;
import java.util.Comparator;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import exp.nidoham.image.ImageStrategy.PreferredImageQuality;

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
            if (video == null) {
                return;
            }

            // Set preferred image quality for thumbnails
            ImageStrategy.setPreferredImageQuality(PreferredImageQuality.HIGH);
            
            // Get thumbnail and channel avatar URLs
            String thumbnailUrl = null;
            String channelAvatar = null;
            
            try {
                List<Image> thumbnails = video.getThumbnails();
                if (thumbnails != null && !thumbnails.isEmpty()) {
                    thumbnailUrl = Collections.max(thumbnails, Comparator.comparingInt(Image::getWidth)).getUrl();
                    //thumbnailUrl = ImageStrategy.choosePreferredImage(thumbnails);
                }
                
                List<Image> uploaderAvatars = video.getUploaderAvatars();
                if (uploaderAvatars != null && !uploaderAvatars.isEmpty()) {
                    channelAvatar = ImageStrategy.choosePreferredImage(uploaderAvatars);
                }
            } catch (Exception e) {
                // Handle any exceptions silently and continue with null URLs
                e.printStackTrace();
            }

            // Set Video Title
            String title = video.getName();
            binding.tvTitle.setText(title != null ? title : "Unknown Title");
            
            ImageStrategy.loadImage(binding.ivThumbnail.getContext(), thumbnailUrl ,binding.ivThumbnail);
            ImageStrategy.loadImage(binding.ivThumbnail.getContext(), channelAvatar ,binding.ivChannelLogo);

            // Set Video Duration
            long duration = video.getDuration();
            if (duration > 0) {
                binding.tvDuration.setText(DurationFormatter.formatSeconds(duration));
                binding.tvDuration.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvDuration.setVisibility(android.view.View.GONE);
            }

            // Set Channel Name and Views/Upload Date
            String channelName = video.getUploaderName();
            long viewCount = video.getViewCount();
            
            StringBuilder infoBuilder = new StringBuilder();
            
            // Add channel name if available
            if (channelName != null && !channelName.trim().isEmpty()) {
                binding.tvTitle.setText(channelName);
            } else {
                binding.tvTitle.setText("Unknown Channel");
            }
            
            // Add view count
            if (viewCount >= 0) {
                infoBuilder.append(Utils.formatViewCount(viewCount));
            }
            
            // Add upload time
            try {
                if (video.getUploadDate() != null && video.getUploadDate().date() != null) {
                    String uploadTime = TimeAgoFormatter.format(video.getUploadDate().date().getTime());
                    if (uploadTime != null && !uploadTime.trim().isEmpty()) {
                        if (infoBuilder.length() > 0) {
                            infoBuilder.append(" • ");
                        }
                        infoBuilder.append(uploadTime);
                    }
                }
            } catch (Exception e) {
                // Handle upload date parsing errors
                e.printStackTrace();
            }

            String channelInfo = infoBuilder.toString();
            binding.tvChannelInfo.setText(channelInfo.isEmpty() ? "No info available" : channelInfo);

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
                    // Use URL as unique identifier
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem;
                    }
                    return Objects.equals(oldItem.getUrl(), newItem.getUrl());
                }

                @Override
                public boolean areContentsTheSame(@NonNull StreamInfoItem oldItem, @NonNull StreamInfoItem newItem) {
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem;
                    }
                    
                    // Compare key properties that affect the UI
                    return Objects.equals(oldItem.getName(), newItem.getName()) &&
                           Objects.equals(oldItem.getUploaderName(), newItem.getUploaderName()) &&
                           oldItem.getViewCount() == newItem.getViewCount() &&
                           oldItem.getDuration() == newItem.getDuration() &&
                           Objects.equals(oldItem.getUploadDate(), newItem.getUploadDate()) &&
                           Objects.equals(oldItem.getThumbnails(), newItem.getThumbnails()) &&
                           Objects.equals(oldItem.getUploaderAvatars(), newItem.getUploaderAvatars());
                }
            };
}