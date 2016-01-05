/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

import java.util.Comparator;

/**
 *
 * @author jennifer
 */
public class TopKStudent implements Comparable<TopKStudent>{

    private int rank;

    private String name;

    private String macAddress;

    private int duration;

    /**
     * TopKStudent constructor that takes in rank, name, macAddress and duration
     *
     * @param rank
     * @param name
     * @param macAddress
     * @param duration
     */
    public TopKStudent(int rank, String name, String macAddress, int duration) {
        this.rank = rank;
        this.name = name;
        this.macAddress = macAddress;
        this.duration = duration;
    }

    /**
     * Returns rank
     *
     * @return int
     */
    public int getRank() {
        return rank;
    }

    /**
     * Sets rank
     *
     * @param rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Returns name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns macAddress
     *
     * @return String
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets macAddress
     *
     * @param macAddress
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Returns duration
     *
     * @return int
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets duration
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    @Override
    public int compareTo(TopKStudent topKStudent) {
        if(rank > topKStudent.getRank()) {
            return 1;
        }else if(rank < topKStudent.getRank()) {
            return -1;
        }else {
            return name.compareTo(topKStudent.getName());
        }
    }

}
