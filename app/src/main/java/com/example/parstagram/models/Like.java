package com.example.parstagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Objects;

@ParseClassName("Like")
public class Like extends ParseObject {

    public static final String KEY_POST = "postLiked";
    public static final String KEY_USER = "user";

    public Like() {
    }

    public String getPost() {
        return ((Post) Objects.requireNonNull(get(KEY_POST))).getObjectId();
    }

    public void setPost(Post post) {
        put(KEY_POST, post);
    }

    public ParseUser getUser() {
        return (ParseUser) get(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

}
