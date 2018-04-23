package com.example.sbarai.openkart.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.sbarai.openkart.Models.CatalogueItem;
import com.example.sbarai.openkart.OpenOrderAddItem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.AsyncTask;

/**
 * Created by jithinjohn on 4/23/18.
 */

public abstract class AddItemImageRetrieverActivity extends AsyncTask<String, Void,Bitmap>{
    protected Object doInBackground(CatalogueItem item, ImageView ivItemImg) {
        URL url = null;
        try {
            url = new URL(item.getProductImg());
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            ivItemImg.setImageBitmap(bmp);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
