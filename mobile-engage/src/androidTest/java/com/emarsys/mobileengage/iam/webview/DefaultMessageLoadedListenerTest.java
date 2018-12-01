package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Application;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.provider.Gettable;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.filters.SdkSuppress;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class DefaultMessageLoadedListenerTest {

    public static final long TIMESTAMP_START = 800L;
    public static final long TIMESTAMP_END = 1000L;
    public static final String REQUEST_ID = "originalRequestId";

    static {
        mock(Activity.class);
        mock(FragmentManager.class);
        mock(Fragment.class);
        mock(IamDialog.class);
        mock(Application.class);
    }

    private FragmentManager fragmentManager;
    private DefaultMessageLoadedListener listener;
    private IamDialog dialog;
    private Repository<Map<String, Object>, SqlSpecification> logRepositoryMock;
    private TimestampProvider timestampProvider;
    private ResponseModel responseModel;
    private Gettable<Activity> currentActivityProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        currentActivityProvider = mock(Gettable.class);
        AppCompatActivity currentActivity = mock(AppCompatActivity.class);
        fragmentManager = mock(FragmentManager.class);
        when(currentActivity.getSupportFragmentManager()).thenReturn(fragmentManager);
        when(currentActivityProvider.get()).thenReturn(currentActivity);
        dialog = mock(IamDialog.class);

        logRepositoryMock = mock(Repository.class);
        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP_END);

        responseModel = mock(ResponseModel.class, Mockito.RETURNS_DEEP_STUBS);
        when(responseModel.getTimestamp()).thenReturn(TIMESTAMP_START);
        when(responseModel.getRequestModel().getId()).thenReturn(REQUEST_ID);

        listener = new DefaultMessageLoadedListener(
                dialog,
                logRepositoryMock,
                responseModel,
                timestampProvider,
                currentActivityProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_iamDialog_shouldNotBeNull() {
        new DefaultMessageLoadedListener(null, logRepositoryMock, responseModel, timestampProvider, currentActivityProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepository_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, null, responseModel, timestampProvider, currentActivityProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_responseModel_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, logRepositoryMock, null, timestampProvider, currentActivityProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, logRepositoryMock, responseModel, null, currentActivityProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_currentActivityProvider_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, logRepositoryMock, responseModel, timestampProvider, null);
    }

    @Test
    public void testOnMessageLoaded_shouldDisplayTheDialog() {
        listener.onMessageLoaded();

        verify(dialog).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldNotShowDialog_whenThereIsNoAvailableActivity() throws Exception {
        when(currentActivityProvider.get()).thenReturn(null);

        listener.onMessageLoaded();

        verify(dialog, times(0)).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldNotShowDialog_whenThereIsAlreadyAFragmentWithSameTag() {
        Fragment fragment = mock(Fragment.class);
        when(fragmentManager.findFragmentByTag(IamDialog.TAG)).thenReturn(fragment);

        listener.onMessageLoaded();

        verify(dialog, times(0)).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldLogLoadingTime() {
        listener.onMessageLoaded();

        Map<String, Object> loadingTimeMetric = new HashMap<>();
        loadingTimeMetric.put("loading_time", 200L);
        loadingTimeMetric.put("id", REQUEST_ID);

        verify(logRepositoryMock).add(loadingTimeMetric);
    }

}