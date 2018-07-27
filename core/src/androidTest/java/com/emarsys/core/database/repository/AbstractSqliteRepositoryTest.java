package com.emarsys.core.database.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.CoreSQLiteDatabase;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.database.DatabaseContract;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.timestamp.TimestampProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_HEADERS;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_METHOD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_PAYLOAD;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_REQUEST_ID;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TIMESTAMP;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_TTL;
import static com.emarsys.core.database.DatabaseContract.REQUEST_COLUMN_NAME_URL;
import static com.emarsys.core.util.serialization.SerializationUtils.serializableToBlob;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractSqliteRepositoryTest {
    private final static String TABLE_NAME = "table";

    private AbstractSqliteRepository<Object> repository;
    private DbHelper dbHelperMock;
    private CoreSQLiteDatabase dbMock;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        dbMock = mock(CoreSQLiteDatabase.class);

        dbHelperMock = mock(DbHelper.class);
        when(dbHelperMock.getReadableCoreDatabase()).thenReturn(dbMock);
        when(dbHelperMock.getWritableCoreDatabase()).thenReturn(dbMock);

        repository = mock(AbstractSqliteRepository.class, Mockito.CALLS_REAL_METHODS);
        repository.tableName = TABLE_NAME;
        repository.dbHelper = dbHelperMock;

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_shouldNotAcceptNull() {
        repository.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemove_shouldNotAcceptNull() {
        repository.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuery_shouldNotAcceptNull() {
        repository.query(null);
    }

    @Test
    public void testAdd_shouldInsertIntoDb() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", "value");
        when(repository.contentValuesFromItem(any())).thenReturn(contentValues);

        Object input = new Object();

        repository.add(input);

        verify(repository).contentValuesFromItem(input);
        verify(dbMock).beginTransaction();
        verify(dbMock).insert(TABLE_NAME, null, contentValues);
        verify(dbMock).setTransactionSuccessful();
        verify(dbMock).endTransaction();
    }

    @Test
    public void testQuery_shouldReturnCorrectResult() {
        SqlSpecification specification = mock(SqlSpecification.class);
        when(specification.getSql()).thenReturn("sql statement");
        when(specification.getArgs()).thenReturn(new String[]{"a", "b", "c"});

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.isAfterLast()).thenReturn(false, false, false, true);

        when(dbMock.rawQuery(any(String.class), any(String[].class))).thenReturn(cursor);

        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();

        when(repository.itemFromCursor(cursor)).thenReturn(item1, item2, item3);

        List<Object> expected = Arrays.asList(item1, item2, item3);
        List<Object> result = repository.query(specification);

        verify(dbMock).rawQuery(specification.getSql(), specification.getArgs());
        assertEquals(expected, result);
    }

    @Test
    public void testQuery_shouldReturnCorrectResult_whenCursorIsEmpty() {
        SqlSpecification specification = mock(SqlSpecification.class);
        when(specification.getSql()).thenReturn("dummy");
        when(specification.getArgs()).thenReturn(new String[]{"value"});

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(false);

        when(dbMock.rawQuery(any(String.class), any(String[].class))).thenReturn(cursor);

        List<Object> expected = Collections.emptyList();
        List<Object> result = repository.query(specification);

        assertEquals(expected, result);
    }

    @Test
    public void testRemove_shouldDeleteSpecifiedRow() {
        SqlSpecification specification = mock(SqlSpecification.class);
        when(specification.getSql()).thenReturn("sql statement");
        when(specification.getArgs()).thenReturn(new String[]{"a", "b", "c"});

        repository.remove(specification);

        verify(dbMock).beginTransaction();
        verify(dbMock).delete(TABLE_NAME, specification.getSql(), specification.getArgs());
        verify(dbMock).setTransactionSuccessful();
        verify(dbMock).endTransaction();
    }

    @Test
    public void testIsEmpty_shouldReturnFalse_whenThereAreRows() {
        DbHelper helper = new CoreDbHelper(InstrumentationRegistry.getTargetContext());
        repository.tableName = DatabaseContract.REQUEST_TABLE_NAME;
        repository.dbHelper = helper;
        CoreSQLiteDatabase db = helper.getWritableCoreDatabase();
        TimestampProvider timestampProvider = new TimestampProvider();
        RequestIdProvider requestIdProvider = new RequestIdProvider();

        RequestModel model1 = new RequestModel.Builder(timestampProvider, requestIdProvider).url("https://google.com").build();
        RequestModel model2 = new RequestModel.Builder(timestampProvider, requestIdProvider).url("https://emarsys.com").build();
        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, contentValuesFrom(model1));
        db.insert(DatabaseContract.REQUEST_TABLE_NAME, null, contentValuesFrom(model2));

        assertFalse(repository.isEmpty());
    }

    @Test
    public void testIsEmpty_shouldReturnTrue_whenTableIsEmpty() {
        DbHelper helper = new CoreDbHelper(InstrumentationRegistry.getTargetContext());
        repository.dbHelper = helper;
        repository.tableName = DatabaseContract.REQUEST_TABLE_NAME;
        CoreSQLiteDatabase db = helper.getWritableCoreDatabase();

        db.execSQL(String.format("DELETE FROM %s;", DatabaseContract.REQUEST_TABLE_NAME));

        assertTrue(repository.isEmpty());
    }

    private ContentValues contentValuesFrom(RequestModel item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(REQUEST_COLUMN_NAME_REQUEST_ID, item.getId());
        contentValues.put(REQUEST_COLUMN_NAME_METHOD, item.getMethod().name());
        contentValues.put(REQUEST_COLUMN_NAME_URL, item.getUrl().toString());
        contentValues.put(REQUEST_COLUMN_NAME_HEADERS, serializableToBlob(item.getHeaders()));
        contentValues.put(REQUEST_COLUMN_NAME_PAYLOAD, serializableToBlob(item.getPayload()));
        contentValues.put(REQUEST_COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        contentValues.put(REQUEST_COLUMN_NAME_TTL, item.getTtl());
        return contentValues;
    }
}