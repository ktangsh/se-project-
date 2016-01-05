/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.AppDAO;
import smua.models.DAO.LocationDAO;
import smua.models.Entities.App;
import smua.models.Entities.Location;
import smua.models.Entities.LocationLookup;
import smua.util.Entities.TimeObject;
import smua.models.PK.LocationPK;

/**
 * 
 * @author famous
 */
@Controller
public class SocialActivenessController {

    @Autowired
    BasicAppController basicAppController;

    @Autowired
    LocationDAO locationDAO;

    @Autowired
    AppDAO appDAO;

    /**
     * Get social activeness report for specified MAC Address
     *
     * @param startDate Start Date
     * @param endDate End Date
     * @param macAddress SHA-1 MAC Address
     * @return LinkedHashMap<String, Integer> of percentage of time spent alone,
     * in groups and total duration in seconds spent in SIS
     */
    public LinkedHashMap<String, Integer> getSocialActivenessReport(Date startDate, Date endDate, String macAddress) {

        LinkedHashMap<String, Integer> outputMap = new LinkedHashMap<>();
        List<Location> locationList = locationDAO.retrieveLocationByStartEndDate(startDate, endDate);
        ArrayList<Location> userLocationList = new ArrayList<Location>();
        Iterator<Location> locationIter = locationList.iterator();
        
        while (locationIter.hasNext()) {
            Location userLocation = locationIter.next();
            if (userLocation.getLocationPK().getMacAddress().equals(macAddress)) {
                userLocationList.add(userLocation);
            }
        }
        
        int totalTimeSpentInSIS = calculateTotalDuration(userLocationList);
        HashMap<String, HashMap<String, List<TimeObject>>> userSemanticTimeMap = new HashMap<String, HashMap<String, List<TimeObject>>>();
        
        //loops through the location list to divide each user
        //according to their semantic place and time
        for (int i = 0; i < locationList.size(); i++) {
            Location userLocation = locationList.get(i);

            String userMacAddress = userLocation.getLocationPK().getMacAddress();
            String semanticPlace = userLocation.getLocationLookup().getSemanticPlace();
            Date time = userLocation.getLocationPK().getTimestamp();

            if (i + 1 < locationList.size()) {
                Location nextUserLocation = locationList.get(i + 1);

                String nextUserMacAddress = nextUserLocation.getLocationPK().getMacAddress();
                String nextSemanticPlace = nextUserLocation.getLocationLookup().getSemanticPlace();
                Date nextTime = nextUserLocation.getLocationPK().getTimestamp();

                if (userMacAddress.equals(nextUserMacAddress)) {

                    Date actualEndTime = nextTime;

                    //If same user but difference mac address
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(time);
                    long timeDiff = (nextTime.getTime() - time.getTime()) / 1000;
                    cal.getTime();

                    if (timeDiff > 300) {
                        cal.add(GregorianCalendar.SECOND, 300);
                        actualEndTime = cal.getTime();
                    }
                    
                    HashMap<String, List<TimeObject>> semanticTimeMap = null;

                    if (userSemanticTimeMap.containsKey(userMacAddress)) {
                        semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                    } else {
                        semanticTimeMap = new HashMap<String, List<TimeObject>>();
                    }

                    if (semanticTimeMap.containsKey((semanticPlace))) {

                        List<TimeObject> timeObjectList = semanticTimeMap.get(semanticPlace);

                        TimeObject curTimeObject = new TimeObject(time, actualEndTime);

                        int counter = 0;
                        boolean merge = false;

                        while (!merge && counter < timeObjectList.size()) {
                            TimeObject timeObject = timeObjectList.get(counter);
                            merge = timeObject.merge(curTimeObject);
                            counter++;
                        }

                        if (!merge) {
                            timeObjectList.add(curTimeObject);
                        }

                    } else {
                        List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                        timeObjectList.add(new TimeObject(time, actualEndTime));
                        semanticTimeMap.put(semanticPlace, timeObjectList);
                    }

                    userSemanticTimeMap.put(userMacAddress, semanticTimeMap);

                } else {

                    HashMap<String, List<TimeObject>> semanticTimeMap = null;
                    Date actualEndTime = time;

                    if (userSemanticTimeMap.containsKey(userMacAddress)) {
                        semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                    } else {
                        semanticTimeMap = new HashMap<String, List<TimeObject>>();
                    }

                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(time);

                    cal.add(GregorianCalendar.SECOND, 300);
                    actualEndTime = cal.getTime();

                    if (semanticTimeMap.containsKey((semanticPlace))) {
                        List<TimeObject> timeObjectList = semanticTimeMap.get(semanticPlace);
                        TimeObject curTimeObject = new TimeObject(time, actualEndTime);
                        timeObjectList.add(curTimeObject);

                    } else {
                        List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                        timeObjectList.add(new TimeObject(time, actualEndTime));
                        semanticTimeMap.put(semanticPlace, timeObjectList);
                    }

                    userSemanticTimeMap.put(userMacAddress, semanticTimeMap);
                }
            } else {
                HashMap<String, List<TimeObject>> semanticTimeMap = null;
                Date actualEndTime = time;

                if (userSemanticTimeMap.containsKey(userMacAddress)) {
                    semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                } else {
                    semanticTimeMap = new HashMap<String, List<TimeObject>>();
                }

                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(time);

                cal.add(GregorianCalendar.SECOND, 300);
                actualEndTime = cal.getTime();

                if (semanticTimeMap.containsKey((semanticPlace))) {
                    List<TimeObject> timeObjectList = semanticTimeMap.get(semanticPlace);
                    TimeObject curTimeObject = new TimeObject(time, actualEndTime);
                    timeObjectList.add(curTimeObject);

                } else {
                    List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                    timeObjectList.add(new TimeObject(time, actualEndTime));
                    semanticTimeMap.put(semanticPlace, timeObjectList);
                }

                userSemanticTimeMap.put(userMacAddress, semanticTimeMap);
            }
        }

        System.out.println("SEMANTIC SIZE:" + userSemanticTimeMap.size());

        HashMap<String, List<TimeObject>> mySemanticTimeMap = userSemanticTimeMap.get(macAddress);

        if (mySemanticTimeMap == null) {
            outputMap.put("total-time-spent-in-sis", 0);
            outputMap.put("group-percent", 0);
            outputMap.put("solo-percent", 0);
            return outputMap;
        }

        userSemanticTimeMap.remove(macAddress);

        HashMap<String, List<TimeObject>> socialTimeLineMap = new HashMap<String, List<TimeObject>>();

        Iterator<String> mapIter = userSemanticTimeMap.keySet().iterator();
        
        
        while (mapIter.hasNext()) {
            String curMacAddress = mapIter.next();
            HashMap<String, List<TimeObject>> semanticTimeMap = userSemanticTimeMap.get(curMacAddress);

            System.out.println("MAC Address : " + curMacAddress);
            Iterator<String> semanticTimeMapIter = semanticTimeMap.keySet().iterator();

            while (semanticTimeMapIter.hasNext()) {
                String semanticPlace = semanticTimeMapIter.next();
                List<TimeObject> timeObjList = semanticTimeMap.get(semanticPlace);
                System.out.println(semanticPlace);
                for (TimeObject timeObject : timeObjList) {
                    System.out.println(timeObject.getStartTime());
                    System.out.println(timeObject.getEndTime());
                }
            }
        }
        //To reset the iterator to the start. Remove later on
        mapIter = userSemanticTimeMap.keySet().iterator();

        while (mapIter.hasNext()) {
            String curMacAddress = mapIter.next();
            HashMap<String, List<TimeObject>> semanticTimeMap = userSemanticTimeMap.get(curMacAddress);

            List<TimeObject> socialTimeLine = new ArrayList<TimeObject>();
            constructSocialTimeLine(socialTimeLine, mySemanticTimeMap, semanticTimeMap);
            Collections.sort(socialTimeLine);
            socialTimeLineMap.put(curMacAddress, socialTimeLine);
        }

        Iterator<String> socialIter = socialTimeLineMap.keySet().iterator();

        System.out.println("===SOCIAL CONSTRUCT===");

        while (socialIter.hasNext()) {
            String curMacAddress = socialIter.next();
            List<TimeObject> socialTime = socialTimeLineMap.get(curMacAddress);

            for (TimeObject timeObject : socialTime) {
                System.out.println("USER : " + curMacAddress);
                System.out.println(timeObject.getStartTime());
                System.out.println(timeObject.getEndTime());
            }
        }

        List<TimeObject> socialTimeList = new ArrayList<TimeObject>();
        HashMap<String, List<TimeObject>> revisedSocialTimeLineMap = new HashMap<String, List<TimeObject>>();
        socialIter = socialTimeLineMap.keySet().iterator();

        //TO merge all dates having the same end time and same next start time together
        while (socialIter.hasNext()) {
            String curMacAddress = socialIter.next();
            List<TimeObject> socialTime = socialTimeLineMap.get(curMacAddress);
            

            for (int i = 0; i < socialTime.size(); i++) {
                TimeObject curTimeObject = socialTime.get(i);

                Date curEndDate = curTimeObject.getEndTime();

                if (i + 1 < socialTime.size()) {
                    TimeObject nextTimeObject = socialTime.get(i + 1);
                    Date nextStartDate = nextTimeObject.getStartTime();

                    if (curEndDate.equals(nextStartDate)) {
                        boolean merge = curTimeObject.merge(nextTimeObject);

                        if (merge) {
                           socialTime.remove(i+1);
                            i--;
                        }
                    }
                }      
            }
            revisedSocialTimeLineMap.put(curMacAddress, socialTime);
        }
        
        
        socialIter = revisedSocialTimeLineMap.keySet().iterator();
        
        
        while (socialIter.hasNext()) {
            String curMacAddress = socialIter.next();
            List<TimeObject> socialTime = revisedSocialTimeLineMap.get(curMacAddress);

            Iterator<TimeObject> timeListIter = socialTime.iterator();

            while (timeListIter.hasNext()) {
                TimeObject curTimeObject = timeListIter.next();
                Date curStartDate = curTimeObject.getStartTime();
                Date curEndDate = curTimeObject.getEndTime();
                long timeDiff = (curEndDate.getTime() - curStartDate.getTime()) / 1000;
                
                //Remove all the TimeObjects that have a duration of less than 5 minutes
                if (timeDiff < 300) {
                    timeListIter.remove();
                } else {
                    socialTimeList.add(curTimeObject);
                }
            }
        }
        Collections.sort(socialTimeList);
        
        //Loop through the entire construct to combine fitting time objects
        int counter = 0;
        for(int i = 0; i < socialTimeList.size(); i++) {
            TimeObject curTimeObject = socialTimeList.get(i);

            if(i+1 < socialTimeList.size()) {
                TimeObject nextTimeObject = socialTimeList.get(i+1);
                if (curTimeObject.merge(nextTimeObject)) {
                    socialTimeList.remove(i+1);
                    i--;
                }
            }
        }

        socialIter = revisedSocialTimeLineMap.keySet().iterator();

        System.out.println("===REVISED SOCIAL CONSTRUCT===");

        while (socialIter.hasNext()) {
            String curMacAddress = socialIter.next();
            List<TimeObject> socialTime = revisedSocialTimeLineMap.get(curMacAddress);

            for (TimeObject timeObject : socialTime) {
                System.out.println("USER : " + curMacAddress);
                System.out.println(timeObject.getStartTime());
                System.out.println(timeObject.getEndTime());
            }
        }

        int totalSocialTime = 0;
        System.out.println("=====FINAL OUTPUT=====");
        
        //Add all the duration of the time objects together to calculate out the total social time
        for (int i = 0; i < socialTimeList.size(); i++) {
            TimeObject timeObject = socialTimeList.get(i);
            System.out.println("START SOCIAL" + timeObject.getStartTime());
            System.out.println("END SOCIAL" + timeObject.getEndTime());
            totalSocialTime += (int) (timeObject.getEndTime().getTime() - timeObject.getStartTime().getTime()) / 1000;
        }
        System.out.println(totalSocialTime);
        int percentSpentInGroup = Math.round(1.0f * totalSocialTime / totalTimeSpentInSIS * 100);
        int percentSpentAlone = 100 - percentSpentInGroup;
        if (totalTimeSpentInSIS == 0) {
            percentSpentInGroup = 0;
            percentSpentAlone = 0;
        }
        
        outputMap.put("total-time-spent-in-sis", totalTimeSpentInSIS);
        outputMap.put("group-percent", percentSpentInGroup);
        outputMap.put("solo-percent", percentSpentAlone);
        return outputMap;
    }

