package me.jangofetthd.lentach;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.jangofetthd.lentach.Services.MusicService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    List<VKApiPost> vkPosts;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    NavigationView navigationView;

    MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    public static MainActivity instance;

    ArrayList<VKApiAudio> vkAudios = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //new
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        set_username();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        swipeRefresh.setOnRefreshListener(this::vkBackgroundLoading);

        //Posts
        recyclerView = (RecyclerView) findViewById(R.id.feedview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        vkPosts = new ArrayList<>();

        NewsFeedRecyclerViewAdapter adapter = new NewsFeedRecyclerViewAdapter(vkPosts, this);
        recyclerView.setAdapter(adapter);

        vkBackgroundLoading();

        /**
         * Service starting
         */
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //navigationView.getMenu().getItem(0).setTitle();
        if (VKSdk.isLoggedIn()) {
            //vk.setText("Выйти");
            navigationView.getMenu().getItem(2).getSubMenu().getItem(0).setTitle("Выйти");
        } else {
            //vk.setText("Войти");
            navigationView.getMenu().getItem(2).getSubMenu().getItem(0).setTitle("Войти");
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            // Handle the camera action
        } else if (id == R.id.vklogin) {
            //new
            if (!VKSdk.isLoggedIn())
                VKSdk.login(this, "photos", "nohttps", "offline", "wall");
            else
            //Toast.makeText(MainActivity.this, "Вы уже авторизировались!", Toast.LENGTH_SHORT).show(); //@TODO доделать кнопку авторизации
            {
                VKSdk.logout();
            }
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_audio){
            Intent goAudio = new Intent(this, AudioPlayerActivity.class);
            startActivity(goAudio);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //new
    public void set_username() {
        View header = navigationView.getHeaderView(0);
        final TextView username = (TextView) header.findViewById(R.id.nav_username);
        final TextView status = (TextView) header.findViewById(R.id.nav_status);
        final ImageView avatar = (ImageView) header.findViewById(R.id.nav_avatar);

        VKParameters parameters = new VKParameters();
        parameters.put("fields", "status,photo_100");

        final VKRequest request = new VKRequest("users.get", parameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                //код обработки объекта
                try {
                    JSONArray jsonObject = response.json.getJSONArray("response");
                    Log.w("JSON", "Vk response json: " + jsonObject.toString());
                    JSONObject name = jsonObject.getJSONObject(0);
                    Log.w("JSON", "" + name);
                    Log.w("OFOFO1", "" + name.get("first_name"));
                    Log.w("OFOFO2", "" + name.get("last_name"));
                    Log.w("OFOFO3", "" + name.get("status"));
                    Log.w("OFOFO4", "" + name.get("photo_100"));
                    //avatar_url=name.get("photo_100").toString();
                            /*for (int i=0; i<jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                vkPosts.add(new VKApiPost(object));
                            }*/

                    /*try{if (VKSdk.isLoggedIn()){
                        Glide.with(ApplicationClass.context).load((name.get("photo_100")).toString()).into(avatar);}}catch (Exception e){
                    }*/

                    Glide.with(ApplicationClass.context).load((name.get("photo_100")).toString()).bitmapTransform(new CropCircleTransformation(ApplicationClass.context)).into(avatar);
                    username.setText((name.get("first_name")).toString() + " " + (name.get("last_name")).toString());
                    status.setText((name.get("status")).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
        //final NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);


    }

    //new
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {

                set_username();

                //username.setText(VKRequest.getRegisteredRequest());
            }

            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class VkAsyncPostsLoading extends AsyncTask<Void, Void, Void> {

        int[] groups;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefresh.setRefreshing(true);

            groups = getResources().getIntArray(R.array.groups);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            vkPosts.clear();

            for (int group : groups) {
                Map<String, Object> params = new ArrayMap<>(3);
                params.put("owner_id", Integer.toString(group));
                params.put("offset", "0");
                params.put("count", "20");

                VKRequest request = VKApi.wall().get(new VKParameters(params));

                request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        try {
                            JSONObject jsonObject = response.json.getJSONObject("response");
                            Log.w("JSON", "Vk response json: " + jsonObject.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                vkPosts.add(new VKApiPost(object));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            swipeRefresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        super.attemptFailed(request, attemptNumber, totalAttempts);
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                    }

                    @Override
                    public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                        super.onProgress(progressType, bytesLoaded, bytesTotal);
                    }
                });
            }

            Collections.sort(vkPosts, new VkPostsComparator());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.getAdapter().notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
        }
    }

    private void vkBackgroundLoading() {
        new VkAsyncPostsLoading().execute();
    }


    class VkPostsComparator implements Comparator<VKApiPost> {

        @Override
        public int compare(VKApiPost first, VKApiPost second) {
            return (int) (second.date - first.date);
        }
    }

    /**
     * Connection to service
     */
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setList(vkAudios);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

}