package com.example.our_capstone;

import android.net.Uri;

public class RecycleModel {
    private Uri uri;
    private String nm ;

    public RecycleModel(Uri uri, String nm) {
        this.uri = uri;
        this.nm = nm;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }
}
