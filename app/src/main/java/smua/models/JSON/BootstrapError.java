/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 *
 * @author r128
 */

public class BootstrapError {
    
    private TreeMap<String,Integer> numRecordLoaded;
    private ArrayList<ErrorMsg> error;

    
    /**
     * BootstrapError constructor that takes in status, Linked hashmap of number of records loaded and Arraylist of errors
     * @param numRecordLoaded Number of records loaded
     * @param error ArrayList of ErrorMsgs
     */
    public BootstrapError(TreeMap<String,Integer> numRecordLoaded, ArrayList<ErrorMsg> error) {
        this.numRecordLoaded = numRecordLoaded;
        this.error = error;
    }
    
  
    /**
     * Return the number of records loaded in LinkedHashMap
     * @return LinkedHashMap<String,Integer>
     */
    public TreeMap<String,Integer> getNumRecordLoaded() {
        return numRecordLoaded;
    }
    
    /**
     * Set the number of records loaded
     * @param numRecordLoaded 
     */
    public void setNumRecordLoaded(TreeMap<String,Integer> numRecordLoaded) {
        this.numRecordLoaded = numRecordLoaded;
    }
    
    
    /**
     * Returns the arrayList of errors
     * @return ArrayList<ErrorMsg>
     */
    public ArrayList<ErrorMsg> getErrors() {
        return error;
    }
    
    /**
     * Takes in an Arraylist of errors and Set the errors
     * @param error 
     */
    public void setErrors(ArrayList<ErrorMsg> error) {
        this.error = error;
    }
    
    
}
