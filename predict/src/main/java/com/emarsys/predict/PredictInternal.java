package com.emarsys.predict;

import com.emarsys.core.util.Assert;

public class PredictInternal {

    public void setTag(String tag){
        Assert.notNull(tag, "Tag must not be null!");
    }

}
