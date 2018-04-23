package com.example.sbarai.openkart.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.sbarai.openkart.Models.CatalogueItem;
import com.example.sbarai.openkart.OpenOrderAddItem;
import com.example.sbarai.openkart.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.AsyncTask;

/**
 * Created by jithinjohn on 4/23/18.
 */

public abstract class AddItemImageRetrieverActivity extends AsyncTask<String, Void,Bitmap>{
    protected Bitmap doInBackground(String... strings) {
        return null;
    }
}
