/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.util.Entities;

import java.util.Date;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author shtang.2014
 */
public class TimeObject implements Comparable<TimeObject> {

    private Date startTime;

    private Date endTime;

    private List<String> usersList;

    //obsolete constructor 
    public TimeObject(Date startTime, Date endTime, List<String> usersList) {
        this(startTime, endTime);
        this.usersList = usersList;
    }

    /**
     * TimeObject constructor
     *
     * @param startTime Start Date
     * @param endTime End Date
     */
    public TimeObject(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Get start date
     *
     * @return Date object describing the beginning of this TimeObject
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Set the beginning Date of this TimeObject
     *
     * @param startTime
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get end date
     *
     * @return Date object describing the ending of this TimeObject
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Set the ending Date of this TimeObject
     *
     * @param endTime
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<String> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<String> usersList) {
        this.usersList = usersList;
    }

    /**
     * Merges overlapping time objects.
     *
     * @param timeObj
     * @return true if merge successfully, else false
     */
    public boolean merge(TimeObject timeObj) {
        boolean merge = false;
        Date startTime = timeObj.getStartTime();
        Date endTime = timeObj.getEndTime();

        //if both start and end within the interval
        if (this.startTime.after(endTime) || this.endTime.before(startTime)) {
            return false;
        }

        //if start time is before beginning of interval and end time is in between intervalor 
        //More for social activeness
        if (this.startTime.after(startTime)) {
            this.startTime = startTime;
            merge = true;
        }

        //if start time is between interval and end time is after end of interval
        //More for social activeness
        if (this.endTime.before(endTime)) {
            this.endTime = endTime;
            merge = true;
        }

        if (this.startTime.before(startTime) || this.startTime.equals(startTime)) {
            if (this.endTime.before(endTime)) {
                this.endTime = endTime;
            }
            merge = true;
        }

        if (startTime.equals(this.endTime)) {
            this.endTime = endTime;
            merge = true;
        }

        if (endTime.equals(this.startTime)) {
            this.startTime = startTime;
            merge = true;
        }
        return merge;
    }

    /**
     * Checks if there is an overlap between the current TimeObject and the
     * other TimeObject
     * @param otherTimeObject
     * @return boolean
     */
    public boolean overlap(TimeObject otherTimeObject) {
        Date otherStartTime = otherTimeObject.getStartTime();
        Date otherEndTime = otherTimeObject.getEndTime();

        if (otherEndTime.before(this.startTime) || otherStartTime.after(this.endTime)) {
            return false;
        }

        if (otherEndTime.equals(this.startTime) || otherStartTime.equals(this.endTime)) {
            return false;
        }

        return true;

    }

    /**
     * Compares one TimeObject to another TimeObject
     *
     * @param o
     * @return 1 if TimeObject is after the TimeObject it is being compared to,
     * -1 if TimeObjet is before the TimeObeject it is being compared to.
     */
    @Override
    public int compareTo(TimeObject o) {
        if (startTime.after(o.startTime)) {
            return 1;
        } else if (startTime.before(o.startTime)) {
            return -1;
        }
        
        if(endTime.after(o.endTime)) {
            return -1;
        }else if(endTime.before(o.endTime)) {
            return 1;
        }else {
            return 0;
        }
    }
}
