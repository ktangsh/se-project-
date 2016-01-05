/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.PK;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author r128
 */
@Embeddable
public class LocationPK implements Serializable {
    
    private String macAddress;
    
    private Date timestamp;
    
    /**
     * Default Constructor
     */
    public LocationPK() {
    }
    
    /**
     * Creates a new LocationPK Object
     * @param macAddress
     * @param timestamp 
     */
    public LocationPK(String macAddress, Date timestamp) {
        this.macAddress = macAddress;
        this.timestamp = timestamp;
    }

    /**
     * returns macAddress
     * @return String
     */
    public String getMacAddress() {
        return macAddress;
    }
    
    /**
     * sets macAddress
     * @param macAddress 
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    /**
     * returns timestamp
     * @return Date
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * sets timestamp
     * @param timestamp 
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    
}