    /**
     * Populates a List of TimeObjects that represent the user's social time.
     *
     * @param socialTimeLine List of TimeObjects to be populated
     * @param logSemanticTimeMap HashMap of semantic place and activity of the
     * login in user
     * @param userSemanticTimeMap HashMap of semantic place and activity of
     * another user
     */
    public void constructSocialTimeLine(List<TimeObject> socialTimeLine, HashMap<String, List<TimeObject>> logSemanticTimeMap, HashMap<String, List<TimeObject>> userSemanticTimeMap) {
        System.out.println("IS NULL? : " + logSemanticTimeMap == null);
        Iterator<String> logSemanticTimeMapIter = logSemanticTimeMap.keySet().iterator();

        while (logSemanticTimeMapIter.hasNext()) {
            String semanticPlace = logSemanticTimeMapIter.next();

            List<TimeObject> logTimeObjects = logSemanticTimeMap.get(semanticPlace);

            if (userSemanticTimeMap.containsKey(semanticPlace)) {

                List<TimeObject> userTimeObjects = userSemanticTimeMap.get(semanticPlace);

                for (TimeObject logTimeObject : logTimeObjects) {
                    Date logStartTime = logTimeObject.getStartTime();
                    Date logEndTime = logTimeObject.getEndTime();

                    for (TimeObject userTimeObject : userTimeObjects) {
                        Date userStartTime = userTimeObject.getStartTime();
                        Date userEndTime = userTimeObject.getEndTime();

                        if (!logEndTime.before(userStartTime) && !logStartTime.after(userEndTime)) {
                            Date actualStart;
                            Date actualEnd;
                            if (logStartTime.before(userStartTime)) {
                                actualStart = userStartTime;
                            } else {
                                actualStart = logStartTime;
                            }

                            if (logEndTime.before(userEndTime)) {
                                actualEnd = logEndTime;
                            } else {
                                actualEnd = userEndTime;
                            }

                            socialTimeLine.add(new TimeObject(actualStart, actualEnd));
                        }
                    }
                }
            }
        }
    }

