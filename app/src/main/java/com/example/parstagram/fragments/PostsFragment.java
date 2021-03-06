package com.example.parstagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parstagram.EndlessRecyclerViewScrollListener;
import com.example.parstagram.MainActivity;
import com.example.parstagram.PostsAdapter;
import com.example.parstagram.R;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class PostsFragment extends Fragment {

    public static final String TAG = "PostsFragment";
    public static final int POSTS_QUERY_LIMIT = 5;
    protected RecyclerView rvPosts;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);

        // 1. create a posts adapter and set the data source to allPosts
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);

        // 2. set the adapter on the recycler view
        rvPosts.setAdapter(adapter);

        // 3. set the layout manager on the recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);

        queryPosts();

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextPosts(page);
                hideProgressBar();
            }
        };

        // Add the scroll listener to recyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        //Set up refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showProgressBar();
                //refresh posts here by calling query & set swipeContainer refreshing to false
                queryPosts();
                swipeContainer.setRefreshing(false);
                hideProgressBar();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    public void showProgressBar() {
        // Show progress item
        MainActivity.miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        MainActivity.miActionProgressItem.setVisible(false);
    }

    protected void loadNextPosts(int page) {
        showProgressBar();
        //Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POSTS_QUERY_LIMIT);
        //query.whereLessThan("createdAt", allPosts.get(allPosts.size() - 1).getCreatedAt());
        query.setSkip(page * POSTS_QUERY_LIMIT);

        query.addDescendingOrder(Post.KEY_CREATED_KEY);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // e == null if success
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                if (posts.size() == 0) {
                    Toast.makeText(getContext(), "You've reached the end of all posts!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    protected void queryPosts() {
        //Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //get all the post objects from the database
        query.include(Post.KEY_USER);
        //set the limit to 20 posts being retrieved
        query.setLimit(POSTS_QUERY_LIMIT);

        query.addDescendingOrder(Post.KEY_CREATED_KEY);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // e == null if success
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
                adapter.clear();
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}