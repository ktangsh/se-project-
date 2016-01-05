/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.Entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import smua.models.PK.LocationPK;

/**
 *
 * @author r128
 */

@Entity
@Table
public class Location implements Serializable{
    
    @EmbeddedId
    private LocationPK locationPK;
    
    @OneToOne
    @JoinColumn(name="locationID",referencedColumnName="locationID")
    private LocationLookup locationLookup;

    /**
     * Default constructor
     */
    public Location() {
    }

    /**
     * Construct Location with locationPK and locationLookup
     * @param locationPK
     * @param locationLookup 
     */
    public Location(LocationPK locationPK, LocationLookup locationLookup) {
        this.locationPK = locationPK;
        this.locationLookup = locationLookup;
    }

    /**
     * Return locationPK
     * @return LocationPK
     */
    public LocationPK getLocationPK() {
        return locationPK;
    }

    /**
     * Set LocationPK 
     * @param locationPK 
     */
    public void setLocationPK(LocationPK locationPK) {
        this.locationPK = locationPK;
    }
    
    /**
     * Return LocationLookup
     * @return LocationLookup
     */
    public LocationLookup getLocationLookup() {
        return locationLookup;
    }

    /**
     * Set LocationLookup
     * @param locationLookup 
     */
    public void setLocationLookup(LocationLookup locationLookup) {
        this.locationLookup = locationLookup;
    }
    
}