    /**
     * Get social app usage time for specified MAC Address
     *
     * @param startDate Start Date
     * @param endDate End Date
     * @param macAddress SHA-1 MAC Address
     * @return TreeMap of app with its respective percentage of app usage out of
     * the entire duration spent of "social" apps
     */
    public TreeMap<String, Integer> getSocialAppUsageTime(Date startDate, Date endDate, String macAddress) {
        TreeMap<String, Integer> socialAppUsageOutputMap = new TreeMap<>();
        List<App> appList = appDAO.retrieveAppByStartEndDate(startDate, endDate);
        List<App> userAppUsageList = new ArrayList<>();

        Iterator<App> appIter = appList.iterator();
        while (appIter.hasNext()) {
            App app = appIter.next();

            if (app.getAppPK().getDemographic().getMacAddress().equals(macAddress)) {
                userAppUsageList.add(app);
                appIter.remove();
            }
        }

        for (int i = 0; i < userAppUsageList.size(); i++) {
            App currApp = userAppUsageList.get(i);
            App nextApp = null;
            String appCategory = currApp.getAppLookup().getCategory();
            String appName = currApp.getAppLookup().getAppName();
            if (i + 1 < userAppUsageList.size()) {
                nextApp = userAppUsageList.get(i + 1);
            }
            if (appCategory.equals("social")) {
                int duration = basicAppController.calculateAppUsageTime(currApp, nextApp);
                if (socialAppUsageOutputMap.containsKey(appName)) {
                    int currUsageDuration = socialAppUsageOutputMap.get(appName);
                    socialAppUsageOutputMap.put(appName, currUsageDuration + duration);
                } else {
                    socialAppUsageOutputMap.put(appName, duration);
                }
            }
        }
        return socialAppUsageOutputMap;
    }

