package com.example.parstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivImage;
    TextView tvDescription;
    TextView tvTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Post post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        tvUsername = findViewById(R.id.tvUsername);
        ivImage = findViewById(R.id.ivImage);
        tvDescription = findViewById(R.id.tvDescription);
        tvTimestamp = findViewById(R.id.tvTimestamp);

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
    }
}