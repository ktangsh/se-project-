/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.Entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author r128
 */
@Entity
@Table
public class AppLookup {
    
    @Id //primary key and therefore foreign key to App
    private int appID;
    
    private String appName;

    
    private String category;

    /**
     * Default constructor
     */
    public AppLookup() {
    }
    
    /**
     * Construct Applookup with appID, appName, category
     * @param appID
     * @param appName
     * @param category 
     */
    public AppLookup(int appID, String appName, String category) {
        this.appID = appID;
        this.appName = appName;
        this.category = category;
    }

    /**
     * Return appID
     * @return int
     */
    public int getAppID() {
        return appID;
    }

    /**
     * Set appID for the AppLookup
     * @param appID 
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }

    /**
     * Return the appName
     * @return String
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Set appName for Applookup
     * @param appName 
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Get category
     * @return String
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set the category for Applookup
     * @param category 
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
}
