package licenta.socializerapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseObject;

public class Application extends android.app.Application {

    public static final boolean APPDEBUG = false;
    public static final String APPTAG = "AnyWall";

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";

    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private static SharedPreferences preferences;
    private static ConfigHelper configHelper;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(AnywallPost.class);
        Parse.initialize(this, "AT9B8WJElr5FcpxnAyhhP3BFneIYYMGK4wXp51Iv", "30agEm7GIGFN0R2D88RD3MVaPafInEMz8TgJbFM7");
        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);
        configHelper = new ConfigHelper();
        configHelper.fetchConfigIfNeeded();
    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }

    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }

    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
    }

}