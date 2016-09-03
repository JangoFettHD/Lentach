package me.jangofetthd.lentach;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiDocument;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiVideo;
import com.vk.sdk.api.model.VKAttachments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.jangofetthd.lentach.Utility.ItemClickSupport;

public class NewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<NewsFeedRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private List<VKApiPost> posts;
    Context context;

    public NewsFeedRecyclerViewAdapter(List<VKApiPost> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }


    @Override
    public NewsFeedRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NewsFeedRecyclerViewAdapter.ViewHolder holder, int position) {
        VKApiPost post = posts.get(position);

        if (!post.text.isEmpty()) {
            holder.pText.setVisibility(View.VISIBLE);
            holder.pText.setText(post.text);
        } else {
            holder.pText.setVisibility(View.GONE);
        }

        //new
        holder.countLikes.setText(Integer.toString(post.likes_count));
        holder.countReposts.setText(Integer.toString(post.reposts_count));

        holder.button_likes.setOnClickListener(this);
        holder.button_likes.setTag(Integer.toString(position));

        long time = post.date * (long) 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat();//"HH:mm:ss dd.MM.YY"); //@TODO TIME FOR SAMSUNG
        holder.pTime.setText(format.format(date).toString());

        List<String> photos = new ArrayList<>(),
                documents = new ArrayList<>(),
                videos = new ArrayList<>();
        List<VKApiAudio> audios = new ArrayList<>();

        for (VKAttachments.VKApiAttachment attachment : post.attachments) {
            if (attachment.getType().equals(VKAttachments.TYPE_PHOTO)) {
                photos.add(((VKApiPhoto) attachment).photo_604);
            } else if (attachment.getType().equals(VKAttachments.TYPE_VIDEO)) {
                videos.add(((VKApiVideo) attachment).external);
            } else if (attachment.getType().equals(VKAttachments.TYPE_DOC)) {
                documents.add(((VKApiDocument) attachment).url);
            } else if (attachment.getType().equals(VKAttachments.TYPE_AUDIO)) {
                audios.add((VKApiAudio) attachment);
            }
        }

        if (photos.isEmpty()) {
            holder.pImagesScroll.setVisibility(View.GONE);
        } else {
            holder.pImagesScroll.setVisibility(View.VISIBLE);
            LinearLayout linearLayout = holder.llPhotos;
            linearLayout.removeAllViewsInLayout();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for (String url : photos) {
                ImageView image = new ImageView(context);
                linearLayout.addView(image, layoutParams);
                Glide.with(context).load(url).into(image);
            }
            ViewGroup.LayoutParams params = holder.pImagesScroll.getLayoutParams();
            if (photos.size() != 1) {
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics());
            } else {
                params = layoutParams;
                holder.pImagesScroll.setLayoutParams(params);
            }
        }
        MainActivity activity = (MainActivity) context;
        if (audios.isEmpty()) {
            holder.pAudiosRecyclerView.setVisibility(View.GONE);
        } else {
            holder.pAudiosRecyclerView.setVisibility(View.VISIBLE);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            holder.pAudiosRecyclerView.setLayoutManager(manager);

            AudioRecyclerViewAdapter adapter = new AudioRecyclerViewAdapter(audios, context);
            holder.pAudiosRecyclerView.setAdapter(adapter);

            ItemClickSupport.addTo(holder.pAudiosRecyclerView).setOnItemClickListener((recyclerView, position1, v) -> {
                if (activity.musicService.getPlayingSong().getId() != audios.get(position1).getId()) {
                    activity.vkAudios.clear();
                    activity.vkAudios.addAll(audios);
                    activity.musicService.setSong(position1);
                    activity.musicService.playSong();
                } else {
                    activity.musicService.pause();
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            });

        }
        if (documents.isEmpty()) {
            holder.pGifsScroll.setVisibility(View.GONE);
        } else {
            holder.pGifsScroll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.button_likes) {
            final VKApiPost post = posts.get(Integer.parseInt((String) view.getTag()));

            VKParameters parameters = new VKParameters();
            parameters.put("type", "post");
            parameters.put("owner_id", Integer.toString(post.from_id));
            parameters.put("item_id", Integer.toString(post.id));

            final VKRequest request = new VKRequest("likes.add", parameters);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    TextView likes_count = (TextView) view.findViewById(R.id.countLikes);
                    likes_count.setText(Integer.toString(post.likes_count));
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                }
            });
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pText;
        private TextView pTime;
        private TextView countReposts;
        private TextView countLikes;
        private HorizontalScrollView pImagesScroll;
        private HorizontalScrollView pGifsScroll;
        private RecyclerView pAudiosRecyclerView;
        private ImageButton pLike;
        private ImageButton pRepost;
        private LinearLayout llPhotos;
        private LinearLayout llDocs;
        private RelativeLayout button_likes;

        public ViewHolder(View itemView) {
            super(itemView);
            pText = (TextView) itemView.findViewById(R.id.text);
            pTime = (TextView) itemView.findViewById(R.id.time);
            countReposts = (TextView) itemView.findViewById(R.id.countReposts);
            countLikes = (TextView) itemView.findViewById(R.id.countLikes);
            pImagesScroll = (HorizontalScrollView) itemView.findViewById(R.id.photos);
            pGifsScroll = (HorizontalScrollView) itemView.findViewById(R.id.gifs);
            pAudiosRecyclerView = (RecyclerView) itemView.findViewById(R.id.audios);
            pLike = (ImageButton) itemView.findViewById(R.id.like);
            pRepost = (ImageButton) itemView.findViewById(R.id.repost);
            llPhotos = (LinearLayout) itemView.findViewById(R.id.llphotos);
            llDocs = (LinearLayout) itemView.findViewById(R.id.llgifs);
            button_likes = (RelativeLayout) itemView.findViewById(R.id.button_likes);
        }
    }

}
