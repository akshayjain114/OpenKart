package com.example.sbarai.openkart;

import android.arch.lifecycle.ProcessLifecycleOwnerInitializer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sbarai.openkart.Models.CollaborationItem;
import com.example.sbarai.openkart.Models.Collaborator;
import com.example.sbarai.openkart.Models.ProspectOrder;
import com.example.sbarai.openkart.Utils.FirebaseManager;
import com.example.sbarai.openkart.Utils.NotificationHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class OpenOrderAddItem extends AppCompatActivity {

    String POid;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_order_add_item);

        Bundle extras = getIntent().getExtras();
        POid = extras.getString("POid");
        userId = FirebaseAuth.getInstance().getUid();

        initVariables();
    }

    private void initVariables() {
        View view = findViewById(R.id.submitItem);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitItem();
            }
        });
    }

    //TODO validate edittext data before sending.
    private void submitItem() {
        EditText etItemLink;
        EditText etItemName;
        EditText etItemRate;
        EditText etItemCount;

        etItemLink = findViewById(R.id.itemLink);
        etItemName = findViewById(R.id.itemName);
        etItemRate = findViewById(R.id.itemRate);
        etItemCount = findViewById(R.id.itemCount);

        if (etItemLink.getText().toString().equals("") || etItemName.getText().toString().equals("") || etItemRate.getText().toString().equals("") || etItemCount.getText().toString().equals("")){
            Toast.makeText(this, "Data invalid. Please check fields", Toast.LENGTH_SHORT).show();
            return;
        }

        final CollaborationItem item = new CollaborationItem();
        item.setItemLink(getFirebaseSafeLink(etItemLink.getText().toString()));
        item.setItemName(etItemName.getText().toString());
        item.setRatePerUnit(Float.valueOf(etItemRate.getText().toString()));
        item.setCount(Float.valueOf(etItemCount.getText().toString()));

        FirebaseManager.getRefToSpecificProspectOrder(POid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final ProspectOrder order = dataSnapshot.getValue(ProspectOrder.class);
                if (order == null){
                    return;
                }
                Collaborator currentCollaborator;
                if (order.getCollaborators() == null)
                    order.setCollaborators(new HashMap<String, Collaborator>());
                if (order.getCollaborators().get(userId) != null) {
                    currentCollaborator = order.getCollaborators().get(userId);
                }else{
                    currentCollaborator = new Collaborator();
                    currentCollaborator.setUserId(userId);
                }
                currentCollaborator.addCollaborationItem(item);
                order.addCollaborator(currentCollaborator);
                dataSnapshot.getRef().setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OpenOrderAddItem.this, "Item added successfully", Toast.LENGTH_SHORT).show();

                        //Check if target amount is reached
                        float targetTotal = order.getTargetTotal();
                        float amountReached = getAmountReached(order);

                        if(amountReached >= targetTotal){
                            //Call firebase API to send a notification!
                            new NotificationTask(POid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            Log.i("Akshay", "Let's see");
                        }

                        finish();
                    }

                    private float getAmountReached(ProspectOrder order) {
                        float amountReached = 0;
                        if (order.getCollaborators() != null) {
                            for (String collaboratorHashKey : order.getCollaborators().keySet()) {
                                Collaborator currentCollaborator = order.getCollaborators().get(collaboratorHashKey);
                                if (currentCollaborator.getCollaborationItems() != null){
                                    for (String itemHashKey: currentCollaborator.getCollaborationItems().keySet()){
                                        CollaborationItem item = currentCollaborator.getCollaborationItems().get(itemHashKey);
                                        amountReached += item.getCount()*item.getRatePerUnit();
                                    }
                                }
                            }
                        }
                        return amountReached;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getFirebaseSafeLink(String s) {
        s = s.replace('.',',');
        s = s.replace('/','\\');
        return s;
    }

    private class NotificationTask extends AsyncTask<Object, Void, String>{

        String topicId;

        NotificationTask(String rt){
            topicId = rt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("Akshay", "Pre execute");
        }
        @Override
        protected String doInBackground(Object... params){
            try{
                NotificationHelper.sendNotification("OpenKart: Cart Ready", "Your cart has reached the target amount", topicId);
            }
            catch (Exception ex){
                Log.e("Exception occured", ex.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String msg) {
            Log.i("AsyncTask", "onPostExecute");
        }
    }

}