package com.emarsys.core.database;

import android.net.Uri;

public final class DatabaseContract {

    private DatabaseContract() {
    }

    public static final String REQUEST_TABLE_NAME = "request";
    public static final String SHARD_TABLE_NAME = "shard";
    public static final String DISPLAYED_IAM_TABLE_NAME = "displayed_iam";
    public static final String BUTTON_CLICKED_TABLE_NAME = "button_clicked";
    public static final String HARDWARE_INFORMATION_TABLE_NAME = "hardware";

    public static final String REQUEST_COLUMN_NAME_REQUEST_ID = "request_id";
    public static final String REQUEST_COLUMN_NAME_METHOD = "method";
    public static final String REQUEST_COLUMN_NAME_URL = "url";
    public static final String REQUEST_COLUMN_NAME_HEADERS = "headers";
    public static final String REQUEST_COLUMN_NAME_PAYLOAD = "payload";
    public static final String REQUEST_COLUMN_NAME_TIMESTAMP = "timestamp";
    public static final String REQUEST_COLUMN_NAME_TTL = "ttl";

    public static final String SHARD_COLUMN_ID = "shard_id";
    public static final String SHARD_COLUMN_TYPE = "type";
    public static final String SHARD_COLUMN_DATA = "data";
    public static final String SHARD_COLUMN_TIMESTAMP = "timestamp";
    public static final String SHARD_COLUMN_TTL = "ttl";

    public static final String DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID = "campaign_id";
    public static final String DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP = "timestamp";

    public static final String BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID = "campaign_id";
    public static final String BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID = "button_id";
    public static final String BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP = "timestamp";

    public static final String HARDWARE_COLUMN_NAME_HARDWARE_ID = "hardware_id";


    public static Uri getHardwareIdProviderUri(String authority) {
        return Uri.parse("content://" + authority).buildUpon().appendPath(HARDWARE_INFORMATION_TABLE_NAME).appendPath(HARDWARE_COLUMN_NAME_HARDWARE_ID).build();
    }

    public static final String[] UPGRADE_TO_1 = {
            "CREATE TABLE IF NOT EXISTS request (" +
                    "request_id TEXT," +
                    "method TEXT," +
                    "url TEXT," +
                    "headers BLOB," +
                    "payload BLOB," +
                    "timestamp INTEGER);"
    };

    public static final String[] UPGRADE_TO_2 = {
            "ALTER TABLE request ADD COLUMN ttl INTEGER DEFAULT " + Long.MAX_VALUE + ";"
    };

    private static final String UPGRADE_TO_3_CREATE_TABLE_SHARD =
            "CREATE TABLE IF NOT EXISTS shard (" +
                    "shard_id TEXT," +
                    "type TEXT," +
                    "data BLOB," +
                    "timestamp INTEGER," +
                    "ttl INTEGER);";
    private static final String UPGRADE_TO_3_ADD_INDEX_TO_ID = "CREATE INDEX shard_id_index ON shard (shard_id);";
    private static final String UPGRADE_TO_3_ADD_INDEX_TO_TYPE = "CREATE INDEX shard_type_index ON shard (type);";


    public static final String[] UPGRADE_TO_3 = {
            UPGRADE_TO_3_CREATE_TABLE_SHARD,
            UPGRADE_TO_3_ADD_INDEX_TO_ID,
            UPGRADE_TO_3_ADD_INDEX_TO_TYPE
    };

    private static final String UPGRADE_TO_4_CREATE_TABLE_DISPLAYED_IAM =
            "CREATE TABLE IF NOT EXISTS displayed_iam (" +
                    "campaign_id TEXT," +
                    "timestamp INTEGER);";

    private static final String UPGRADE_TO_4_CREATE_TABLE_BUTTON_CLICKED =
            "CREATE TABLE IF NOT EXISTS button_clicked (" +
                    "campaign_id TEXT," +
                    "button_id TEXT," +
                    "timestamp INTEGER);";

    public static final String[] UPGRADE_TO_4 = {
            UPGRADE_TO_4_CREATE_TABLE_DISPLAYED_IAM,
            UPGRADE_TO_4_CREATE_TABLE_BUTTON_CLICKED
    };

    private static final String UPGRADE_TO_5_CREATE_TABLE_SHARED_HW_ID =
            "CREATE TABLE IF NOT EXISTS hardware (" +
                    "hardware_id TEXT);";

    public static final String[] UPGRADE_TO_5 = {
            UPGRADE_TO_5_CREATE_TABLE_SHARED_HW_ID
    };

    public static final String[][] MIGRATION = {
            UPGRADE_TO_1,
            UPGRADE_TO_2,
            UPGRADE_TO_3,
            UPGRADE_TO_4,
            UPGRADE_TO_5
    };
}
