package com.example.android.newsapp;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.newsapp.databinding.ListItemBinding;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsAdapter extends ArrayAdapter<News> {

    private News currentNewsItem;
    private Date newsItemDate = null;

    // Tag for log messages.
    private static final String LOG_TAG = "NewsAdapter";

    /**
     * Constructs a new NewsAdapter.
     *
     * @param context is the current context
     * @param news    is the list of news items
     */
    NewsAdapter(Activity context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view, set Data Binding.
        ListItemBinding binding;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ListItemBinding) convertView.getTag();
        }

        // Get the News object located at this position in the list.
        currentNewsItem = getItem(position);

        // Make sure binding and currentNewsItem aren't null.
        assert binding != null;
        assert currentNewsItem != null;

        // Bind the views.
        ImageView ivArticleThumbnail = binding.ivArticleThumbnail;
        TextView tvCategory = binding.tvCategory;
        TextView tvArticle = binding.tvArticle;
        TextView tvContributor = binding.tvContributor;
        TextView tvDate = binding.tvDate;
        TextView tvTime = binding.tvTime;

        // Get the image associated with the article and set it on the ImageView,
        // if not available set the ImageView visibility to "GONE".
        if (currentNewsItem.getArticleThumbnail().equals("")) {
            // Remove the ImageView if an image isn't available.
            ivArticleThumbnail.setVisibility(View.GONE);
        } else {
            // Make sure the ImageView is visible if the image was previously unavailable.
            ivArticleThumbnail.setVisibility(View.VISIBLE);
            Picasso.get().load(currentNewsItem.getArticleThumbnail()).into(ivArticleThumbnail);
        }

        // Get the type of news that the article is classified as and set this text on the TextView.
        tvCategory.setText(currentNewsItem.getCategory());

        // Get the title of the article and set this text on the TextView.
        tvArticle.setText(currentNewsItem.getArticle());

        // Get the name of the person who wrote the article and set this text on the TextView,
        // if not available set "Contributor Unavailable" on the TextView.
        if (currentNewsItem.getContributor().equals("")) {
            // Make the text italic if the contributor is unavailable.
            tvContributor.setTypeface(Typeface.create(tvContributor.getTypeface(),
                    Typeface.ITALIC));
            tvContributor.setText(R.string.noContributor);
        } else {
            // Make sure the text returns to normal if the contributor was previously unavailable.
            tvContributor.setTypeface(Typeface.create(tvContributor.getTypeface(),
                    Typeface.NORMAL));
            tvContributor.setText(currentNewsItem.getContributor());
        }

        // Get the date of the article and set this text on the TextViews,
        // if not available set the TextViews visibility to "GONE".
        if (currentNewsItem.getDate().equals("")) {
            // Remove the date and time TextViews if the date is unavailable.
            tvDate.setVisibility(View.GONE);
            tvTime.setVisibility(View.GONE);
        } else {
            // Make sure the date and time TextViews are visible if the date was
            // previously unavailable.
            tvDate.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.VISIBLE);

            // Create a date object from the original date String.
            createDateObject();

            // Set the formatted date on the TextView.
            tvDate.setText(String.format(getContext().getString(R.string.dash), formatDate()));

            // Set the formatted time on the TextView.
            tvTime.setText(formatTime());
        }

        // Return the whole list item layout with the news data.
        return binding.getRoot();
    }

    // Create a date object.
    private void createDateObject() {
        // The original date formatting.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault());

        // Catch the exception.
        try {
            newsItemDate = dateFormat.parse(currentNewsItem.getDate());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Problem getting the date.", e);
        }
    }

    // Return the formatted date corresponding to user's locale.
    private String formatDate() {
        DateFormat formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return formattedDate.format(newsItemDate);
    }

    // Return the formatted time corresponding to user's locale.
    private String formatTime() {
        DateFormat formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT);
        return formattedTime.format(newsItemDate);
    }
}
