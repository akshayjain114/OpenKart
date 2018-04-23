package com.example.sbarai.openkart;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.example.sbarai.openkart.Models.CollaborationItem;
import com.example.sbarai.openkart.Models.Collaborator;
import com.example.sbarai.openkart.Models.ProspectOrder;
import com.example.sbarai.openkart.Utils.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import static java.time.temporal.ChronoUnit.DAYS;

public class ProspectOrderDetails extends AppCompatActivity {

//    BottomSheetDialog dialog;
    String POid;
    String userId;
    Button chatButton;
    View addItem;
    LayoutInflater inflater;
    LinearLayout listOfItems;
    Boolean smart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prospect_order_details);
        Bundle extras = getIntent().getExtras();
        POid = extras.getString("POid");
        userId = FirebaseAuth.getInstance().getUid();

        initVariable();
        fillData();
    }

    private void fillData() {
        final TextView storeName = findViewById(R.id.storeName);
        TextView initiatedBy = findViewById(R.id.initiated_by);
        final TextView orderDate = findViewById(R.id.order_date);
        final TextView targetValue = findViewById(R.id.tvalue);
        final TextView amtReached = findViewById(R.id.avalue);
        final TextView remainingAmount = findViewById(R.id.rvalue);
        final TextView totalCollaborators = findViewById(R.id.collaborators_num);
        final TextView totalItems = findViewById(R.id.items_num);
        final TextView distanceView = findViewById(R.id.milesText);
        final TextView customLocation = findViewById(R.id.shipping_address);

        setInitiatedBy(initiatedBy);

        FirebaseManager.getRefToSpecificProspectOrder(POid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ProspectOrder order = dataSnapshot.getValue(ProspectOrder.class);
                        storeName.setText(order.getDesiredStore());
                        setDateToView(order.getOrderDate(),orderDate);
                        setAmounts(targetValue, amtReached, remainingAmount,
                                totalCollaborators, totalItems, order);
                        distanceView.setText(getDistanceFromLocation(order.getLocLat(), order.getLocLon()) + " Miles");
                        customLocation.setText("Custom location. (Click)");
                        customLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openMapsToALocation(order.getLocation());
                            }
                        });
                        smart=order.isSmart();
                        if(order.getCreatorKey().equals(userId)){
                            Button b_merge = findViewById(R.id.button_merge);
                            b_merge.setVisibility(View.VISIBLE);
                            b_merge.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(ProspectOrderDetails.this, MergerCarts.class);
                                    Bundle b = new Bundle();
                                    b.putString("mergingKey", POid);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setInitiatedBy(final TextView initiatedBy) {
        FirebaseManager.getRefToUserName(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        initiatedBy.setText(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private void openMapsToALocation(Location location) {
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", location.getLatitude(), location.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);

    }

    private void setAmounts(TextView targetValue,
                            TextView amountReached,
                            TextView remainingAmount,
                            TextView totalCollaborators,
                            TextView totalItems,
                            ProspectOrder order) {
        targetValue.setText(String.valueOf(order.getTargetTotal()));
        int totalColaboratorsCount = 0;
        int totalItemsCount = 0;
        float amtReached = 0;
        if (order.getCollaborators() != null) {
            for (String collaboratorHashKey : order.getCollaborators().keySet()) {
                totalColaboratorsCount++;
                Collaborator currentCollaborator = order.getCollaborators().get(collaboratorHashKey);
                if (currentCollaborator.getCollaborationItems() != null){
                    for (String itemHashKey: currentCollaborator.getCollaborationItems().keySet()){
                        totalItemsCount++;
                        CollaborationItem item = currentCollaborator.getCollaborationItems().get(itemHashKey);
                        amtReached += item.getCount()*item.getRatePerUnit();
                    }
                }
            }
        }
        amountReached.setText(String.valueOf(amtReached));
        float remainder = order.getTargetTotal() - amtReached;
        if (remainder < 0) remainder = 0;
        remainingAmount.setText(String.valueOf(remainder));
        totalCollaborators.setText(String.valueOf(totalColaboratorsCount) + " Collaborators");
        totalItems.setText(String.valueOf(totalItemsCount) + " Total items");
    }

    public static String getDistanceFromLocation(double lat1, double lon1) {

        double lat2 = OpenOrders.location.getLatitude();
        double lon2 = OpenOrders.location.getLongitude();

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = 0;

        distance = Math.sqrt(Math.pow(distance, 2) + Math.pow(height, 2)) ;
        distance = distance * 0.00062137;


        return String.format("%.2f", distance);

    }

    private void setDateToView(long orderDate, TextView orderDateView) {
        long now = System.currentTimeMillis();
        long diff = now - orderDate;
        int daysToGo = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (daysToGo < 0)
            orderDateView.setText("overdue");
        else if (daysToGo == 0)
            orderDateView.setText("Today");
        else if (daysToGo == 1)
            orderDateView.setText("Tomorrow");
        else
            orderDateView.setText("" + daysToGo + " Days left");
    }

    private void getOrderDetailsAndShowItems() {
        FirebaseManager.getRefToSpecificProspectOrder(POid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProspectOrder order = dataSnapshot.getValue(ProspectOrder.class);
                showAllItems(order);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listOfItems.removeAllViews();
        getOrderDetailsAndShowItems();
    }

    private void showAllItems(ProspectOrder order) {
        //dummy data
        if (order == null)
            return;
        if (order.getCollaborators() != null){
            for (String collaboratorHashKey : order.getCollaborators().keySet()) {
                Collaborator collaborator = order.getCollaborators().get(collaboratorHashKey);
                View collaboratorView = inflater.inflate(R.layout.prospect_order_collaborator_name, null);
                TextView collaboratorName = collaboratorView.findViewById(R.id.collaborator_name);
                setCollaboratorName(collaboratorName, collaboratorHashKey);
                listOfItems.addView(collaboratorView);

                if (collaborator.getCollaborationItems() != null) {
                    for (String itemHashKey : collaborator.getCollaborationItems().keySet()) {
                        final CollaborationItem item = collaborator.getCollaborationItems().get(itemHashKey);
                        View itemView = inflater.inflate(R.layout.prospect_order_item_details, null);
                        TextView itemName = itemView.findViewById(R.id.item_name);
                        itemName.setText(item.getItemName());
                        TextView itemCount = itemView.findViewById(R.id.item_count);
                        itemCount.setText(String.valueOf(item.getCount()));
                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getFirebaseUnsafeString(item.getItemLink())));
                                startActivity(browserIntent);
                            }
                        });

                        listOfItems.addView(itemView);
                    }
                }
            }
        }
//        listOfItems.addView(inflater.inflate(R.layout.prospect_order_item_details,null));
    }

    private String getFirebaseUnsafeString(String s) {
        s = s.replace(",",".");
        s = s.replace("\\","/");
        return s;
    }

    private void setCollaboratorName(final TextView collaboratorName, String collaboratorHashKey) {
        FirebaseManager.getRefToUserName(collaboratorHashKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    collaboratorName.setText(dataSnapshot.getValue().toString());
                }catch (Exception e){
                    collaboratorName.setText("Error fetching name");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initVariable() {
        inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        listOfItems = findViewById(R.id.list_of_items);
        chatButton = findViewById(R.id.button_chat);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChat(POid);
            }
        });
        addItem = findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProspectOrderDetails.this,OpenOrderAddItem.class);
                intent.putExtra("POid",POid);
                intent.putExtra("smart",smart);
                startActivity(intent);
            }
        });
    }

    private void openChat(String POid) {
        Intent intent = new Intent(ProspectOrderDetails.this,POChatActivity.class);
        intent.putExtra("POid",POid);
        startActivity(intent);
    }

    /*private void openBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }*/
}
