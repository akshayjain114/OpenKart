package com.example.sbarai.openkart.Utils;

/**
 * Created by sbarai on 1/19/18.
 */

public class Constants {

    public class CollaborationItem {

        public static final int UNITTYPE_COUNT = 0;
        public static final int UNITTYPE_GRAM = 1;
        public static final int UNITTYPE_KILOGRAM = 2;
        public static final int UNITTYPE_MILLILITER = 3;
        public static final int UNITTYPE_LITRE = 4;

    }

    public class ProspectOrder{

        public static final int STATUS_ACTIVE = 0;
        public static final int STATUS_CANCELED = 1;
        public static final int STATUS_ORDERED = 2;
        public static final int STATUS_PARTIALLY_DELIVERED = 3;
        public static final int STATUS_DELIVERED = 4;

    }

    public class FirebaseManager {

        public static final String APP_DATA = "appData";
        public static final String OPEN_ORDERS = "openOrders";
        public static final String PROSPECT_ORDERS = "prospectOrders";
        public static final String GEO_FIRE = "geofire";
        public static final String USERS = "users";
        public static final String AUTOFILL = "autofill";
        public static final String CATALOGUE = "catalogue";
    }
}
