package gidraf.tiaplayer.utils;

import android.content.SharedPreferences;

public class AppPreferences {

    public static SharedPreferences preferences;
    public static SharedPreferences.Editor edit_preferences;
    static AppPreferences singleton;
    public String app = "Ultra_HD_Preference";

    public AppPreferences() {
    }

    public static AppPreferences getInstance() {
        if (singleton == null) {
            singleton = new AppPreferences();
        }

        return singleton;
    }

    public void setAppTheme(String themeColor, String statusBarColor) {
        edit_preferences.putString("actionBar_color", themeColor).commit();
        edit_preferences.putString("statusBar_color", statusBarColor).commit();
    }
}
