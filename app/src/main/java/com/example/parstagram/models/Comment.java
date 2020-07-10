package com.example.parstagram.models;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class Comment extends ParseObject {

    public static final String KEY_POST = "replyingTo";
    public static final String KEY_USER = "user";
    public static final String KEY_CREATED_KEY = "createdAt";

    public Comment() {
    }

    public Post getPost() {
        return (Post) getParseObject(KEY_POST);
    }

    public void setPost(Post post) {
        put (KEY_POST, post);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

}
