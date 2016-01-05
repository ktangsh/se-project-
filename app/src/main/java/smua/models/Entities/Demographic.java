/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.Entities;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import smua.models.PK.AppPK;

/**
 *
 * @author r128
 */
@Entity
@Table
public class Demographic {

    @Id
    private String macAddress;

    private String name;

    private String password;

    private String email;

    private char gender;
    
    private String cca;
    
    /**
     * Default constructor
     */
    public Demographic() {
        
    }

    /**
     * Construct Demographic with email and password
     * @param email
     * @param password 
     */
    public Demographic(String email, String password) {
        this.password = password;
        this.email = email;
    }

    /**
     * Construct Demographic with macaddress, name, password, email and gender
     * @param macAddress
     * @param name
     * @param password
     * @param email
     * @param gender 
     */
    public Demographic(String macAddress, String name, String password, String email, char gender, String cca) {
        this(email,password);
        this.macAddress = macAddress;
        this.name = name;
        this.gender = gender;
        this.cca = cca;
    }

    /**
     * Return macAddress
     * @return String
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Set the macAddress for Demographics
     * @param macAddress 
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Returns name from Demographic
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name for Demographic
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return passwords from Demographic
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password for Demographic
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Return email from Demographic
     * @return String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email for Demographic
     * @param email 
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns gender from Demographic
     * @return char
     */
    public char getGender() {
        return gender;
    }

    /**
     * Set gender for Demographic
     * @param gender 
     */
    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getCca() {
        return cca;
    }

    public void setCca(String cca) {
        this.cca = cca;
    }
    
}
