package com.example.android.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.newsapp.databinding.ActivityNewsBinding;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {

    private NewsAdapter adapter;
    private boolean isConnected;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private static final int NEWS_LOADER_ID = 1;

    // Enter the api key here, so it can be easily removed and added as needed.
    private static final String API_KEY = "18c2ab65-dca1-48ac-a2a5-2b7026dac1b4";

    // Guardian search url.
    private static final String NEWS_REQUEST_URL =
            "https://content.guardianapis.com/search?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNewsBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_news);

        // Bind the views.
        progressBar = binding.progressBar;
        ListView newsList = binding.newsList;
        tvStatus = binding.tvStatus;

        // Set the adapter.
        adapter = new NewsAdapter(this, new ArrayList<News>());
        newsList.setAdapter(adapter);

        // Check to see if there is an network connection.
        checkNetworkConnection();

        // Set the empty state, if there aren't any results.
        newsList.setEmptyView(tvStatus);

        // If there is a network connection, fetch the data.
        if (isConnected) {
            // Initialize the loader.
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Display an error.
            // Hide the ProgressBar.
            progressBar.setVisibility(View.GONE);

            // Set this text if there isn't a network connection.
            tvStatus.setText(R.string.no_connection);
        }

        // Set a listener for the list that opens the Guardian web page for the news item that
        // is clicked.
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Get the position of the news item that was clicked.
                News currentNewsItem = adapter.getItem(position);

                // Create an intent to open the Guardian web page for the news item.
                Intent newsWebPage = new Intent(Intent.ACTION_VIEW);
                assert currentNewsItem != null;
                newsWebPage.setData(Uri.parse(currentNewsItem.getUrl()));

                // Make sure an app is installed to complete this action.
                if (newsWebPage.resolveActivity(getPackageManager()) != null) {
                    // Start the intent if there is an app installed to handle the intent.
                    startActivity(newsWebPage);
                } else {
                    // Show a toast if there isn't an app installed to handle the intent.
                    Toast noAppForIntent = Toast.makeText(getApplication(),
                            getString(R.string.install_web_browser), Toast.LENGTH_SHORT);
                    noAppForIntent.setGravity(Gravity.CENTER, 0, 0);
                    noAppForIntent.show();
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<List<News>> onCreateLoader(int id, @Nullable Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        /*
        getString retrieves a String value from the preferences. The second parameter is
        the default value for this preference.
        */
        // Preference for the topic of news articles to display.
        String topic = sharedPrefs.getString(
                getString(R.string.settings_topic_key),
                getString(R.string.settings_topic_default)
        );

        // Preference to search the news articles.
        String search = sharedPrefs.getString(
                getString(R.string.settings_search_key),
                getString(R.string.settings_search_default)
        );

        // Preference to order the news articles by.
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        // Preference for the number of news articles to display.
        String numberOfItems = sharedPrefs.getString(
                getString(R.string.settings_number_of_items_key),
                getString(R.string.settings_number_of_items_default)
        );

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(NEWS_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value (example: "order-by=oldest").
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        // If the topic field isn't blank add a parameter to show specific topics.
        if (!topic.trim().equals("")) {
            uriBuilder.appendQueryParameter("section", topic);
        }
        // If the search field isn't blank add a parameter to search for specific tags.
        if (!search.trim().equals("")) {
            uriBuilder.appendQueryParameter("q", search);
        }
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("page-size", numberOfItems);

        // Return the completed uri
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<News>> loader, List<News> news) {

        // Clear out existing data.
        adapter.clear();
        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            adapter.addAll(news);
        }

        // Hide the ProgressBar.
        progressBar.setVisibility(View.GONE);

        // Set this text if there aren't any results.
        tvStatus.setText(R.string.no_news);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<News>> loader) {
        // Clear out existing data.
        adapter.clear();
    }

    @Override
    // This method initializes the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    // This method is called whenever an item in the options menu is selected.
    public boolean onOptionsItemSelected(MenuItem item) {
        // Determine which item is selected and take the appropriate action.
        int id = item.getItemId();
        // Open settings.
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Checks to see if there is an network connection.
    public void checkNetworkConnection() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the details of the active network.
        assert connMgr != null;
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnected();
    }
}
