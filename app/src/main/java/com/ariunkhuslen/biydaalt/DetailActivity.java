package com.ariunkhuslen.biydaalt;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle;
    ImageView detailImage;

    public static final String EXTRA_IMAGE = "Зураг";
    public static final String EXTRA_DESC = "Агуулга хийх зүйлс";
    public static final String EXTRA_TITLE = "Гарчиг";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize views
        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);

        // Retrieve data from the Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String desc = bundle.getString(EXTRA_DESC);
            String title = bundle.getString(EXTRA_TITLE);
            String imageUrl = bundle.getString(EXTRA_IMAGE);

            // Set data to views
            if (desc != null) {
                detailDesc.setText(desc);
            }
            if (title != null) {
                detailTitle.setText(title);
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(detailImage);
            } else {
                detailImage.setImageResource(R.drawable.uploadimg);
            }
        }
    }
}
