/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.AppDAO;
import smua.models.DAO.LocationDAO;
import smua.models.Entities.App;
import smua.models.Entities.AppLookup;
import smua.models.Entities.Location;
import smua.util.Entities.TimeObject;

/**
 *
 * @author r128
 */
@Controller
public class AdvancedSmartphoneOveruseController {

    @Autowired
    LocationDAO locationDAO;

    @Autowired
    SocialActivenessController socialActivenessController;

    @Autowired
    AppDAO appDAO;

    @Autowired
    BasicAppController basicAppController;

    /**
     * Returns the advanced smartphone overuse report based on the 
     * start date, end date and macaddress input by the user
     * @param startDate data specified in format yyyy-mm-dd
     * @param endDate date specified in format yyyy-mm-dd
     * @param macAddress macAddress of the logged in user.
     * @return HashMap<String, String> containing total non-productive app usage
     * time, overuse index, small group time and class time
     */
    public HashMap<String, String> getAdvSmartphoneReport(Date startDate, Date endDate, String macAddress) {
        List<App> userAppList = appDAO.retrieveAppByStartEndDate(startDate, endDate);
        List<Location> locationList = locationDAO.retrieveLocationByStartEndDate(startDate, endDate);
        System.out.println("List size : " + locationList.size());

        HashMap<String, String> data = new HashMap<String, String>();

        List<Location> userLocations = new ArrayList<>();
        for (Location location : locationList) {
            if (location.getLocationPK().getMacAddress().equals(macAddress)) {
                userLocations.add(location);
            }
        }
        //remove app reccords not belonging to user
        Iterator<App> appIter = userAppList.iterator();
        while (appIter.hasNext()) {
            App app = appIter.next();
            if (!app.getAppPK().getDemographic().getMacAddress().equals(macAddress)) {
                appIter.remove();
            }
        }

        //test
        List<TimeObject> classTimeList = calculateClassDuration(userLocations);
        System.out.println("Index : " + getOveruseIndex(100, 16));
        System.out.println("Index : " + getOveruseIndex(100, 4));
        System.out.println("Index : " + getOveruseIndex(100, 5));
        int classTime = 0;

        //calculate in class duration
        for (int i = 0; i < classTimeList.size(); i++) {
            TimeObject curTimeObject = classTimeList.get(i);
            Date startTime = curTimeObject.getStartTime();
            Date endTime = curTimeObject.getEndTime();
            classTime += (endTime.getTime() - startTime.getTime()) / 1000;
        }

        List<TimeObject> smallGroupTimeList = calculateGroupDuration(locationList, macAddress);

        int smallGroupTime = 0;

        for (int i = 0; i < smallGroupTimeList.size(); i++) {
            TimeObject curTimeObject = smallGroupTimeList.get(i);
            Date startTime = curTimeObject.getStartTime();
            Date endTime = curTimeObject.getEndTime();
            smallGroupTime += (endTime.getTime() - startTime.getTime()) / 1000;
        }

        List<TimeObject> smallGroupClassTimeList = smallGroupTimeList;
        smallGroupClassTimeList.addAll(classTimeList);

        Iterator<TimeObject> smallGroupClassTimeListIter = smallGroupClassTimeList.iterator();
        int counter = 0;
        while (smallGroupClassTimeListIter.hasNext()) {
            TimeObject curTimeObject = smallGroupClassTimeListIter.next();

            for (int i = counter + 1; i < smallGroupClassTimeList.size(); i++) {
                TimeObject nextTimeObject = smallGroupClassTimeList.get(i);
                if (nextTimeObject.merge(curTimeObject)) {
                    smallGroupClassTimeListIter.remove();
                    break;
                }
            }
            counter++;
        }
        int smallGroupClassTime = 0;
        for (int i = 0; i < smallGroupClassTimeList.size(); i++) {
            TimeObject curTimeObject = smallGroupClassTimeList.get(i);
            Date startTime = curTimeObject.getStartTime();
            Date endTime = curTimeObject.getEndTime();
            smallGroupClassTime += (endTime.getTime() - startTime.getTime()) / 1000;
        }

        int nonProductivePercentage = getNonProductivePercentage(userAppList, smallGroupClassTimeList);
        long totalNonProductiveAppUsageTime = 0;

        List<TimeObject> nonProductiveAppUsageList = getNonProductiveAppUsage(userAppList);
        for (TimeObject o : nonProductiveAppUsageList) {
            totalNonProductiveAppUsageTime += (o.getEndTime().getTime() - o.getStartTime().getTime());
        }
        String overuseIndex = getOveruseIndex(smallGroupClassTime, nonProductivePercentage);
        data.put("classTime", classTime + "");
        data.put("smallGroupTime", smallGroupTime + "");
        data.put("unproductiveAppUsage", (int) (totalNonProductiveAppUsageTime / 1000) + "");
        data.put("overuseIndex", overuseIndex);
        System.out.println("CLASS TIME" + classTime);
        System.out.println("SMALL GROUP TIME" + smallGroupTime);
        System.out.println("Overuse Index" + overuseIndex);
        return data;
    }

