package com.android_task;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private LinearLayout parentLayout;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentLayout = findViewById(R.id.parentLayout);
        executor = Executors.newSingleThreadExecutor();

        fetchData();
    }

    private void fetchData() {
        Future<String> future = executor.submit(() -> {
            try {
                URL url = new URL("https://www.reddit.com/top.json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                bufferedReader.close();
                inputStream.close();
                urlConnection.disconnect();

                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        try {
            String result = future.get();

            if (result != null) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray children = data.getJSONArray("children");

                for (int i = 0; i < children.length(); i++) {
                    JSONObject childObject =children.getJSONObject(i);
                    JSONObject childData = childObject.getJSONObject("data");

                    String authorName = childData.getString("author");
                    long publicationTime = childData.getLong("created_utc");
                    String thumbnailUrl = childData.getString("thumbnail");
                    int numComments = childData.getInt("num_comments");

                    createPostView(authorName, publicationTime, thumbnailUrl, numComments);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPostView(String authorName, long publicationTime, String thumbnailUrl, int numComments) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View postView = inflater.inflate(R.layout.post_layout, parentLayout, false);

        TextView authorTextView = postView.findViewById(R.id.authorTextView);
        TextView dateTextView = postView.findViewById(R.id.dateTextView);
        ImageView imageView = postView.findViewById(R.id.imageView);
        TextView commentsTextView = postView.findViewById(R.id.commentsTextView);

        authorTextView.setText("Author: " + authorName);
        dateTextView.setText("Date of Publication: " + FormatDate.fDate(publicationTime));
        Picasso.get().load(thumbnailUrl).into(imageView);
        commentsTextView.setText("Number of Comments: " + numComments);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the URL of the image in a browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(thumbnailUrl));
                startActivity(intent);
            }
        });

        // Add OnLongClickListener to the imageView
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Save the image to the gallery
                Picasso.get()
                        .load(thumbnailUrl)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                // Save the bitmap to the gallery
                                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Image", "Image description");
                                Toast.makeText(MainActivity.this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                // Handle the error
                                Toast.makeText(MainActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Not needed
                            }
                        });
                return true;
            }
        });

        parentLayout.addView(postView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}