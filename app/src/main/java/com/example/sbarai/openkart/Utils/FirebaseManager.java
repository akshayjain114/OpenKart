package com.example.sbarai.openkart.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sbarai on 1/26/18.
 */

public class FirebaseManager {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static DatabaseReference getRootRef(){
        return database.getReference();
    }

    public static DatabaseReference getRefToAppData(){
        DatabaseReference ref = getRootRef();
        return ref.child(Constants.FirebaseManager.APP_DATA);
    }

    public static DatabaseReference getRefToOpenOrders(){
        DatabaseReference ref = getRefToAppData();
        return ref.child(Constants.FirebaseManager.OPEN_ORDERS);
    }

    public static DatabaseReference getRefToProspectOrders(){
        DatabaseReference ref = getRefToOpenOrders();
        return ref.child(Constants.FirebaseManager.PROSPECT_ORDERS);
    }

    public static DatabaseReference getRefToSpecificProspectOrder(String key){
        DatabaseReference ref = getRefToProspectOrders();
        return ref.child(key);
    }

    public static DatabaseReference getRefToGeofireForProspectOrders(){
        DatabaseReference ref = getRefToOpenOrders();
        return ref.child(Constants.FirebaseManager.GEO_FIRE);
    }

    //User details
    public static DatabaseReference getRefToUsersNode(){
        DatabaseReference ref = getRootRef().child(Constants.FirebaseManager.USERS);
        return ref;
    }

    public static DatabaseReference getRefToSpecificUser(String key){
        DatabaseReference ref = getRefToUsersNode();
        return ref.child(key);
    }

    public static DatabaseReference getRefToUserName(String key){
        DatabaseReference ref = getRefToSpecificUser(key);
        return ref.child("name");
    }

    public static DatabaseReference getRefToAutofill(){
        DatabaseReference ref = getRootRef();
        return ref.child(Constants.FirebaseManager.AUTOFILL);
    }

    public static DatabaseReference getRefToCatalogue(){
        DatabaseReference ref = getRootRef();
        return ref.child(Constants.FirebaseManager.CATALOGUE);
    }
}
