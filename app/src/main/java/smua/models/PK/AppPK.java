/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.PK;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import smua.models.Entities.Demographic;

/**
 *
 * @author r128
 */

@Embeddable
public class AppPK implements Serializable{
//AppPK: Multivalued attribute becomes a separate relation with foreign key
//Applied to a persistent field or property of an entity class or mapped superclass to denote a composite primary key that is an embeddable class. 
//The embeddable class must be annotated as Embeddable.
//There must be only one EmbeddedId annotation and no Id annotation when the EmbeddedId annotation is used.
    private Date timestamp;
    
    @OneToOne
    @JoinColumn(name="macAddress",referencedColumnName="macAddress")
    private Demographic demographic;
    
    /**
     * Default Constructor
     */
    public AppPK() {
    }
    
    /**
     * Creates a new AppPK object
     * @param timestamp
     * @param demographic 
     */
    public AppPK(Date timestamp, Demographic demographic) {
        this.timestamp = timestamp;
        this.demographic = demographic;
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
    
    /**
     * returns demographic
     * @return Demographic
     */
    public Demographic getDemographic() {
        return demographic;
    }
    
    /**
     * sets demographic
     * @param demographic 
     */
    public void setDemographic(Demographic demographic) {
        this.demographic = demographic;
    }
    
    
}
