package com.sonicworkflow.serveraddlas.custom_adapters;

/**
 * Created by umarbradshaw on 12/5/15.
 */
public class ShowListModel {

    String showName = null;
    String showTime = null;
    String contributor_name = null;
    String showID = null;


    public ShowListModel(String showName, String showTime, String contributor_name, String showID){

        this.showName = showName;
        this.showTime = showTime;
        this.contributor_name = contributor_name;
        this.showID = showID;

    }

    public String getShowName() {
        return showName;
    }

    public String getShowTime() {
        return showTime;
    }

    public String getContributor_name() {
        return contributor_name;
    }

    public String getShowID() {
        return showID;
    }
}

