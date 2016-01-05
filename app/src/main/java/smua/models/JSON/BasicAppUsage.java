/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import smua.models.Entities.App;

/**
 *
 * @author famous
 */
public class BasicAppUsage {

    private int count;
    private int percentage;
    List<BasicAppUsage> basicAppUsageList;
    String category;
    String sortCategory;
    private List<App> appList;
    private List<LinkedHashMap<String,Integer>> categoryListResult;
    /**
     * This method returns the Category list result
     * @return 
     */
    public List<LinkedHashMap<String, Integer>> getCategoryListResult() {
        return categoryListResult;
    }
    
    /**
     * This method sets the categoryListResult
     * @param categoryListResult 
     */
    public void setCategoryListResult(List<LinkedHashMap<String, Integer>> categoryListResult) {
        this.categoryListResult = categoryListResult;
    }
    
    
    /**
     * Default Constructor for BasicAppUsage
     */
    public BasicAppUsage(){}
    
    /**
     * This constructor creates a new BasicAppUsage object with the given parameters.
     * @param count
     * @param percentage
     * @param basicAppUsageList
     * @param category
     * @param sortCategory
     * @param appList
     * @param categoryListResult 
     */
    public BasicAppUsage(int count, int percentage, ArrayList<BasicAppUsage> basicAppUsageList, String category, String sortCategory, List<App> appList, ArrayList<LinkedHashMap<String, Integer>> categoryListResult) {
        this.count = count;
        this.percentage = percentage;
        this.basicAppUsageList = basicAppUsageList;
        this.category = category;
        this.sortCategory = sortCategory;
        this.appList = appList;
        this.categoryListResult = categoryListResult;
    }

    
    /**
     * Returns the count of users in the breakdown.
     * @return 
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Returns the percentage of users represented in the breakdown
     * @return 
     */
    public int getPercentage() {
        return percentage;
    }
    
    /**
     * Returns a List of BasicAppUsage objects
     * @return 
     */
    public List<BasicAppUsage> getBasicAppUsageList() {
        return basicAppUsageList;
    }
    
    /**
     * Sets the count of users in this BasicAppUsage object
     * @param count 
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    /**
     * Sets the percentage represented in this BasicAppUsage object
     * @param percentage 
     */
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
    
    /**
     * Sets the List of BasicAppUsage objects in this object.
     * @param basicAppUsageList 
     */
    public void setBasicAppUsageList(List<BasicAppUsage> basicAppUsageList) {
        this.basicAppUsageList = basicAppUsageList;
    }
    
    /**
     * Sets the category name of the breakdown this BasicAppUsage object represents
     * @param category 
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Sets the category values e.g year:2015,2014 etc; of the breakdown this BasicAppUsage object
     * @param sortCategory 
     */
    public void setSortCategory(String sortCategory) {
        this.sortCategory = sortCategory;
    }
    
    /**
     * Sets the list of app usage represented in this BasicAppUsage object.
     * @param appList 
     */
    public void setAppList(List<App> appList) {
        this.appList = appList;
    }
    
    /**
     * Returns the category.
     * @return 
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Returns the sortCategory.
     * @return 
     */
    public String getSortCategory() {
        return sortCategory;
    }
    
    /**
     * Returns the list of app usage represented in this BasicAppUsage object
     * @return 
     */
    public List<App> getAppList() {
        return appList;
    }
    
    /**
     * Returns a String representation of this object in this format:
     * [sortCategory]:[count](percentage%}
     * @return 
     */
    public String toString() {
        if(sortCategory.equals("F")) {
            sortCategory = "Female";
        }else if(sortCategory.equals("M")) {
            sortCategory = "Male";
        }
        return sortCategory + ": " + count + " (" + percentage + "%)"; 
    }
    
    
    
}
