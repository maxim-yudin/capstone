package jqsoft.apps.vkflow;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maximyudin on 21.03.16.
 */
public class GA {
    public enum Target {
        APP
    }

    private final Map<Target, Tracker> mTrackers = new HashMap<>();

    private static GA sInstance;
    private final Context mContext;

    /**
     * Don't instantiate directly - use {@link #getInstance()} instead.
     */
    private GA(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized void initialize(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Extra call to initialize analytics trackers");
        }

        sInstance = new GA(context);

        // first initialization tracker
        getInstance().get(Target.APP);
    }

    public static synchronized GA getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }

        return sInstance;
    }

    /**
     * Create event
     */
    public static Map<String, String> createEvent(String eventCategory, String eventAction, String eventLabel) {
        return new HitBuilders.EventBuilder()
                .setCategory(eventCategory)
                .setAction(eventAction)
                .setLabel(eventLabel)
                .build();
    }

    /**
     * Send event.
     * <p/>
     * You can create event using createEvent() method.
     */
    public static void sendEvent(Map<String, String> event) {
        Tracker defaultTracker = getInstance().get(Target.APP);
        if (defaultTracker != null) {
            defaultTracker.send(event);
        }
    }

    public synchronized Tracker get(Target target) {
        if (!mTrackers.containsKey(target)) {
            Tracker tracker;
            switch (target) {
                case APP:
                    tracker = GoogleAnalytics.getInstance(mContext).newTracker(R.xml.global_tracker);
                    tracker.enableAutoActivityTracking(true);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled analytics target " + target);
            }
            mTrackers.put(target, tracker);
        }

        return mTrackers.get(target);
    }
}
