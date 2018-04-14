package com.example.sbarai.openkart.Utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aksha on 4/14/2018.
 */

public class NotificationHelper {

    final static String apiKey = "AAAApNg8CD8:APA91bGeCvAZK3ENgOBwSnuNaaqCm60rkyeNKv6a0hNvAOzC9fM8VdOfy2xW0i3Maq2ntSkyW-4tsnEu40TRiyxFzexgZM4UPKaz_4ukyOhqDO-G3nF31ybJuqs22NVK-FawNJigC5SI";

    public static void sendNotification(String title, String body, String deviceToken){

        URL url = null;
        try {
            url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + apiKey);
            conn.setDoOutput(true);
            JSONObject message = buildNotification(title, body, deviceToken);

            OutputStream os = conn.getOutputStream();
            os.write(message.toString().getBytes());
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            Log.i("Notif resp code", String.valueOf(responseCode));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", e.toString());
        }

    }

    public static JSONObject buildNotification(String title, String body, String deviceToken){
        JSONObject message = new JSONObject();
        try {
            message.put("to", deviceToken);
            message.put("priority", "high");

            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", "Your cart has reached the target amount!");
            message.put("notification", notification);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Exception", e.toString());
            return null;
        }
        return message;
    }

}
