package com.example.parstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parstagram.models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.example.parstagram.PostsAdapter.KEY_PROFILE_IMAGE;

public class PostDetailsActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivImage;
    TextView tvDescription;
    TextView tvTimestamp;
    ImageView ivProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Post post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        tvUsername = findViewById(R.id.tvUsername);
        ivImage = findViewById(R.id.ivImage);
        tvDescription = findViewById(R.id.tvDescription);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        ivProfileImage = findViewById(R.id.ivProfileImage);

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
    }
}