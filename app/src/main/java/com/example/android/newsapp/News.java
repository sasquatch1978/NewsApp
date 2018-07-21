package com.example.android.newsapp;

public class News {

    private String articleThumbnail;
    private String topic;
    private String article;
    private String contributor;
    private String date;
    private String url;

    /**
     * Create a new News object.
     *
     * @param articleThumbnail is the image associated with the article
     * @param topic            is the type of news that the article is classified as
     * @param article          is the title of the article
     * @param contributor      is the name of the person who wrote the article
     * @param date             is when the article was written
     * @param url              is the url for the actual article
     */
    News(String articleThumbnail, String topic, String article, String contributor,
         String date, String url) {
        this.articleThumbnail = articleThumbnail;
        this.topic = topic;
        this.article = article;
        this.contributor = contributor;
        this.date = date;
        this.url = url;
    }

    // Get the image associated with the article.
    public String getArticleThumbnail() {
        return articleThumbnail;
    }

    // Get the type of news that the article is classified as.
    public String getTopic() {
        return topic;
    }

    // Get the title of the article.
    public String getArticle() {
        return article;
    }

    // Get the name of the person who wrote the article.
    public String getContributor() {
        return contributor;
    }

    // Get when the article was written.
    public String getDate() {
        return date;
    }

    // Get the url for the actual article.
    public String getUrl() {
        return url;
    }
}
