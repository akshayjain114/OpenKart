package com.example.sbarai.openkart.Adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sbarai.openkart.Models.CollaborationItem;
import com.example.sbarai.openkart.Models.Collaborator;
import com.example.sbarai.openkart.Models.ProspectOrder;
import com.example.sbarai.openkart.OpenOrders;
import com.example.sbarai.openkart.ProspectOrderDetails;
import com.example.sbarai.openkart.R;
import com.example.sbarai.openkart.Utils.FirebaseManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.DAYS;


/**
 * Created by sbarai on 2/8/18.
 */

public class RvProspectOrderAdapter extends RecyclerView.Adapter<RvProspectOrderAdapter.MyViewHolder> {


    private LayoutInflater inflater;
    private List<String> data = Collections.EMPTY_LIST;
    private static Context context;
    private View noDataFound;

    public RvProspectOrderAdapter(Context context, List<String> data) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        if (data.size() == 0)
            this.data = new ArrayList<>();
        else
            this.data = data;
    }

    public void setNoDataFound(View view) {
        noDataFound = view;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_rv_prospect_order, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.storeTitle.setText("Loading...");
        setContentToHolder(holder, position, 1);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOrderDetails(data.get(position));
            }
        });
    }

    private void goToOrderDetails(String id) {
        Intent intent = new Intent(context, ProspectOrderDetails.class);
        intent.putExtra("POid", id);
        context.startActivity(intent);
    }

    public void setContentToHolder(final MyViewHolder holder, final int position, final int attempt) {
        String key = data.get(position);
        DatabaseReference ref = FirebaseManager.getRefToSpecificProspectOrder(key);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProspectOrder order = dataSnapshot.getValue(ProspectOrder.class);
                try {
                    holder.storeTitle.setText(order.getDesiredStore());
                    setDateToView(order.getOrderDate(),holder.orderDate);
                    setAmounts(holder.targetValue, holder.amountReached, holder.remainingAmount, order);
                    holder.distance.setText(getDistanceFromLocation(order.getLocLat(), order.getLocLon()) + " Miles");
                } catch (Exception e) {
                    Log.d("TAGG", "Exception generated");
                    e.printStackTrace();
                    if (order == null) {
                        Log.d("TAGG", "order is null");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (attempt < 3) {
                    setContentToHolder(holder, position, attempt + 1);
                } else {
                    data.remove(position);
                    notifyDataSetChanged();
                }
            }
        });
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

    private void setAmounts(TextView targetValue, TextView amountReached, TextView remainingAmount, ProspectOrder order) {
        targetValue.setText(String.valueOf(order.getTargetTotal()));
        float amtReached = 0;
        if (order.getCollaborators() != null) {
            for (String collaboratorHashKey : order.getCollaborators().keySet()) {
                Collaborator currentCollaborator = order.getCollaborators().get(collaboratorHashKey);
                if (currentCollaborator.getCollaborationItems() != null){
                    for (String itemHashKey: currentCollaborator.getCollaborationItems().keySet()){
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
    }

    public void dataSetChanged(){
        if (data.size() == 0){
            noDataFound.setVisibility(View.VISIBLE);
            Log.d("TAGG","making visible");
        }else{
            noDataFound.setVisibility(View.GONE);
            Log.d("TAGG","making gone");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void insertIntoData(String key) {
        data.add(key);
        dataSetChanged();
        notifyItemInserted(data.indexOf(key));
//        Log.d("TAGG","Notifying dataset changed");
    }

    public void removeFromData(String key) {
        int index = data.indexOf(key);
        data.remove(index);
        notifyItemRemoved(index);
//        dataSetChanged();
//        Log.d("TAGG","removed from data, new size" + data.size());
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView storeTitle;
        TextView initiatedBy;
        TextView orderDate;
        TextView targetValue;
        TextView amountReached;
        TextView remainingAmount;
        TextView distance;
        View rootView;
        public MyViewHolder(View itemView) {
            super(itemView);
            storeTitle = itemView.findViewById(R.id.storeName);
            rootView = itemView.findViewById(R.id.root);
            initiatedBy = itemView.findViewById(R.id.initiated_by);
            orderDate = itemView.findViewById(R.id.order_date);
            targetValue = itemView.findViewById(R.id.tvalue);
            amountReached = itemView.findViewById(R.id.avalue);
            remainingAmount = itemView.findViewById(R.id.rvalue);
            distance = itemView.findViewById(R.id.milesText);
        }
    }
}
