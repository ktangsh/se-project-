/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.util.ArrayList;
import java.util.Collection;
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
import smua.models.DAO.AppLookupDAO;
import smua.models.DAO.DemographicDAO;
import smua.models.Entities.App;
import smua.models.Entities.AppLookup;
import smua.models.Entities.Demographic;
import smua.models.JSON.TopKApp;
import smua.models.JSON.TopKSchool;
import smua.models.JSON.TopKStudent;

/**
 *
 * @author shtang.2014
 */
@Controller
public class TopKController {

    @Autowired
    AppDAO appDAO;

    @Autowired
    AppLookupDAO appLookupDAO;

    @Autowired
    DemographicDAO demographicDAO;

    /**
     * Retrieve Top K Apps given a start date, end date, school and top K value
     *
     * @param startDateTime the start date and time  of the input field 
     * @param endDateTime the end date and time of the input field 
     * @param school selected school (SIS, Business, Economics, Accountancy, SOCSC, Law) 
     * @param topKValue top K value chosen by user, where k can range from 1 to 10 (both inclusive, in integer increments)
     * 
     * @return ArrayList<TopKApp> ArrayList of TopKApp objects 
     */
    public ArrayList<TopKApp> retrieveTopKApps(Date startDateTime, Date endDateTime, String school, String topKValue) {

        List<App> appList = appDAO.retrieveTopKAppBySchool(startDateTime, endDateTime, school);
        //calculate app duration based on given startdatetime, enddatetime and school 
        TreeMap<Integer, Integer> map = calculateAppDuration(appList);
        int topKNum = Integer.parseInt(topKValue);
        //store the relevant app in top k values and place it in order in a LinkedHashMap in order of descending order of duration 
        LinkedHashMap<Integer, Integer> durationMap = topKAppsSortByDuration(map, topKNum);
        //rank according to the descending order of duration 
        ArrayList<TopKApp> rankList = topKAppsSortByRank(durationMap);
        //sort all app in alphabetical order, especially for tie ranks
        Collections.sort(rankList);
        return rankList;
    }

    /**
     * Calculates total app usage duration from the list of app and orders the
     * app names in alphabetical order
     *
     * @param appList List of App
     * 
     * @return TreeMap<Integer, Integer> TreeMap of App id as key and duration as respective value
     */
    public TreeMap<Integer, Integer> calculateAppDuration(List<App> appList) {

        TreeMap<Integer, Integer> appDurationMap = new TreeMap<>();

        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();
            int curAppID = curApp.getAppLookup().getAppID();

            GregorianCalendar curCalDay = new GregorianCalendar();

            curCalDay.setTime(curTimestamp);
            int curDay = curCalDay.get(GregorianCalendar.DAY_OF_MONTH);
            int curMonth = curCalDay.get(GregorianCalendar.MONTH);
            int curYear = curCalDay.get(GregorianCalendar.YEAR);

            curCalDay = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);

            //ensure retrieval of app is up to the last app to prevent indexOutOfBoundException
            if (i + 1 < appList.size()) {
                App nextApp = appList.get(i + 1);
                int nextAppID = nextApp.getAppLookup().getAppID();
                String nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
                Date nextTimestamp = nextApp.getAppPK().getTimestamp();

                GregorianCalendar nextCalDay = new GregorianCalendar();
                nextCalDay.setTime(nextTimestamp);
                int nextDay = nextCalDay.get(GregorianCalendar.DAY_OF_MONTH);
                int nextMonth = nextCalDay.get(GregorianCalendar.MONTH);
                int nextYear = nextCalDay.get(GregorianCalendar.YEAR);

                nextCalDay = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                int timeDiff = 0;

                //checks if same user 
                if (curMacAddress.equals(nextMacAddress)) {

                    if (curCalDay.equals(nextCalDay)) {
                        timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                    } else {
                        timeDiff = (int) ((nextCalDay.getTime().getTime() - curTimestamp.getTime()) / 1000);

                        if (timeDiff > 10) {
                            timeDiff = 10;
                        }
                    }

                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }

                    //if Treemap previously contains an app duration of a particular app id, add on the stored duration 
                    if (appDurationMap.containsKey(curAppID)) {
                        timeDiff += appDurationMap.get(curAppID);
                    }
                    appDurationMap.put(curAppID, timeDiff);

                } else {
                    nextCalDay = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);
                    nextCalDay.add(GregorianCalendar.DAY_OF_MONTH, 1);

