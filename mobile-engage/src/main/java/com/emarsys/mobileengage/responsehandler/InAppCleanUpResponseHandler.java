package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class InAppCleanUpResponseHandler extends AbstractResponseHandler {

    private static final String OLD_MESSAGES = "old_messages";

    private final Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    private final Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;

    public InAppCleanUpResponseHandler(
            Repository<DisplayedIam, SqlSpecification> displayedIamRepository,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository) {
        Assert.notNull(displayedIamRepository, "DisplayedIamRepository must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickedRepository must not be null!");
        this.displayedIamRepository = displayedIamRepository;
        this.buttonClickedRepository = buttonClickedRepository;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        boolean shouldHandle = false;

        JSONObject json = responseModel.getParsedBody();
        if (json != null && json.has(OLD_MESSAGES) && isCustomEventResponseModel(responseModel)) {
            JSONArray array = json.optJSONArray(OLD_MESSAGES);
            shouldHandle = array.length() > 0;
        }
        return shouldHandle;
    }

    private boolean isCustomEventResponseModel(ResponseModel responseModel) {
        return RequestUrlUtils.isCustomEvent_V3(responseModel.getRequestModel().getUrl().toString());
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject json = responseModel.getParsedBody();
        JSONArray oldMessages = json.optJSONArray(OLD_MESSAGES);
        String[] ids = new String[oldMessages.length()];
        for (int i = 0; i < oldMessages.length(); i++) {
            ids[i] = oldMessages.optString(i);
        }
        displayedIamRepository.remove(new FilterByCampaignId(ids));
        buttonClickedRepository.remove(new FilterByCampaignId(ids));
    }
}
