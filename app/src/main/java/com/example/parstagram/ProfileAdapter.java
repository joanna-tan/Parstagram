package com.example.parstagram;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;


public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {


    private static final String KEY_PROFILE_IMAGE = "profileImage";
    private Context context;
    private List<Post> posts;

    public ProfileAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_post, parent, false);
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
        private ImageView ivImage;
        private ImageView ivProfileImage;
        private TextView tvUsername;
        private RelativeLayout content;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            content = itemView.findViewById(R.id.content);
        }

        public void bind(final Post post) {

            ParseFile image = post.getImage();
            //ParseFile is used by parse to define/store images
            if (image != null) {
                Glide.with(context).load(post.getImage().getUrl()).into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
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



        }
    }
}