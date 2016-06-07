package licenta.socializerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

public class Application extends android.app.Application {

    public static final boolean APPDEBUG = false;
    public static final String APPTAG = "AnyWall";

    // Used to pass location from MainActivity to PostActivity

    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private static SharedPreferences preferences;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);
        MultiDex.install(this);
    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }

    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
    }
}