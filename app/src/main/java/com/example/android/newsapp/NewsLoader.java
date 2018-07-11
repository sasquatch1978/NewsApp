package com.example.android.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

// Loads a list of news by using an AsyncTask to perform the network request to the given URL.
public class NewsLoader extends AsyncTaskLoader<List<News>> {

    private String url;

    /**
     * Constructs a new NewsLoader.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    NewsLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        // Don't perform the request if there isn't a URL.
        if (url == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of news.
        return QueryUtils.fetchNewsData(url);
    }
}