                    timeDiff = (int) ((nextCalDay.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    if (timeDiff > 10) {
                        timeDiff = 10;
                    }

                    if (appDurationMap.containsKey(curAppID)) {
                        timeDiff += appDurationMap.get(curAppID);
                    }

                    appDurationMap.put(curAppID, timeDiff);
                }
            //to account for the very last app 
            } else {
                GregorianCalendar nextDate = new GregorianCalendar();
                nextDate.setTime(curTimestamp);
                int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextYear = nextDate.get(GregorianCalendar.YEAR);

                nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                int timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);

                //if time difference is more than 10 seconds, set it as 10 seconds 
                if (timeDiff > 10) {
                    timeDiff = 10;
                }

                //check if Treemap previously contains appId 
                if (appDurationMap.containsKey(curAppID)) {
                    timeDiff += appDurationMap.get(curAppID);
                }
                
                appDurationMap.put(curAppID, timeDiff);
            }

        }

        return appDurationMap;
    }

    /**
     * Sorts the app usage duration in descending order up to the top K value
     *
     * @param map TreeMap of App id as key and duration as the respective value 
     * @param topKNum top K value chosen by user, where k can range from 1 to 10 (both inclusive, in integer increments)
     * 
     * @return LinkedHashMap<Integer, Integer> LinkedHashMap in order of descending order of duration 
     */
    public LinkedHashMap<Integer, Integer> topKAppsSortByDuration(TreeMap<Integer, Integer> map, int topKNum) {
        LinkedHashMap<Integer, Integer> linkedDurationMap = new LinkedHashMap<>();

        Collection<Integer> durationValues = map.values();
        ArrayList<Integer> list = new ArrayList<>(durationValues);
        Collections.sort(list); //sorting done in ascending natural ordering 
        Collections.reverse(list); //reverse the order of duration in the list, so that app is placed in descending order

        for (int i = 0; i < list.size() && i < topKNum; i++) {
            //sort duration only up till K value 
            int duration = list.get(i);
            Iterator<Integer> appIdIter = map.keySet().iterator();
            while (appIdIter.hasNext()) {
                int appId = appIdIter.next();
                if (map.get(appId) == duration) {
                    //if duration matches the corresponding duration in the Treemap 
                    //we put in ranking order for the app duration in descending order in the linkedhashmap 
                    //at the same time, replace the matching app from the map 
                    linkedDurationMap.put(appId, duration);
                }
            }
        }
        return linkedDurationMap;
    }

    /**
     * Assigns rank to the TopKApp based on the duration from the linked
     * duration map
     *
     * @param linkedDurationMap LinkedHashmap of app id as key and duration as respective value
     * 
     * @return ArrayList<TopKApp> List of ranked TopKApp 
     */
    public ArrayList<TopKApp> topKAppsSortByRank(LinkedHashMap<Integer, Integer> linkedDurationMap) {
        ArrayList<TopKApp> sortedList = new ArrayList<>();
        Iterator<Integer> appNameIter = linkedDurationMap.keySet().iterator();
        int prevAppID = 0;
        int rankCount = 1;
        int counter = 1;
        List<AppLookup> appLookupList = appLookupDAO.retrieveAll();
        if (appNameIter.hasNext()) {
            prevAppID = appNameIter.next();   //retrieve id of first app
            for (AppLookup al : appLookupList) { //search for app name
                int prevAppDuration = linkedDurationMap.get(prevAppID);
                if (al.getAppID() == prevAppID) {
                    String prevAppName = al.getAppName();
                    sortedList.add(new TopKApp(rankCount, prevAppName, prevAppDuration));    //add into rank list
                }
            }
        }
        while (appNameIter.hasNext()) {   //retrieving id for subsequent apps in linkedDurationMap
            int curAppID = appNameIter.next();
            int prevAppDuration = linkedDurationMap.get(prevAppID);
            int curAppDuration = linkedDurationMap.get(curAppID);
            String curAppName = null;

            if (prevAppDuration == curAppDuration) {    //check if both records will be of the same rank
                counter++;
                for (AppLookup al : appLookupList) { //search for app name
                    prevAppDuration = linkedDurationMap.get(prevAppID);
                    if (al.getAppID() == curAppID) {
                        curAppName = al.getAppName();
                        sortedList.add(new TopKApp(rankCount, curAppName, prevAppDuration));    //add into rank list
                    }
                }

            } else {
                rankCount += counter;
                counter = 1;
                for (AppLookup al : appLookupList) { //search for app name
                    if (al.getAppID() == curAppID) {
                        curAppName = al.getAppName();
                        sortedList.add(new TopKApp(rankCount, curAppName, curAppDuration));    //add into rank list
                    }
                }
            }
            prevAppID = curAppID;
        }
        return sortedList;
    }

    /**
     * Retrieves the list of TopKStudents ranked in descending order
     *
     * @param startDateTime the start date and time of the input field 
     * @param endDateTime the end date and time of the input field 
     * @param appCategory selected App Category from the list: (Books, Social, Education, Entertainment, Information, Library, Local, Tools, Fitness, Games, Others)
     * @param topKValue top K value chosen by user, where k can range from 1 to 10 (both inclusive, in integer increments)
     * 
     * @return ArrayList<TopKStudent> ArrayList of ranked Top K Student  
     */
    public ArrayList<TopKStudent> retrieveTopKStudents(Date startDateTime, Date endDateTime, String appCategory, String topKValue) {

        List<App> appList = appDAO.retrieveAppByMacDate(startDateTime, endDateTime);
        HashMap<String, Integer> macMap = new HashMap<>(); //storing mac address as key, duration as value 
        if (appList.isEmpty()) {
            //checks if HQL query returns zero entry, will return empty list
            return new ArrayList<>();
        }

        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();

            GregorianCalendar curDate = new GregorianCalendar();

            curDate.setTime(curTimestamp);
            int curDay = curDate.get(GregorianCalendar.DAY_OF_MONTH);
            int curMonth = curDate.get(GregorianCalendar.MONTH);
            int curYear = curDate.get(GregorianCalendar.YEAR);

            curDate = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);

            //ensure retrieval of app is up to the last app to prevent indexOutOfBoundException
            if (i + 1 < appList.size()) {
                App nextApp = appList.get(i + 1);
                String nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
                Date nextTimestamp = nextApp.getAppPK().getTimestamp();

                GregorianCalendar nextDate = new GregorianCalendar();
                nextDate.setTime(nextTimestamp);
                int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextYear = nextDate.get(GregorianCalendar.YEAR);

                nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                int timeDiff = 0;

                //check if same user 
                if (curMacAddress.equals(nextMacAddress)) {

                    //check if current app date is the same as the next app date 
                    if (curDate.equals(nextDate)) {
                        timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                    } else {
                        timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    }

                    //if time difference is more than 120 seconds, assign it as 10 seconds instead 
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }
                    
                    //check if currApp matches the user selected app category and if hashmap previously contains the stored user 
                    if (curApp.getAppLookup().getCategory().equals(appCategory)) {
                        if (macMap.containsKey(curMacAddress)) {
                            timeDiff += macMap.get(curMacAddress);
                        }
                        macMap.put(curMacAddress, timeDiff);
                    }
                    
                } else {
                    nextDate = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);
                    //set next date as the day after given current date 
                    nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                    timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    //check if time difference is more than 10 seconds
                    if (timeDiff > 10) {
                        timeDiff = 10;
                    }

                    if (curApp.getAppLookup().getCategory().equals(appCategory)) {
                        if (macMap.containsKey(curMacAddress)) {
                            timeDiff += macMap.get(curMacAddress);
                        }
                        macMap.put(curMacAddress, timeDiff);
                    }
                }
            //account for the very last app 
            } else {
                GregorianCalendar nextDate = new GregorianCalendar();
                nextDate.setTime(curTimestamp);
                int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextYear = nextDate.get(GregorianCalendar.YEAR);

                nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                int timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                if (timeDiff > 10) {
                    timeDiff = 10;
                }

                if (curApp.getAppLookup().getCategory().equals(appCategory)) {
                    if (macMap.containsKey(curMacAddress)) {
                        timeDiff += macMap.get(curMacAddress);
                    }
                    macMap.put(curMacAddress, timeDiff);
                }
            }
        }

        LinkedHashMap<String, Integer> linkedDurationMap = topKStudentsSortByDuration(macMap, Integer.parseInt(topKValue));
        ArrayList<TopKStudent> studentList = topKStudentsSortByRank(linkedDurationMap);
        Collections.sort(studentList);
        return studentList;
    }

    /**
     * Sorts the students by their app usage duration up to the top K value
     *
     * @param map HashMap of App id as key and duration as the respective value 
     * @param topKValue top K value chosen by user, where k can range from 1 to 10 (both inclusive, in integer increments)
     * 
     * @return LinkedHashMap<String, Integer>
     */
    public LinkedHashMap<String, Integer> topKStudentsSortByDuration(HashMap<String, Integer> map, int topKValue) {
        LinkedHashMap<String, Integer> linkedDurationMap = new LinkedHashMap<>();

        Collection<Integer> durationValues = map.values();
        ArrayList<Integer> list = new ArrayList<>(durationValues);
        Collections.sort(list); //sorting done in ascending natural ordering 
        Collections.reverse(list); //reverse the order of duration in the list, so that app is placed in descending order

        for (int i = 0; i < topKValue; i++) {
            Iterator<String> macAddressIter = map.keySet().iterator();
            if (i < list.size()) {
                double duration = list.get(i);
                while (macAddressIter.hasNext()) {
                    String macAddress = macAddressIter.next();
                    int storedDuration = map.get(macAddress);
                    if (storedDuration != 0 && storedDuration == duration) {
                        //compare if stored duration in map corresponds to the duration in the list 
                        linkedDurationMap.put(macAddress, storedDuration);
                        //remove the current macAddress after storing it in linkedDurationMap
                        macAddressIter.remove();
                    }
                }
            }
        }
        return linkedDurationMap;
    }

    /**
     * Assigns rank to the TopKApp based on the duration from the linked
     * duration map
     *
     * @param linkedDurationMap LinkedHashMap of macAddresses as key and duration as respective value
     * 
     * @return ArrayList<TopKStudent> ArrayList of TopKStudent 
     */
    public ArrayList<TopKStudent> topKStudentsSortByRank(LinkedHashMap<String, Integer> linkedDurationMap) {
        ArrayList<TopKStudent> sortedList = new ArrayList<>();
        Iterator<String> macAddressIter = linkedDurationMap.keySet().iterator(); 
        String curMacAddress = null;
        String name = null;
        int curDuration = 0;
        int rankCount = 1;

        List<Demographic> demographicList = demographicDAO.retrieveAll();
        while (macAddressIter.hasNext()) {

            curMacAddress = macAddressIter.next();
            int durationToCompare = linkedDurationMap.get(curMacAddress);
            
            //to account for first rank 
            if (rankCount == 1 && curDuration == 0) {
                curDuration = linkedDurationMap.get(curMacAddress);
            }
            
            //if current duration is not equivalent to the duration of comparison
            if (curDuration != durationToCompare) {
                rankCount = sortedList.size() + 1;
            }
            
            for (Demographic demo : demographicList) {
                String macAddress = demo.getMacAddress();
                if (curMacAddress.equals(macAddress)) {
                    name = demo.getName();
                    break;
                }
            }
            
            curDuration = durationToCompare;
            
            sortedList.add(new TopKStudent(rankCount, name, curMacAddress, curDuration));
        }
        return sortedList;
    }

    /**
     * Retrieves TopKSchool given start date, end date, app category and top K
     * value
     *
     * @param startDateTime the start date and time  of the input field 
     * @param endDateTime the end date and time  of the input field 
     * @param appCategory selected App Category from the list: (Books, Social, Education, Entertainment, Information, Library, Local, Tools, Fitness, Games, Others)
     * @param topKValue top K value chosen by user, where k can range from 1 to 10 (both inclusive, in integer increments)
     * 
     * @return ArrayList<TopKSchool> ArrayList of Top K Student 
     */
    public ArrayList<TopKSchool> retrieveTopKSchools(Date startDateTime, Date endDateTime, String appCategory, String topKValue) {

        int topKNum = Integer.parseInt(topKValue);

        List<App> appList = appDAO.retrieveAppByMacDate(startDateTime, endDateTime);
        HashMap<String, Integer> macMap = new HashMap<>(); //storing mac address as key, duration as value 

        if (appList.isEmpty()) {
            //checks if HQL query returns zero entry, will return empty list
            return new ArrayList<>();
        }
        int total = 0;

        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();
            
            //if app category matches the selected app category  
            if (appCategory.equals(curApp.getAppLookup().getCategory())) {
                GregorianCalendar curCalDay = new GregorianCalendar();

                curCalDay.setTime(curTimestamp);
                int curDay = curCalDay.get(GregorianCalendar.DAY_OF_MONTH);
                int curMonth = curCalDay.get(GregorianCalendar.MONTH);
                int curYear = curCalDay.get(GregorianCalendar.YEAR);

                curCalDay = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);
                
                //ensure retrieval of app is up to the last app to prevent indexOutOfBoundException
                if (i + 1 < appList.size()) {
                    App nextApp = appList.get(i + 1);
                    String nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
                    Date nextTimestamp = nextApp.getAppPK().getTimestamp();

                    GregorianCalendar nextDate = new GregorianCalendar();
                    nextDate.setTime(nextTimestamp);
                    int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                    int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                    int nextYear = nextDate.get(GregorianCalendar.YEAR);

                    nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                    int timeDiff = 0;
                    //if current app user is the same as the next app user 
                    if (curMacAddress.equals(nextMacAddress)) {

                        if (curCalDay.equals(nextDate)) {
                            timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                        } else {
                            timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                        }

                        if (timeDiff > 120) {
                            timeDiff = 10;
                        }

                        if (macMap.containsKey(curMacAddress)) {
                            timeDiff += macMap.get(curMacAddress);
                        }
                        macMap.put(curMacAddress, timeDiff);

                    } else {

                        nextDate = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);
                        nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                        timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                        if (timeDiff > 10) {
                            timeDiff = 10;
                        }

                        if (macMap.containsKey(curMacAddress)) {
                            timeDiff += macMap.get(curMacAddress);
                        }
                        macMap.put(curMacAddress, timeDiff);
                    }
                } else {
                    GregorianCalendar nextDate = new GregorianCalendar();
                    nextDate.setTime(curTimestamp);
                    int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                    int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                    int nextYear = nextDate.get(GregorianCalendar.YEAR);

                    nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                    nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                    int timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }

                    if (macMap.containsKey(curMacAddress)) {
                        timeDiff += macMap.get(curMacAddress);
                    }
                    macMap.put(curMacAddress, timeDiff);
                }
            }
        }
        HashMap<String, Integer> schoolMap = topKSchoolsSortByStudent(macMap); 
        LinkedHashMap<String, Integer> linkedDurationMap = topKSchoolsSortByDuration(schoolMap, topKNum);
        ArrayList<TopKSchool> topKSchoolList = topKSchoolsSortByRank(linkedDurationMap);
        //sort list in alphabetical order, especially for tie ranks
        Collections.sort(topKSchoolList);
        return topKSchoolList;
    }

    /**
     * Calculate total app usage duration of schools by the app usage duration
     * of the students from the school
     *
     * @param macMap HashMap of school name as key and app usage duration as respective value 
     * 
     * @return HashMap<String, Integer> Sorted HashMap of school name as key and duration as value 
     */
    public HashMap<String, Integer> topKSchoolsSortByStudent(HashMap<String, Integer> macMap) {

        HashMap<String, Integer> schoolMap = new HashMap<>();   //key = school name, integer = app usage duration for all students in the school
        Iterator<String> macAddressIter = macMap.keySet().iterator();
        //retrieve list of demographics to compare mac addresses with macMap to get email
        List<Demographic> demographicList = demographicDAO.retrieveAll();

        while (macAddressIter.hasNext()) {
            String macAddress = macAddressIter.next();
            int duration = macMap.get(macAddress);
            String email = "";
            for (Demographic d : demographicList) { //compare mac addresses to get email of student
                String curMacAddress = d.getMacAddress();
                if (curMacAddress.equals(macAddress)) {
                    email = d.getEmail();
                    break;
                }
            }

            String schoolName = "";
            String schoolInEmail = email.substring(email.indexOf("@"), email.indexOf(".", email.indexOf("@"))); //get school from email
            switch (schoolInEmail) {    //check if student is from which school based on the email
                case "@sis":
                    schoolName = "sis";
                    break;
                case "@economics":
                    schoolName = "economics";
                    break;
                case "@accountancy":
                    schoolName = "accountancy";
                    break;
                case "@business":
                    schoolName = "business";
                    break;
                case "@socsc":
                    schoolName = "socsc";
                    break;
                case "@law":
                    schoolName = "law";
            }
            if (schoolMap.containsKey(schoolName)) {
                duration += schoolMap.get(schoolName);  //add student's app usage duration to the school's total duration
            }
            schoolMap.put(schoolName, duration);
        }
        return schoolMap;
    }

    /**
     * Sorts schools based on the app usage duration up to the top K value
     *
     * @param schoolMap HashMap sorted based on school 
     * @param topKNum Top K value input by user 
     * 
     * @return LinkedHashMap<String, Integer> LinkedHashMap of sorted app based on school 
     */
    public LinkedHashMap<String, Integer> topKSchoolsSortByDuration(HashMap<String, Integer> schoolMap, int topKNum) {
        LinkedHashMap<String, Integer> linkedDurationMap = new LinkedHashMap<>();

        Collection<Integer> durationValues = schoolMap.values();
        ArrayList<Integer> list = new ArrayList<>(durationValues);
        Collections.sort(list); //sorting done in ascending natural ordering 
        Collections.reverse(list); //reverse the order of duration in the list, so that app is placed in descending order

        if (topKNum > list.size()) {
            topKNum = list.size(); //assigning the topKNum to the max number of the list 
        }

        for (int i = 0; i < topKNum; i++) {
            Iterator<String> schoolMapIter = schoolMap.keySet().iterator();
            int duration = list.get(i);
            while (schoolMapIter.hasNext()) {
                String school = schoolMapIter.next();
                int storedDuration = schoolMap.get(school);
                if (storedDuration != 0 && storedDuration == duration) {
                    //compare if stored duration in map corresponds to the duration in the list 
                    linkedDurationMap.put(school, storedDuration);
                    //remove the current school after storing it in linkedDurationMap
                    schoolMapIter.remove();
                }
            }
        }
        return linkedDurationMap;
    }

    /**
     * Assigns rank to the TopKSchool based on the duration from the linked
     * duration map
     *
     * @param linkedDurationMap LinkedHashMap of school as key and total app duration as the respective value 
     * 
     * @return ArrayList<TopKSchool> ArrayList of TopKSchool 
     */
    public ArrayList<TopKSchool> topKSchoolsSortByRank(LinkedHashMap<String, Integer> linkedDurationMap) {
        
        ArrayList<TopKSchool> sortedList = new ArrayList<>();
        Iterator<String> schoolIter = linkedDurationMap.keySet().iterator();
        String school = null;
        int curDuration = 0;
        int rankCount = 1;

        while (schoolIter.hasNext()) {
            
            school = schoolIter.next();
            int durationToCompare = linkedDurationMap.get(school);
            
            //account for the first rank 
            if(rankCount == 1 && curDuration == 0) {
                curDuration = linkedDurationMap.get(school);
            }
            
            //if current duration is not equivalent to the duration of comparison
            if (curDuration != durationToCompare) {
                rankCount = sortedList.size() + 1;
            }
            
            curDuration = durationToCompare;
            
            sortedList.add(new TopKSchool(rankCount, school, curDuration));
        }
        return sortedList;
    }
}
