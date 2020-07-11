package com.example.parstagram;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.models.Like;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_USERNAME = "username";

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private ImageView ivImage;
        private ImageView ivLike;
        private TextView tvDescription;
        private ConstraintLayout content;
        private TextView tvTimestamp;
        private ImageView ivProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            content = itemView.findViewById(R.id.post_item);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivLike = itemView.findViewById(R.id.ivLike);
        }

        public void bind(final Post post) {
            // Bind the post data to the view elements
            tvDescription.setText(post.getDescription());
            tvUsername.setText(post.getUser().getUsername());
            tvTimestamp.setText(Post.getRelativeTimeAgo(post.getCreatedAt().toString()));
            ParseFile image = post.getImage();
            //ParseFile is used by parse to define/store images
            if (image != null) {
                Glide.with(context).load(post.getImage().getUrl()).into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
            }

            ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE_IMAGE);

            if (profileImage != null) {
                GlideApp.with(context)
                        .load(profileImage.getUrl())
                        .transform(new RoundedCornersTransformation(10, 5))
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24);
            }

            //set an onClickListener to the entire post body & go to post details
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("post", Parcels.wrap(post));
                    context.startActivity(intent);

                }
            });

            //find if the post has been liked by the current user
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
            query.include(Like.KEY_USER);
            query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());

            query.findInBackground(new FindCallback<Like>() {
                @Override
                public void done(List<Like> likes, ParseException e) {
                    // e == null if success
                    if (e != null) {
                        Log.e(TAG, "Issue with getting posts", e);
                        return;
                    }

                    for (Like like : likes) {
                        Log.i(TAG, "Post: " + like.getPost());
                        if (like.getPost().equals(post.getObjectId())) {
                            ivLike.setSelected(true);
                        }
                    }

                }
            });

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ivLike.setSelected(true);

                    //get the like array from post
                    Like like = new Like();
                    like.setUser(ParseUser.getCurrentUser());
                    like.setPost(post);

                    ArrayList<Like> likes = post.getLikes();
                    if (likes == null) {
                        likes = new ArrayList<>();
                        post.put(KEY_LIKES, likes);
                    }
                    //add one like to it
                    post.getLikes().add(like);

                    //put it back to the Parse server
                    post.saveInBackground();
                }
            });

        }


    }
}