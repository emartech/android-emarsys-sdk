package com.emarsys.predict.api.model;

import java.util.List;
import java.util.Map;

public interface Logic {

    String getLogicName();

    Map<String, String> getData();

    List<String> getVariants();
}