    /**
     * Returns non productive app usage
     *
     * @param userAppList list of app usage belonging to user
     * @return List<TimeObject> representing the user's app usage of non
     * productive apps
     */
    public List<TimeObject> getNonProductiveAppUsage(List<App> userAppList) {
        int currAppUsageTime = 0;
        boolean isNonProductive = false;
        Date currAppStartDate = null;
        List<TimeObject> nonProductiveAppUsageList = new ArrayList<TimeObject>();
        for (int i = 0; i < userAppList.size(); i++) {
            App currApp = userAppList.get(i);
            AppLookup currAppLookup = currApp.getAppLookup();
            String category = currAppLookup.getCategory();
            String currAppName = currAppLookup.getAppName();
            GregorianCalendar curCalDay = new GregorianCalendar();

            //check if this is first index of the list, or if different app usage
            if (currAppStartDate == null) {
                curCalDay.setTime(currApp.getAppPK().getTimestamp());
            }

            //check if app falls under nonproductive category
            isNonProductive = !category.equalsIgnoreCase("education") && !category.equalsIgnoreCase("information");

            App nextApp = null;
            String nextAppName = null;
            if (i + 1 < userAppList.size()) {
                nextApp = userAppList.get(i + 1);
                nextAppName = nextApp.getAppLookup().getAppName();
            }

            //calls method in BasicAppController to calculate app usage time
            int usageDuration = basicAppController.calculateAppUsageTime(currApp, nextApp);
            if (currAppName.equalsIgnoreCase(nextAppName)) {
                currAppUsageTime += usageDuration;
            } else {
                currAppUsageTime += usageDuration;
                //QN: REASON FRO CHECKING FOR NON-PRODUCTIVE APP CATEGORY AT THE BACK OF CALCULATION? 
                if (isNonProductive) {
                    Date startDate = curCalDay.getTime();
                    curCalDay.add(GregorianCalendar.SECOND, currAppUsageTime);
                    Date endDate = curCalDay.getTime();
                    nonProductiveAppUsageList.add(new TimeObject(startDate, endDate));
                }
                currAppStartDate = null;
                currAppUsageTime = 0;
            }
        }

        return nonProductiveAppUsageList;
    }

