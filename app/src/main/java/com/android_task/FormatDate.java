package com.android_task;

public class FormatDate {
    public static String fDate(long publicationTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long elapsedTimeSeconds = currentTime - publicationTime;

        if (elapsedTimeSeconds < 60) {
            return elapsedTimeSeconds + " seconds ago";
        } else if (elapsedTimeSeconds < 3600) {
            long minutes = elapsedTimeSeconds / 60;
            return minutes + " minutes ago";
        } else {
            long hours = elapsedTimeSeconds / 3600;
            return hours + " hours ago";
        }
    }
}
