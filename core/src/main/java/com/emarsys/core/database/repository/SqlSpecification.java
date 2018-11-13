package com.emarsys.core.database.repository;

public interface SqlSpecification {

    boolean isDistinct();

    String[] getColumns();

    String getSelection();

    String[] getSelectionArgs();

    String getGroupBy();

    String getHaving();

    String getOrderBy();

    String getLimit();

}