    /**
     * Get the total number of time in seconds that the user had non productive
     * app usage while in a small group or class
     *
     * @param userAppList list of app usage belonging to user
     * @param classSmallGroupTime list of time objects representing the interval
     * that the user was in a class or a small group
     * @return int total seconds of non productive app use in small group or
     * class
     */
    public int getNonProductivePercentage(List<App> userAppList, List<TimeObject> classSmallGroupTime) {
        int nonProductiveAppTime = 0;
        List<TimeObject> appTimeObjectList = getNonProductiveAppUsage(userAppList);

        //match again list of classSmallGroupTime to calculate non productive app time in class
        //condition, timeobjects must be ordered chronologically
        //nonProductiveAppTime
        double totalSum = 0;

        //loop through each smallGroupTime object
        for (int i = 0; i < classSmallGroupTime.size(); i++) {
            TimeObject curTimeObject = classSmallGroupTime.get(i);
            Date curClassGroupStart = curTimeObject.getStartTime();
            Date curClassGroupEnd = curTimeObject.getEndTime();
            System.out.println("START TIME " + curClassGroupStart);
            System.out.println("START TIME " + curClassGroupEnd);

            //within each loop, loop through non productive app usage to find overlaps
            for (int j = 0; j < appTimeObjectList.size(); j++) {
                TimeObject appTimeObject = appTimeObjectList.get(j);
                System.out.println("APP START : " + appTimeObject.getStartTime());
                System.out.println("APP END : " + appTimeObject.getEndTime());
                if (appTimeObject.getStartTime().after(curTimeObject.getEndTime())) {
                    break;
                }

                if (appTimeObject.overlap(curTimeObject)) {
                    Date appStart = appTimeObject.getStartTime();
                    Date appEnd = appTimeObject.getEndTime();
                    Date actualStart = null;
                    Date actualEnd = null;

                    if (appStart.before(curClassGroupStart)) {
                        actualStart = curClassGroupStart;
                    } else {
                        actualStart = appStart;
                    }

                    if (appEnd.before(curClassGroupEnd)) {
                        actualEnd = appEnd;
                    } else {
                        actualEnd = curClassGroupEnd;
                    }

                    double timeDiff = (actualEnd.getTime() - actualStart.getTime()) * 1.0 / 1000;
                    System.out.println("TIME DIFF" + timeDiff);
                    totalSum += timeDiff;
                }
            }
        }

        nonProductiveAppTime = (int) Math.round(totalSum);

        return nonProductiveAppTime;
    }

    /**
     * Returns the advanced smartphone overuse index of the user
     *
     * @param classSmallGroupTime duration in seconds spent in class or a small
     * group
     * @param nonProductiveAppTime duration in seconds spent on non productive
     * app usage while in a class or small group
     * @return String overuse index
     */
    public String getOveruseIndex(int classSmallGroupTime, int nonProductiveAppTime) {
        String overuseIndex = "";
        int percent = Math.round(1.0f * nonProductiveAppTime / classSmallGroupTime * 100);
        if (percent > 15) {
            overuseIndex = "Overusing";
        } else if (percent < 5) {
            overuseIndex = "Normal";
        } else {
            overuseIndex = "ToBeCautious";
        }
        return overuseIndex;
    }

    /**
     * Calculates the total time that the logged in user spent in class
     *
     * @param userLocations list of locations that the user has been to.
     * @return List<TimeObject> of in class duration
     */
    public List<TimeObject> calculateClassDuration(List<Location> userLocations) {
        List<TimeObject> classDurationList = new ArrayList<>();
        int curTime = 0;
        Date startDate = null;

        for (int i = 0; i < userLocations.size(); i++) {
            boolean isClass = false;
            Location location = userLocations.get(i);
            if (startDate == null) {
                startDate = location.getLocationPK().getTimestamp();
            }
            String semanticPlace = location.getLocationLookup().getSemanticPlace();
            isClass = semanticPlace.indexOf("SR") >= 0 && semanticPlace.indexOf("SMUSISL") >= 0;
            String nextSemanticPlace = null;
            Location nextLocation = null;
            if (i + 1 < userLocations.size()) {
                nextLocation = userLocations.get(i + 1);
                nextSemanticPlace = nextLocation.getLocationLookup().getSemanticPlace();
            } else {
                curTime += 300;
            }
            if (isClass && semanticPlace.equals(nextSemanticPlace)) {
                //did we account for isClass but diff semanticPlace in the middle 
                System.out.println("HELLO");
                curTime += socialActivenessController.getDuration(location, nextLocation);

            } else {
                System.out.println("TIME MORE THAN 1 HR :" + (curTime >= 3600) + " T:" + curTime);
                if (curTime >= 3600) {
                    //QN: ASSUME CLASS TIME IF MORE THAN 3600 SEC???
                    //beforehand curTime will not be added if location is not a valid SR 
                    //account for the very next update after the last SR reported 
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTime(startDate);
                    //add current time to startdate and set it as end date 
                    calendar.add(GregorianCalendar.SECOND, curTime);
                    Date endDate = calendar.getTime();
                    System.out.println(endDate);
                    classDurationList.add(new TimeObject(startDate, endDate));
                    if (nextLocation != null) {
                        startDate = nextLocation.getLocationPK().getTimestamp();
                    }
                    curTime = 0;
                }
            }
        }

        return classDurationList;
    }

