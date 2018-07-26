package com.emarsys.core.connection;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.testUtil.ConnectionTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ConnectionWatchDogTest {

    private Context context;
    private Handler mockHandler;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        mockHandler = mock(Handler.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextIsNull() {
        new ConnectionWatchDog(null, mockHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlerMustNotBeNull() {
        new ConnectionWatchDog(context, null);
    }

    @Test
    public void testConstructor_setContextSuccessfully() {
        ConnectionWatchDog watchDog = new ConnectionWatchDog(context, mockHandler);
        assertNotNull(watchDog.context);
    }

    @Test
    public void testConstructor_shouldKeepReferenceToApplicationContext() {
        ConnectionWatchDog watchDog = new ConnectionWatchDog(InstrumentationRegistry.getContext(), mockHandler);
        assertTrue(watchDog.context instanceof Application);
    }

    @Test
    public void testConstructor_connectivityManagerShouldBeSet() {
        ConnectionWatchDog watchDog = new ConnectionWatchDog(context, mockHandler);
        assertNotNull(watchDog.connectivityManager);
    }

    @Test
    public void testRegisterReceiver_shouldCallRegisterReceiver() {
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(true, ConnectivityManager.TYPE_WIFI);
        Context appContextMock = contextMock.getApplicationContext();

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(contextMock, mockHandler);
        ConnectionChangeListener connectionChangeListener = mock(ConnectionChangeListener.class);
        connectionWatchDog.registerReceiver(connectionChangeListener);

        ArgumentCaptor<IntentFilter> captor = ArgumentCaptor.forClass(IntentFilter.class);

        verify(appContextMock).registerReceiver(any(ConnectionWatchDog.ConnectivityChangeReceiver.class), captor.capture());
        assertTrue(captor.getValue().hasAction(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterReceiver_shouldThrowException_whenReceiverRegistrationHasAlreadyCalled() {
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(true, ConnectivityManager.TYPE_WIFI);
        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(contextMock, mockHandler);
        ConnectionChangeListener connectionChangeListener = mock(ConnectionChangeListener.class);

        connectionWatchDog.registerReceiver(connectionChangeListener);
        connectionWatchDog.registerReceiver(connectionChangeListener);
    }

    @Test
    public void testIsConnected_Online() {
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(true, ConnectivityManager.TYPE_MOBILE_DUN);
        ConnectionWatchDog watchDog = new ConnectionWatchDog(contextMock, mockHandler);
        assertTrue(watchDog.isConnected());
    }

    @Test
    public void testIsConnected_Offline() {
        Context contextMock = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(false, -1);
        ConnectionWatchDog watchDog = new ConnectionWatchDog(contextMock, mockHandler);
        assertFalse(watchDog.isConnected());
    }

}