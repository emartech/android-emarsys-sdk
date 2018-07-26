package com.emarsys.mobileengage.iam.model.buttonclicked;

public final class ButtonClickedContract {

    private ButtonClickedContract() {
    }

    public static final String TABLE_NAME = "button_clicked";
    public static final String COLUMN_NAME_CAMPAIGN_ID = "campaign_id";
    public static final String COLUMN_NAME_BUTTON_ID = "button_id";
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s TEXT," +
                    "%s TEXT," +
                    "%s INTEGER" +
                    ");",
            TABLE_NAME,
            COLUMN_NAME_CAMPAIGN_ID,
            COLUMN_NAME_BUTTON_ID,
            COLUMN_NAME_TIMESTAMP
    );

    public static final String SQL_CLEAR = String.format(
            "DELETE FROM %s;", TABLE_NAME
    );

    public static final String SQL_SELECT_ALL = String.format(
            "SELECT * FROM %s;", TABLE_NAME
    );

    public static final String SQL_SELECT_BY_EVENT_NAME = String.format(
            "SELECT * FROM %s WHERE event_name=?;", TABLE_NAME
    );

}

