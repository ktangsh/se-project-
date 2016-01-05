/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

import java.util.HashMap;

/**
 *
 * @author jennifer
 */
public class SmartphoneOveruse {

    private String overuseIndex;

    private HashMap<String, Double> metrics;

    /**
     * SmartphoneOveruse Constructor that takes in overuseIndex and HashMap of
     * metrics
     *
     * @param overuseIndex
     * @param metrics
     */
    public SmartphoneOveruse(String overuseIndex, HashMap<String, Double> metrics) {
        this.overuseIndex = overuseIndex;
        this.metrics = metrics;
    }

    /**
     * Returns overuseIndex
     * 
     * @return String
     */
    public String getOveruseIndex() {
        return overuseIndex;
    }

    /**
     * Sets overuseIndex
     *
     * @param overuseIndex
     */
    public void setOveruseIndex(String overuseIndex) {
        this.overuseIndex = overuseIndex;
    }

    /**
     * Returns metrics
     *
     * @return HashMap<String, Double>
     */
    public HashMap<String, Double> getMetrics() {
        return metrics;
    }

    /**
     * Sets metrics
     *
     * @param metrics
     */
    public void setMetrics(HashMap<String, Double> metrics) {
        this.metrics = metrics;
    }

}
