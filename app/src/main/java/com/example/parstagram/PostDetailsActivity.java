package com.example.parstagram;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

import static com.example.parstagram.PostsAdapter.KEY_PROFILE_IMAGE;
import static com.example.parstagram.models.Post.KEY_LIKES;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailsActivity";
    TextView tvUsername;
    ImageView ivImage;
    TextView tvDescription;
    TextView tvTimestamp;
    TextView tvNumLikes;
    ImageView ivProfileImage;
    ImageView ivLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        final Post post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        tvUsername = findViewById(R.id.tvUsername);
        ivImage = findViewById(R.id.ivImage);
        tvDescription = findViewById(R.id.tvDescription);
        tvNumLikes = findViewById(R.id.tvNumLikes);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivLike = findViewById(R.id.ivLike);

        assert post != null;

        tvDescription.setText(post.getDescription());
        tvUsername.setText(post.getUser().getUsername());
        tvTimestamp.setText(Post.getRelativeTimeAgo(post.getCreatedAt().toString()));
        ParseFile image = post.getImage();
        //ParseFile is used by parse to define/store images
        if (image != null) {
            Glide.with(this).load(post.getImage().getUrl()).into(ivImage);
        }
        else {
            ivImage.setVisibility(View.GONE);
        }

        ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE_IMAGE);

        if (profileImage != null) {
            GlideApp.with(this)
                    .load(profileImage.getUrl())
                    .transform(new RoundedCornersTransformation(10, 5))
                    .into(ivProfileImage);
        }
        else {
            ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24);
        }

        if (post.getLikes() != null) {
            tvNumLikes.setText(new StringBuilder().append(post.getLikes().size()).append(" like(s)").toString());
        } else {
            tvNumLikes.setText(R.string.zero_likes);
        }

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
                //update like count if the like hasn't been selected already
                if (!ivLike.isSelected()) {
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

                    tvNumLikes.setText(new StringBuilder().append(post.getLikes().size()).append(" likes").toString());

                    //put it back to the Parse server
                    post.saveInBackground();
                }
            }
        });

    }
}