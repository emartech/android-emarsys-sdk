package com.emarsys.core.database

import android.net.Uri

object DatabaseContract {
    const val REQUEST_TABLE_NAME = "request"
    const val SHARD_TABLE_NAME = "shard"
    const val DISPLAYED_IAM_TABLE_NAME = "displayed_iam"
    const val BUTTON_CLICKED_TABLE_NAME = "button_clicked"
    const val HARDWARE_IDENTIFICATION_TABLE_NAME = "hardware_identification"
    const val REQUEST_COLUMN_NAME_REQUEST_ID = "request_id"
    const val REQUEST_COLUMN_NAME_METHOD = "method"
    const val REQUEST_COLUMN_NAME_URL = "url"
    const val REQUEST_COLUMN_NAME_HEADERS = "headers"
    const val REQUEST_COLUMN_NAME_PAYLOAD = "payload"
    const val REQUEST_COLUMN_NAME_TIMESTAMP = "timestamp"
    const val REQUEST_COLUMN_NAME_TTL = "ttl"
    const val SHARD_COLUMN_ID = "shard_id"
    const val SHARD_COLUMN_TYPE = "type"
    const val SHARD_COLUMN_DATA = "data"
    const val SHARD_COLUMN_TIMESTAMP = "timestamp"
    const val SHARD_COLUMN_TTL = "ttl"
    const val DISPLAYED_IAM_COLUMN_NAME_CAMPAIGN_ID = "campaign_id"
    const val DISPLAYED_IAM_COLUMN_NAME_TIMESTAMP = "timestamp"
    const val BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID = "campaign_id"
    const val BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID = "button_id"
    const val BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP = "timestamp"
    const val HARDWARE_IDENTIFICATION_COLUMN_NAME_HARDWARE_ID = "hardware_id"
    const val HARDWARE_IDENTIFICATION_COLUMN_NAME_ENCRYPTED_HARDWARE_ID = "encrypted_hardware_id"
    const val HARDWARE_IDENTIFICATION_COLUMN_NAME_SALT = "salt"
    const val HARDWARE_IDENTIFICATION_COLUMN_NAME_IV = "iv"

    fun getHardwareIdProviderUri(authority: String): Uri {
        return Uri.parse("content://$authority").buildUpon().appendPath(HARDWARE_IDENTIFICATION_TABLE_NAME).build()
    }

    val UPGRADE_TO_1 = arrayOf(
            "CREATE TABLE IF NOT EXISTS request (" +
                    "request_id TEXT," +
                    "method TEXT," +
                    "url TEXT," +
                    "headers BLOB," +
                    "payload BLOB," +
                    "timestamp INTEGER);"
    )
    val UPGRADE_TO_2 = arrayOf(
            "ALTER TABLE request ADD COLUMN ttl INTEGER DEFAULT " + Long.MAX_VALUE + ";"
    )
    private const val UPGRADE_TO_3_CREATE_TABLE_SHARD = "CREATE TABLE IF NOT EXISTS shard (" +
            "shard_id TEXT," +
            "type TEXT," +
            "data BLOB," +
            "timestamp INTEGER," +
            "ttl INTEGER);"
    private const val UPGRADE_TO_3_ADD_INDEX_TO_ID = "CREATE INDEX shard_id_index ON shard (shard_id);"
    private const val UPGRADE_TO_3_ADD_INDEX_TO_TYPE = "CREATE INDEX shard_type_index ON shard (type);"
    val UPGRADE_TO_3 = arrayOf(
            UPGRADE_TO_3_CREATE_TABLE_SHARD,
            UPGRADE_TO_3_ADD_INDEX_TO_ID,
            UPGRADE_TO_3_ADD_INDEX_TO_TYPE
    )
    private const val UPGRADE_TO_4_CREATE_TABLE_DISPLAYED_IAM = "CREATE TABLE IF NOT EXISTS displayed_iam (" +
            "campaign_id TEXT," +
            "timestamp INTEGER);"
    private const val UPGRADE_TO_4_CREATE_TABLE_BUTTON_CLICKED = "CREATE TABLE IF NOT EXISTS button_clicked (" +
            "campaign_id TEXT," +
            "button_id TEXT," +
            "timestamp INTEGER);"
    val UPGRADE_TO_4 = arrayOf(
            UPGRADE_TO_4_CREATE_TABLE_DISPLAYED_IAM,
            UPGRADE_TO_4_CREATE_TABLE_BUTTON_CLICKED
    )
    private const val UPGRADE_TO_5_CREATE_TABLE_HARDWARE_IDENTIFICATION = """CREATE TABLE IF NOT EXISTS hardware_identification (
                    hardware_id TEXT,
                    encrypted_hardware_id TEXT,
                    salt TEXT,
                    iv TEXT
                    );"""
    val UPGRADE_TO_5 = arrayOf(
            UPGRADE_TO_5_CREATE_TABLE_HARDWARE_IDENTIFICATION
    )

    @JvmField
    val MIGRATION = arrayOf(
            UPGRADE_TO_1,
            UPGRADE_TO_2,
            UPGRADE_TO_3,
            UPGRADE_TO_4,
            UPGRADE_TO_5
    )
}