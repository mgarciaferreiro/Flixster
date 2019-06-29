package com.example.flixster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flixster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    //constants
    public static final String API_BASE_URL = "https://api.themoviedb.org/3";
    public static final String API_KEY_PARAM = "api_key";
    public final static String TAG = "MovieDetailsActivity";

    Movie movie;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    TextView releaseDate;
    ImageView trailerPlaceholder;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        releaseDate = (TextView) findViewById(R.id.releaseDate);
        trailerPlaceholder = (ImageView) findViewById(R.id.trailerPlaceholder);

        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        String imageUrl = (String) Parcels.unwrap(getIntent().getParcelableExtra("backdropImageUrl"));
        Log.d("MovieDetailsActivity", String.format("Showing details for %s", movie.getTitle()));

        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        releaseDate.setText(String.format("Release date: %s", movie.getReleaseDate()));

        Glide.with(this)
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(this, 20, 0))
                .placeholder(R.drawable.flicks_backdrop_placeholder)
                .error(R.drawable.flicks_backdrop_placeholder)
                .into(trailerPlaceholder);

        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        getVideoId();
    }

    public void onPlayVideo(View view) {
        Intent intent = new Intent(this, MovieTrailerActivity.class);
        intent.putExtra("video_id", Parcels.wrap(videoId));
        this.startActivity(intent);
    }

    // get video id
    private void getVideoId() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = API_BASE_URL + "/movie/" + movie.getId() + "/videos";
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    JSONObject object = results.getJSONObject(0);
                    String id = object.getString("key");
                    String site = object.getString("site");
                    Log.i(TAG, String.format("Parsed video id %s", id));
                    if (site.equals("YouTube")) {
                        videoId = id;
                    } else {
                        videoId = null;
                    }
                } catch (JSONException e) {
                    Log.e("MovieTrailerActivity", "Failed to parse video id");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("MovieTrailerActivity", "Failed to get video id");
            }
        });
    }
}
