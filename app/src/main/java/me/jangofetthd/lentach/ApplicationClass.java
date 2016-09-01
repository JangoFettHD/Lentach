package me.jangofetthd.lentach;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Created by JangoFettHD on 31.08.2016.
 */
public class ApplicationClass extends Application {

    public static Context context;
    /*VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VKAccessToken is invalid
            }
        }
    };*/

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        VKSdk.initialize(this);
    }

}
