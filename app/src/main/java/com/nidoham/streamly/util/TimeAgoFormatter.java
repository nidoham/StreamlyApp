package com.nidoham.streamly.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgoFormatter {

    public static String format(Date uploadDate) {
        long now = System.currentTimeMillis();
        long time = uploadDate.getTime();
        long diff = now - time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "m ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + "h ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            // পুরনো ভিডিও: তারিখ দেখাও
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(uploadDate);
        }
    }
}