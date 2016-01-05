/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

/**
 *
 * @author shtang.2014
 */
public class UsageHeatmap {

    private String semanticPlace;
    private int numberOfpeopleUsingPhone;
    private int crowdDensity;

    /**
     * UsageHeatmap constructor that takes in semanticPlace,
     * numberOfpeopleUsingPhone and crowdDensity
     *
     * @param semanticPlace Location name; e.g SISSR2-4
     * @param numberOfpeopleUsingPhone Count of people using phone
     * @param crowdDensity Crowd Density Value
     */
    public UsageHeatmap(String semanticPlace, int numberOfpeopleUsingPhone, int crowdDensity) {
        this.semanticPlace = semanticPlace;
        this.numberOfpeopleUsingPhone = numberOfpeopleUsingPhone;
        this.crowdDensity = crowdDensity;
    }

    /**
     * Returns semanticPlace
     *
     * @return String name of semantic place
     */
    public String getSemanticPlace() {
        return semanticPlace;
    }

    /**
     * 
     * @param semanticPlace 
     */
    public void setSemanticPlace(String semanticPlace) {
        this.semanticPlace = semanticPlace;
    }

    /**
     * Returns numberOfPeopleUsingPhone
     *
     * @return count of people using phone
     */
    public int getNumberOfpeopleUsingPhone() {
        return numberOfpeopleUsingPhone;
    }

    /**
     * Sets numberOfPeopleUsingPhone
     *
     * @param numberOfpeopleUsingPhone
     */
    public void setNumberOfpeopleUsingPhone(int numberOfpeopleUsingPhone) {
        this.numberOfpeopleUsingPhone = numberOfpeopleUsingPhone;
    }

    /**
     * Returns crowdDensity
     *
     * @return Crowd Density Value
     */
    public int getCrowdDensity() {
        return crowdDensity;
    }

    /**
     * Sets crowdDensity
     *
     * @param crowdDensity
     */
    public void setCrowdDensity(int crowdDensity) {
        this.crowdDensity = crowdDensity;
    }
}