    /**
     * Calculates the total time that the logged in user spent in small groups
     * less the time that the user was in the presence of 3 other users or more
     *
     * @param locationList list of locations
     * @param macAddress mac address of logged in user
     * @return List<TimeObject> of group duration
     */
    public List<TimeObject> calculateGroupDuration(List<Location> locationList, String macAddress) {
        LinkedHashMap<String, Integer> outputMap = new LinkedHashMap<>();
        ArrayList<Location> userLocationList = new ArrayList<Location>();
        Iterator<Location> locationIter = locationList.iterator();

        //redundant
        while (locationIter.hasNext()) {
            Location userLocation = locationIter.next();
            if (userLocation.getLocationPK().getMacAddress().equals(macAddress)) {
                userLocationList.add(userLocation);
            }
        }

        //reuse method in SocialActivenessController to calculate total time spent in SIS
        int totalTimeSpentInSIS = socialActivenessController.calculateTotalDuration(userLocationList);
        HashMap<String, HashMap<String, List<TimeObject>>> userSemanticTimeMap = new HashMap<String, HashMap<String, List<TimeObject>>>();

        //for each location in location list.
        for (int i = 0; i < locationList.size(); i++) {
            Location userLocation = locationList.get(i);

            String userMacAddress = userLocation.getLocationPK().getMacAddress();
            String userSemanticPlace = userLocation.getLocationLookup().getSemanticPlace();
            Date userTimestamp = userLocation.getLocationPK().getTimestamp();

            //check if currLocation object is the last location in the list.
            if (i + 1 < locationList.size()) {
                Location nextUserLocation = locationList.get(i + 1);

                String nextUserMacAddress = nextUserLocation.getLocationPK().getMacAddress();
                String nextUserSemanticPlace = nextUserLocation.getLocationLookup().getSemanticPlace();
                Date nextUserTimestamp = nextUserLocation.getLocationPK().getTimestamp();

                //check if macAddress in currLocation object is the same as the macAddress in the next location object
                if (userMacAddress.equals(nextUserMacAddress)) {

                    Date actualEndTime = nextUserTimestamp;

                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(userTimestamp);
                    long timeDiff = (nextUserTimestamp.getTime() - userTimestamp.getTime()) / 1000;
                    //QN*********************Redundant code?***********************//
                    cal.getTime();

                    //if time difference between two locations is more than 300 seconds, take default 300 seconds
                    if (timeDiff > 300) {
                        cal.add(GregorianCalendar.SECOND, 300);
                        actualEndTime = cal.getTime();
                    }

                    //create empty HashMap of location and list of time object representing actiivity within the given semantic place
                    HashMap<String, List<TimeObject>> semanticTimeMap = null;

                    //if reccord exists, get map from userSemanticTimeMap
                    if (userSemanticTimeMap.containsKey(userMacAddress)) {
                        semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                    } else {
                        semanticTimeMap = new HashMap<String, List<TimeObject>>();
                    }

                    if (semanticTimeMap.containsKey((userSemanticPlace))) {

                        List<TimeObject> timeObjectList = semanticTimeMap.get(userSemanticPlace);

                        TimeObject curTimeObject = new TimeObject(userTimestamp, actualEndTime);

                        int counter = 0;
                        boolean merge = false;

                        //try to merge all time objects in the list for the current semantic place
                        //QN: WHY NO NEED SORT THROUGH THE TIMEOBJECTLIST BEFORE MERGING???
                        //based on insertion ascending order 
                        while (!merge && counter < timeObjectList.size()) {
                            TimeObject timeObject = timeObjectList.get(counter);
                            //one user can only be present at the same location at one time 
                            //this explains why once merge is true, exit while loop 
                            //doesnt mean that there is only one TimeObject at the end date 
                            //curTimeObject will just be garbage collected 
                            merge = timeObject.merge(curTimeObject);
                            counter++;
                        }

                        if (!merge) {
                            //unable to merge and reaches the end of the timeobject list
                            //for those cant be merged, there will be two separate TimeObjects in this case
                            timeObjectList.add(curTimeObject);
                        }

                    } else {
                        //Hashmap does not contain the TimeObject list initially
                        //initialise list 
                        List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                        timeObjectList.add(new TimeObject(userTimestamp, actualEndTime));
                        semanticTimeMap.put(userSemanticPlace, timeObjectList);
                    }

                    userSemanticTimeMap.put(userMacAddress, semanticTimeMap);

                } else {//if different mac address,

                    HashMap<String, List<TimeObject>> semanticTimeMap = null;
                    Date actualEndTime = userTimestamp;

                    if (userSemanticTimeMap.containsKey(userMacAddress)) {
                        semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                    } else {
                        semanticTimeMap = new HashMap<String, List<TimeObject>>();
                    }

                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(userTimestamp);

                    cal.add(GregorianCalendar.SECOND, 300);
                    actualEndTime = cal.getTime();

                    if (semanticTimeMap.containsKey((userSemanticPlace))) {
                        List<TimeObject> timeObjectList = semanticTimeMap.get(userSemanticPlace);
                        TimeObject curTimeObject = new TimeObject(userTimestamp, actualEndTime);
                        timeObjectList.add(curTimeObject);

                    } else {
                        List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                        timeObjectList.add(new TimeObject(userTimestamp, actualEndTime));
                        semanticTimeMap.put(userSemanticPlace, timeObjectList);
                    }

                    userSemanticTimeMap.put(userMacAddress, semanticTimeMap);
                }
            } else {
                //account for last location in the list 
                HashMap<String, List<TimeObject>> semanticTimeMap = null;
                Date actualEndTime = userTimestamp;

                if (userSemanticTimeMap.containsKey(userMacAddress)) {
                    semanticTimeMap = userSemanticTimeMap.get(userMacAddress);
                } else {
                    semanticTimeMap = new HashMap<String, List<TimeObject>>();
                }

                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(userTimestamp);

                cal.add(GregorianCalendar.SECOND, 300);
                actualEndTime = cal.getTime();

                if (semanticTimeMap.containsKey((userSemanticPlace))) {
                    List<TimeObject> timeObjectList = semanticTimeMap.get(userSemanticPlace);
                    TimeObject curTimeObject = new TimeObject(userTimestamp, actualEndTime);
                    timeObjectList.add(curTimeObject);

                } else {
                    List<TimeObject> timeObjectList = new ArrayList<TimeObject>();
                    timeObjectList.add(new TimeObject(userTimestamp, actualEndTime));
                    semanticTimeMap.put(userSemanticPlace, timeObjectList);
                }

                userSemanticTimeMap.put(userMacAddress, semanticTimeMap);
            }
        }

        //semanticTimeMap of logged in user.
        HashMap<String, List<TimeObject>> loggedInUserSemanticTimeMap = userSemanticTimeMap.get(macAddress);

        if (loggedInUserSemanticTimeMap == null) {
            return new ArrayList<TimeObject>();
        }
        //remove logged-in user macaddress in userSemanticTimeMap after retrieval 
        userSemanticTimeMap.remove(macAddress);

        HashMap<String, List<TimeObject>> socialTimeLineMap = new HashMap<String, List<TimeObject>>();

        Iterator<String> mapIter = userSemanticTimeMap.keySet().iterator();

        //iterate through map
        //TESTING METHOD 
        //printing out the logged-in users that he had been through 
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

        mapIter = userSemanticTimeMap.keySet().iterator();

        //for each user, get semantic time map
        while (mapIter.hasNext()) {
            String curMacAddress = mapIter.next();
            HashMap<String, List<TimeObject>> semanticTimeMap = userSemanticTimeMap.get(curMacAddress);

            //construct list of time objects that represent social time
            List<TimeObject> socialTimeLine = new ArrayList<TimeObject>();
            constructSocialTimeLine(socialTimeLine, curMacAddress, loggedInUserSemanticTimeMap, semanticTimeMap);
            Collections.sort(socialTimeLine);
            socialTimeLineMap.put(curMacAddress, socialTimeLine);
        }
        
        Iterator<String> socialIter = socialTimeLineMap.keySet().iterator();

        System.out.println("==============SOCIAL CONSTRUCT================");

        //TEST CODE 
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
        //Social Interactions of the other users with the logged-in user 
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

                    //try to merge time objects, remove if merged successfully.
                    if (curEndDate.equals(nextStartDate)) {
                        boolean merge = curTimeObject.merge(nextTimeObject);

                        if (merge) {
                            //remove the next TimeObject if merge successful 
                            socialTime.remove(i + 1);
                            i--;
                        }
                    }
                } else {
                    //at the last TimeObject 
                    //and make sure there is more than two TimeObjects to compare with in the list 
                    if (socialTime.size() > 1) {
                        TimeObject prevTimeObject = socialTime.get(socialTime.size() - 1);
                        Date prevEndDate = prevTimeObject.getEndTime();
                        Date curStartDate = curTimeObject.getStartTime();

                        if (curStartDate.equals(prevEndDate)) {
                            boolean merge = prevTimeObject.merge(curTimeObject);
                            if (merge) {
                                //removing the VERY LAST object 
                                //last index = n - 1 
                                socialTime.remove(i - 1);
                                i--;
                            }
                        }
                    }
                }
            }
            revisedSocialTimeLineMap.put(curMacAddress, socialTime);
        }

        //Iterator<String> revisedSocialIter = revisedSocialTimeLineMap.keySet().iterator();
        System.out.println("==============REVISED SOCIAL CONSTRUCT================");
        socialIter = revisedSocialTimeLineMap.keySet().iterator();
        //for each time object in social time map, make corrections.
        //TEST CODE 
        while (socialIter.hasNext()) {
            String curMacAddress = socialIter.next();
            List<TimeObject> socialTime = revisedSocialTimeLineMap.get(curMacAddress);

            for (int i = 0; i < socialTime.size(); i++) {
                TimeObject timeObject = socialTime.get(i);

                Date startTime = timeObject.getStartTime();
                Date endTime = timeObject.getEndTime();

                //check if social interaction lasts more than 5 mins 
                //if yes add to list
                if (((endTime.getTime() - startTime.getTime()) * 1.0 / 1000) >= 300) {
                    socialTimeList.add(timeObject);
                    System.out.println("START TIME : " + startTime);
                }
            }
        }
        System.out.println("SOCIAL TIME LIST SIZE :" + socialTimeList.size());
        Collections.sort(socialTimeList);

        //to count the number of big group interaction time at this moment 
        //(3 or more friends) wrt logged-in user
        List<TimeObject> bigGroupList = new ArrayList<TimeObject>();
        TimeObject curOverlap = null;
        TimeObject preOverlap = null;

        //overlapping social interactions here 
        for (int i = 0; i < socialTimeList.size(); i++) {
            TimeObject curTimeObject = socialTimeList.get(i);
            Date curStartDate = curTimeObject.getStartTime();
            Date curEndDate = curTimeObject.getEndTime();

            for (int j = i + 1; j < socialTimeList.size(); j++) {
                TimeObject nextTimeObject = socialTimeList.get(j);

                Date nextStartDate = nextTimeObject.getStartTime();
                if (nextStartDate.after(curEndDate)) {
                    break;
                }
                if (curTimeObject.overlap(nextTimeObject)) {

                    Date nextEndDate = nextTimeObject.getEndTime();
                    //actualEndDate as the variable 
                    Date actualEndDate = curEndDate;

                    if (nextEndDate.before(curEndDate)) {
                        actualEndDate = nextEndDate;
                    }

                    curOverlap = new TimeObject(nextStartDate, actualEndDate);
                    //if there are overlaps, create timeobject representing overlap with next start date and ACTUAL end date.
                    if (preOverlap != null && curOverlap != null && preOverlap.overlap(curOverlap)) {
                        //QN: WHY NEED TO REINITALISE CUROVERLAP? 
                        //ensure three or more users here 
                        //because of two or more overlapping areas
                        curOverlap = new TimeObject(nextStartDate, actualEndDate);
                        bigGroupList.add(curOverlap);
                    }
                }
                preOverlap = curOverlap;
            }
        }
        //sort big group list time objects in chronological order.
        Collections.sort(bigGroupList);
        Iterator<TimeObject> bigGroupListIter = bigGroupList.iterator();
        int counter = 1;

        //merge bigGroup TimeObject if possible
        for (int i = 0; i < bigGroupList.size(); i++) {
            TimeObject curTimeObject = bigGroupList.get(i);
            //do no need to account for the last TimeObject in this case
            //just trying to merge all TimeObjects except for the last one 
            if (i + 1 < bigGroupList.size()) {
                TimeObject nextTimeObject = bigGroupList.get(i + 1);

                if (curTimeObject.merge(nextTimeObject)) {
                    System.out.println("REMOVE");
                    bigGroupList.remove(i + 1);
                    i--;
                }

            }

        }

        System.out.println("==========BIG GROUP TIME==============");
        for (int i = 0; i < bigGroupList.size(); i++) {
            TimeObject curTimeObject = bigGroupList.get(i);
            System.out.println("START TIME : " + curTimeObject.getStartTime());
            System.out.println("END TIME : " + curTimeObject.getEndTime());
        }
        
         System.out.println("==========SOCIAL GROUP TIME==============");
        for (int i = 0; i < socialTimeList.size(); i++) {
            TimeObject curTimeObject = socialTimeList.get(i);
            System.out.println("START TIME : " + curTimeObject.getStartTime());
            System.out.println("END TIME : " + curTimeObject.getEndTime());
        }

        //iterate through social time and big group time to find overlaps
        //remove socialTimeList TimeObjects to find small group social durations in the end 
        //bigGroup TimeObject as reference of comparison 
        //but REMOVAL from SOCIALTIMELIST 
        for (TimeObject bigGroupTimeObj : bigGroupList) {
            Date bigGroupStartTime = bigGroupTimeObj.getStartTime();
            Date bigGroupEndTime = bigGroupTimeObj.getEndTime();

            for (int i = 0; i < socialTimeList.size(); i++) {
                TimeObject userTimeObj = socialTimeList.get(i);
                Date userStartTime = userTimeObj.getStartTime();
                Date userEndTime = userTimeObj.getEndTime();

                if (userStartTime.after(bigGroupEndTime)) {
                    break;
                }
                System.out.println("Overlap:"+bigGroupTimeObj.overlap(userTimeObj));
                System.out.println(userStartTime);
                if (bigGroupTimeObj.overlap(userTimeObj)) {
                    
                    //if big group start time before user start time and big group sart time ater end time.
                    if (bigGroupStartTime.before(userStartTime) && bigGroupEndTime.after(userEndTime)) {
                        socialTimeList.remove(i);
                        i--;

                        //else if big group start time equals user start time and big group end time equals to user end time.
                    } else if (bigGroupStartTime.equals(userStartTime) && bigGroupEndTime.equals(userEndTime)) {
                        socialTimeList.remove(i);
                        i--;

                        //if big group start time equals user strt time and big group end time after user end time
                    } else if (bigGroupStartTime.equals(userStartTime) && bigGroupEndTime.after(userEndTime)) {
                        socialTimeList.remove(i);
                        i--;

                        //if big group start time before user star time big group end time equals user end time.
                    } else if (bigGroupStartTime.before(userStartTime) && bigGroupEndTime.equals(userEndTime)) {
                        socialTimeList.remove(i);
                        i--;

                        //if big group start time after user start time and big group end time before user end time.
                    } else if (bigGroupStartTime.after(userStartTime) && bigGroupEndTime.before(userEndTime)) {
                        int startDuration = (int) (bigGroupStartTime.getTime() - userStartTime.getTime()) / 1000;
                        int endDuration = (int) (userEndTime.getTime() - bigGroupEndTime.getTime()) / 1000;
                        boolean startTimeUsed = false;

                        if (startDuration < 300 && endDuration < 300) {
                            socialTimeList.remove(i);
                            i--;
                        } else {
                            if (startDuration >= 300) {
                                userTimeObj.setEndTime(bigGroupStartTime);
                                startTimeUsed = true;
                            }

                            if (startTimeUsed && endDuration >= 300) {
                                //create new TimeObject since we have previously shortened userTimeObj
                                TimeObject endTimeObj = new TimeObject(bigGroupEndTime, userEndTime);
                                //add to socialTimeList, NOT bigGroupList 
                                socialTimeList.add(i + 1, endTimeObj);
                                i++;
                            } else if (endDuration >= 300) {
                                userTimeObj.setStartTime(bigGroupEndTime);
                                
                            }
                        }

                    } else if (bigGroupStartTime.after(userStartTime)) {
                        int duration = (int) (bigGroupStartTime.getTime() - userStartTime.getTime()) / 1000;
                        if (duration < 300) {
                            socialTimeList.remove(i);
                            i--;
                        } else {
                            userTimeObj.setEndTime(bigGroupStartTime);
                        }

                    //QN: AREN'T THEY THE SAME??? to account for all permutations 
                    } else if (bigGroupStartTime.equals(userStartTime)) {
                        //bigGroupEndTime is BEFORE userEndTime 
                        int duration = (int) (userEndTime.getTime() - bigGroupEndTime.getTime()) / 1000;
                        if (duration < 300) {
                            socialTimeList.remove(i);
                            i--;
                        } else {
                            userTimeObj.setStartTime(bigGroupEndTime);  
                        }

                    } else if (bigGroupEndTime.before(userEndTime)) {
                        //bigGroupStartTime is EQUAL TO userStartTime
                        int duration = (int) (userEndTime.getTime() - bigGroupEndTime.getTime()) / 1000;
                        if (duration < 300) {
                            socialTimeList.remove(i);
                            i--;
                        } else {
                            userTimeObj.setStartTime(bigGroupEndTime);
                        }

                    }
                }
            }
        }
        System.out.println("==========NON BIG GROUP SOCIAL GROUP TIME==============");
        for (int i = 0; i < socialTimeList.size(); i++) {
            TimeObject curTimeObject = socialTimeList.get(i);
            System.out.println("START TIME : " + curTimeObject.getStartTime());
            System.out.println("END TIME : " + curTimeObject.getEndTime());
        }

        for (int i = 0; i < socialTimeList.size(); i++) {
            TimeObject curTimeObject = socialTimeList.get(i);

            Date curEndDate = curTimeObject.getEndTime();

            if (i + 1 < socialTimeList.size()) {
                TimeObject nextTimeObject = socialTimeList.get(i + 1);
                Date nextStartDate = nextTimeObject.getStartTime();

                boolean merge = curTimeObject.merge(nextTimeObject);

                if (merge) {
                    System.out.println("MERGE");
                    socialTimeList.remove(i + 1);
                    i--;
                }

            }
        }

        return socialTimeList;

    }

    /**
     * Constructs a list of timeline pertaining to each user according to the
     * time that they came into contact with the logged-in user at each semantic
     * that the logged-in user was in
     *
     * @param socialTimeLine empty list of time object to be constructed
     * @param userMacAddress user's mac address
     * @param logSemanticTimeMap login user's semantic time map
     * @param userSemanticTimeMap other user's semantic time map
     */
    public void constructSocialTimeLine(List<TimeObject> socialTimeLine, String userMacAddress, HashMap<String, List<TimeObject>> loggedInUserSemanticTimeMap, HashMap<String, List<TimeObject>> userSemanticTimeMap) {
        System.out.println("IS NULL? : " + loggedInUserSemanticTimeMap == null);
        Iterator<String> logSemanticTimeMapIter = loggedInUserSemanticTimeMap.keySet().iterator();

        while (logSemanticTimeMapIter.hasNext()) {
            String semanticPlace = logSemanticTimeMapIter.next();

            List<TimeObject> logTimeObjects = loggedInUserSemanticTimeMap.get(semanticPlace);

            if (userSemanticTimeMap.containsKey(semanticPlace)) {
                //check for overlapping timing within the same semantic place as logged in user 
                List<TimeObject> userTimeObjects = userSemanticTimeMap.get(semanticPlace);

                for (TimeObject logTimeObject : logTimeObjects) {
                    Date logStartTime = logTimeObject.getStartTime();
                    Date logEndTime = logTimeObject.getEndTime();
                    //comparing logged in user TimeObject with other user's TimeObject
                    //to check if there is overlap 
                    for (TimeObject userTimeObject : userTimeObjects) {
                        Date userStartTime = userTimeObject.getStartTime();
                        Date userEndTime = userTimeObject.getEndTime();

                        if (!logEndTime.before(userStartTime) && !logStartTime.after(userEndTime)) {
                            //other user's TimeObject is WITHIN logged-in user TimeObject
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

                            if (!actualStart.equals(actualEnd)) {
                                List<String> usersList = new ArrayList<String>();
                                usersList.add(userMacAddress);
                                socialTimeLine.add(new TimeObject(actualStart, actualEnd, usersList));
                            }

                        }
                    }
                }
            }
        }
    }
}
