package com.gop.dynamicdatasource.enums;

/**
 * Created by wenzhiwei on 17/1/16.
 */
public enum DatasourceType {

    READ("READ"),
    WRITE("WRITE");

    private String type;

    DatasourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
