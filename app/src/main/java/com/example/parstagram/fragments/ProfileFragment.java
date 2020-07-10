package com.example.parstagram.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.example.parstagram.BitmapScaler;
import com.example.parstagram.DeviceDimensionsHelper;
import com.example.parstagram.GlideApp;
import com.example.parstagram.MainActivity;
import com.example.parstagram.ProfileAdapter;
import com.example.parstagram.R;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.app.Activity.RESULT_OK;

// class ProfileFragment loads all posts created by the current user
public class ProfileFragment extends PostsFragment {
    ProfileAdapter profileAdapter;

    public final String TAG = "ProfileFragment";
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    ImageView ivProfileImage;
    TextView tvUsername;
    ParseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);

        user = ParseUser.getCurrentUser();
        tvUsername.setText(user.getUsername());
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_IMAGE);

        if (profileImage != null) {
            GlideApp.with(Objects.requireNonNull(getContext()))
                    .load(profileImage.getUrl())
                    .transform(new RoundedCornersTransformation(50, 20))
                    .into(ivProfileImage);
        }
        else {
            ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24);
        }

        ivProfileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getContext(), "Set profile picture!", Toast.LENGTH_SHORT).show();
                onLaunchCamera(view);

                if (photoFile == null) {
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                    user.put(KEY_PROFILE_IMAGE, new ParseFile(photoFile));
                    user.saveInBackground();

                return true;
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);

        super.rvPosts.setLayoutManager(gridLayoutManager);

        profileAdapter = new ProfileAdapter(getContext(), super.allPosts);
        super.rvPosts.setAdapter(profileAdapter);

        super.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showProgressBar();
                //refresh posts here by calling query & set swipeContainer refreshing to false
                queryPosts();
                swipeContainer.setRefreshing(false);
                hideProgressBar();
            }
        });

    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        //      makes an implicit intent for image capture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // fileProvider wraps the photofile and is put into the storage
        Uri fileProvider = FileProvider.getUriForFile(Objects.requireNonNull(getContext()), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                File resizedUri = null;
                try {
                    resizedUri = resizeBitmap(rawTakenImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Picture wasn't saved!", Toast.LENGTH_SHORT).show();

                }
                assert resizedUri != null;
                Bitmap takenImage = BitmapFactory.decodeFile(resizedUri.getAbsolutePath());

                // Load the taken image into a preview
                    GlideApp.with(Objects.requireNonNull(getContext()))
                            .load(takenImage)
                            .transform(new RoundedCornersTransformation(50, 20))
                            .into(ivProfileImage);

            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File resizeBitmap(Bitmap rawTakenImage) throws IOException {
        // Get height or width of screen at runtime
        int screenWidth = DeviceDimensionsHelper.getDisplayWidth(Objects.requireNonNull(getContext()));

        //Resize bitmap
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, screenWidth);
        //Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //Compress the image further
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        //Create a new file for the resized bitmap
        File resizedFile = getPhotoFileUri(photoFileName + "_resize");

        resizedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(resizedFile);
        // Write the bytes of the bitmap to file
        fos.write(bytes.toByteArray());
        fos.close();

        return resizedFile;
    }

    // Returns the File for a photo stored on disk given the fileName
    // Uri = uniform resource identifier to represent the image captured
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(Objects.requireNonNull(getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        //limit query retrieval to posts where the user is the same as the current logged in user
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(POSTS_QUERY_LIMIT);

        query.addDescendingOrder(Post.KEY_CREATED_KEY);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
                profileAdapter.clear();
                allPosts.addAll(posts);
                profileAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void loadNextPosts() {
        //Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(POSTS_QUERY_LIMIT);
        query.whereLessThan("createdAt", allPosts.get(allPosts.size() - 1).getCreatedAt());

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
                allPosts.addAll(posts);
            }
        });
    }
}