    /**
     * Calculate total duration spent by user in SIS
     *
     * @param userLocationList
     * @return int value in seconds of time spent by user in SIS
     */
    public int calculateTotalDuration(List<Location> userLocationList) {
        int totalDuration = 0;
        for (int i = 0; i < userLocationList.size(); i++) {
            Location location = userLocationList.get(i);
            if (i + 1 < userLocationList.size()) {
                Location nextLocation = userLocationList.get(i + 1);
                int temp = getDuration(location, nextLocation);
                totalDuration += temp;
                System.out.println("THIS DURATION : " + temp + " CURR TOTAL : " + totalDuration);
            } else {
                //last known location in SIS 
                //retrieve the next day Gregorian Calendar 
                int temp = getDuration(location, null);
                totalDuration += temp;
                System.out.println("THIS DURATION : " + temp + " CURR TOTAL : " + totalDuration);
            }
        }
        return totalDuration;
    }

    /**
     * Get duration spent at a location given two location objects. Duration is
     * calculated by the time difference between the 1st and the 2nd Location
     * object
     *
     * @param currLocation First Location object
     * @param nextLocation Next Location object
     * @return int value in seconds duration spent at First Location object
     */
    public int getDuration(Location currLocation, Location nextLocation) {
        int locatedTime = 0;

        //for first location
        LocationPK currLocationPK = currLocation.getLocationPK();
        String currMacAddress = currLocationPK.getMacAddress();
        Date currTimeStamp = currLocationPK.getTimestamp();
        String currSemanticPlace = currLocation.getLocationLookup().getSemanticPlace();
        GregorianCalendar currDate = new GregorianCalendar();
        currDate.setTime(currTimeStamp);
        int day = currDate.get(GregorianCalendar.DAY_OF_MONTH);
        int month = currDate.get(GregorianCalendar.MONTH);
        int year = currDate.get(GregorianCalendar.YEAR);
        currDate = new GregorianCalendar(year, month, day, 0, 0, 0);

        LocationPK nextLocationPK = null;
        String nextMacAddress = null;
        Date nextTimeStamp = null;
        String nextSemanticPlace = null;
        GregorianCalendar nextDate = null;
        int nextAppDay = 0;
        int nextAppMonth = 0;
        int nextAppYear = 0;
        //for next location
        if (nextLocation != null) {
            nextLocationPK = nextLocation.getLocationPK();
            nextMacAddress = nextLocationPK.getMacAddress();
            nextTimeStamp = nextLocationPK.getTimestamp();
            nextSemanticPlace = nextLocation.getLocationLookup().getSemanticPlace();
            nextDate = new GregorianCalendar();
            nextDate.setTime(nextTimeStamp);
            nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
            nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
            nextAppYear = nextDate.get(GregorianCalendar.YEAR);
            nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, 0, 0, 0);
        }

