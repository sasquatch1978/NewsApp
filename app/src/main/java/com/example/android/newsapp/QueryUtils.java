package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    // Tag for the log messages
    private static final String LOG_TAG = "QueryUtils";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Guardian dataset and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Return the list of {@link News}
        return extractFeatureFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractFeatureFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<News> news = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of results (or news items).
            JSONArray newsArray = baseJsonResponse.getJSONObject("response").getJSONArray
                    ("results");

            // For each news item in the newsArray, create an {@link News} object
            for (int i = 0; i < newsArray.length(); i++) {

                // Get a single news item at position i within the list of news
                JSONObject currentNewsItem = newsArray.getJSONObject(i);

                // Extract the value for the "currentNewsItem" object with the key called
                // "sectionName"
                String category = currentNewsItem.getString("sectionName");

                // Extract the value for the "currentNewsItem" object with the key called "webTitle"
                String article = currentNewsItem.getString("webTitle");

                // Check to see if there is a date.
                String date = "";
                if (currentNewsItem.has("webPublicationDate")) {
                    // Extract the value for the "currentNewsItem" object with the key called
                    // "webPublicationDate"
                    date = currentNewsItem.getString("webPublicationDate");
                }

                // Extract the value for the "currentNewsItem" object with the key called "webUrl"
                String url = currentNewsItem.getString("webUrl");

                // For a given news item, extract the JSONObject associated with the
                // key called "fields" if it exists, which represents a list of all fields
                // for that news item.
                String articleThumbnail = "";
                if (currentNewsItem.has("fields")) {
                    JSONObject fields = currentNewsItem.getJSONObject("fields");
                    // Check to see if there is an image.
                    if (fields.has("thumbnail")) {
                        // Extract the value for the "fields" object with the key called "thumbnail"
                        articleThumbnail = fields.getString("thumbnail");
                    }
                }

                // For a given news item, extract the JSONArray associated with the
                // key called "tags", which represents a list of all tags
                // for that news item.
                JSONArray tags = currentNewsItem.getJSONArray("tags");

                // Check to see if there is a contributor.
                String contributor = "";
                if (tags.length() != 0) {
                    JSONObject tagObject = tags.getJSONObject(0);
                    if (tagObject.has("webTitle")) {
                        // Extract the value for the "tags" object with the key called "webTitle"
                        contributor = tagObject.getString("webTitle");
                    }
                }

                // Create a new {@link News} object with the articleThumbnail, category, article,
                // contributor, newsItemDate, and url from the JSON response.
                News newsItem = new News(articleThumbnail, category, article, contributor, date,
                        url);

                // Add the new {@link News} to the list of news items.
                news.add(newsItem);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        // Return the list of news items.
        return news;
    }
}
