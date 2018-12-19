package com.emarsys.core.util.log;

public enum CoreTopic implements LogTopic {
    /**
     Topic for logging requests and responses
     */
    NETWORKING("ems_networking"),

    /**
     Topic for logging connectivity changes
     */
    CONNECTIVITY("ems_connectivity"),

    /**
     Topic for logging offline functionality, like persisting and reading request objects from the offline queue
     */
    OFFLINE("ems_offline"),

    /**
     * Topic for logging events from the CoreSdkHandler
     */
    CONCURRENCY("ems_concurrency"),

    /**
     * Topic for logging from utility classes
     */
    UTIL("ems_util");

    private String topic;

    CoreTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        return topic;
    }
}
