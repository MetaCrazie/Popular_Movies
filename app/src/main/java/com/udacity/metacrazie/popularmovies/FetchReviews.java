package com.udacity.metacrazie.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Pratyusha on 28/04/2016.
 */
public class FetchReviews extends AsyncTask<String, Void, ArrayList<ReviewObj>> {

    Context context;
    public ReviewAdapter reviewAdapter;
    RecyclerView reviewView;
    SharedPreferences pref;
    ArrayList<ReviewObj> reviewResults;
    int movieId;
    Uri buildUri;

    public FetchReviews(Context c, RecyclerView v, ArrayList<ReviewObj> revRes, int movieId) {
        super();
        context = c;
        reviewView = v;
        this.reviewResults = revRes;
        this.movieId = movieId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!reviewResults.isEmpty()) {
            reviewResults.clear();
        }
        Log.d("AsyncTask", "Started");
    }

    @Override
    protected ArrayList<ReviewObj> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        HttpURLConnection client = null;
        BufferedReader bufferedReader = null;
        String searchJSONstr = null;
        final String SEARCH_BASE_URL =
                "https://api.themoviedb.org/3/movie/";
        final String API_KEY = context.getString(R.string.api_key);
        final String API_KEY_PARAM = "api_key";




            //build the review URL

        buildUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath("reviews")
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

            Log.d("Background", "Started" );

            URL url = null;

            try {
                url = new URL(buildUri.toString());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            Log.d("URL", url.toString());
            try {
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.connect();
                InputStream inputStream = client.getInputStream();
                if (inputStream == null)
                    return null;
                StringBuilder buffer = new StringBuilder();


                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0)
                    return null;
                searchJSONstr = buffer.toString();
                Log.d("JSON Str", searchJSONstr);
            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (client != null) {
                    client.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("Main", "Error closing stream", e);
                    }
                }
            }

            try

            {
                return getSearchDataFromJson(searchJSONstr);
            } catch (
                    Exception e
                    )

            {
                e.printStackTrace();
            }

            return null;
        }

        private ArrayList<ReviewObj> getSearchDataFromJson (String searchJSONstr)
        throws Exception {
            final String LIST_NAME = "results";
            final String AUTHOR = "author";
            final String TEXT = "content";

            JSONObject searchResult = new JSONObject(searchJSONstr);
            JSONArray rArray = searchResult.getJSONArray(LIST_NAME);

            Log.d("reviewArray", rArray.toString());

            for (int i = 0; i < rArray.length(); i++) {
                JSONObject movieObject = rArray.getJSONObject(i);
                String author = movieObject.getString(AUTHOR);
                String text = movieObject.getString(TEXT);

                reviewResults.add(new ReviewObj(author, text));
            }

            return reviewResults;

        }

        @Override
        protected void onPostExecute (ArrayList < ReviewObj > Reviews) {
            super.onPostExecute(Reviews);

            if (Reviews.isEmpty()) {
                Reviews.add(new ReviewObj("", context.getString(R.string.no_reviews)));
            }
            reviewAdapter = new ReviewAdapter(Reviews, context);
            reviewView.setAdapter(reviewAdapter);
            reviewView.setVisibility(View.VISIBLE);
        }
    }
