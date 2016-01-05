/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.Entities;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import smua.models.PK.AppPK;

/**
 *
 * @author r128
 */
@Entity
@Table
public class App implements Serializable{
    
    @EmbeddedId
    private AppPK appPK;
    
    //foreign key to AppLookup
    @OneToOne
    @JoinColumn(name="appID",referencedColumnName="appID")
    private AppLookup appLookup;

    
    /**
     * Default constructor
     */
    public App() {
    }
    
    /**
     * App constructs a new App with appPK and appLookup
     * @param appPK
     * @param appLookup 
     */
    public App(AppPK appPK, AppLookup appLookup) {
        this.appPK=appPK;
        this.appLookup = appLookup;
    }
    
    /**
     * Returns AppPK entity
     * @return AppPK
     */
    public AppPK getAppPK() {
        return appPK;
    }
    
    /**
     * Set the appPK for the App
     * @param appPK 
     */
    public void setAppPK(AppPK appPK) {
        this.appPK = appPK;
    }

    /**
     * Return AppLookup entity
     * @return AppLookup
     */
    public AppLookup getAppLookup() {
        return appLookup;
    }

    /**
     * Set the appLookup for the App
     * @param appLookup 
     */
    public void setAppLookup(AppLookup appLookup) {
        this.appLookup = appLookup;
    }
    
    
}
