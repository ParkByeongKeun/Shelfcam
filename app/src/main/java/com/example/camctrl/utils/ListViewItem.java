package com.example.camctrl.utils;

public class ListViewItem {
    private String titleStr;
    private String titleSerial;

    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setTitleSerial(String serial) {
        titleSerial = serial;
    }

    public String getSerial() {
        return this.titleSerial;
    }
    public String getTitle() {
        return this.titleStr;
    }
}