        //ensures same user and same semantic place or same user 
        if (currMacAddress.equals(nextMacAddress)) {
            if (currDate.equals(nextDate)) {
                locatedTime = (int) (nextTimeStamp.getTime() - currTimeStamp.getTime()) / 1000;
            } else {
                locatedTime = (int) (nextDate.getTime().getTime() - currTimeStamp.getTime()) / 1000;
            }

            if (locatedTime > 300) {
                locatedTime = 300;
            }
        } else {
            //user has no further update on the semantic place and has left the place
            //add 5 minutes (i.e. 300 seconds) to the locatedTime
            int nextDay = 0;
            int nextMonth = 0;
            int nextYear = 0;
            GregorianCalendar calendar = null;
            if (nextTimeStamp == null) {
                //initialise the next day with reference to calculate duration 
                calendar = new GregorianCalendar();
                calendar.setTime(currTimeStamp);
                nextDay = calendar.get(GregorianCalendar.DAY_OF_MONTH);
                nextMonth = calendar.get(GregorianCalendar.MONTH);
                nextYear = calendar.get(GregorianCalendar.YEAR);
                calendar = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
                locatedTime = (int) (calendar.getTime().getTime() - currTimeStamp.getTime()) / 1000;
            }

            if (locatedTime > 300) {
                locatedTime = 300;
            }
        }

        return locatedTime;
    }

}
