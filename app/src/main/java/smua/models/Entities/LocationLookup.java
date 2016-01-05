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
public class LocationLookup {
    
    @Id
    private int locationID;
    
    private String semanticPlace;

    /**
     * Default constructor
     */
    public LocationLookup() {
    }

    /**
     * Construct LocationLookup with locationID and semanticPlace
     * @param locationID
     * @param semanticPlace 
     */
    public LocationLookup(int locationID, String semanticPlace) {
        this.locationID = locationID;
        this.semanticPlace = semanticPlace;
    }

    /**
     * Returns locationID
     * @return int
     */
    public int getLocationID() {
        return locationID;
    }

    /**
     * Set locationID 
     * @param locationID 
     */
    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    /**
     * Returns Semantic Place
     * @return String
     */
    public String getSemanticPlace() {
        return semanticPlace;
    }

    /**
     * Set the Semantic Place
     * @param semanticPlace 
     */
    public void setSemanticPlace(String semanticPlace) {
        this.semanticPlace = semanticPlace;
    }
    
    
}
