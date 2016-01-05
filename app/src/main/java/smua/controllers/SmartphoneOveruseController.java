/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.AppDAO;
import smua.models.DAO.DemographicDAO;
import smua.models.Entities.App;
import smua.models.JSON.SmartphoneOveruse;

/**
 *
 * @author shtang.2014
 */
@Controller
public class SmartphoneOveruseController {

    @Autowired
    DemographicDAO demographicDAO;

    @Autowired
    AppDAO appDAO;

    /**
     * Retrieve total smartphone usage duration on a daily basis based on the logged-in user data only 
     *
     * @param startDate start date of the input field 
     * @param endDate end date of the input field 
     * @param email email of the logged in user 
     * @return SmartphoneOveruse object 
     */
    public SmartphoneOveruse retrieveSmartphoneOveruse(Date startDate, Date endDate, String email) {

        //calculate the number of days based on startdate and enddate 
        int numOfDays = (int) Math.ceil((endDate.getTime() - startDate.getTime()) / (1000.0 * 24 * 60 * 60));
        //retrieve the list of app based on startdate, enddate and email of the user 
        List<App> appList = appDAO.retrieveSmartphoneOveruse(startDate, endDate, email);

        //Calculation of average daily smartphone usage duration
        int total = 0;
        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();

            GregorianCalendar curDate = new GregorianCalendar();

            //set Gregorian calendar date to current app date and subsequently retrieve the day, month and year 
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
                int nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextAppYear = nextDate.get(GregorianCalendar.YEAR);

                nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, 0, 0, 0);
                int timeDiff = 0;

                //check if same user for two consecutive apps
                if (curMacAddress.equals(nextMacAddress)) {

                    //check if current date of the first app is the same as the next app date 
                    if (curDate.equals(nextDate)) {
                        timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                    } else {
                        timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    }

                    //if time difference is more than 120 seconds, assign it as 10 seconds 
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }

                    //add time difference to the total time 
                    total += timeDiff;
                
                //if different users 
                } else {
                    nextDate = new GregorianCalendar(curYear, curMonth, curDay, 0, 0, 0);
                    //assign the next date of the subsequent app as the day after the current date
                    nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                    if (curDate.equals(nextDate)) {
                        timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                    } else {

                        timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    }

                    //if time difference is more than 120 seconds, assign it as 10 seconds
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }
                    //add time difference to the total time 
                    total += timeDiff;

                }
            } else {
                GregorianCalendar nextDate = new GregorianCalendar();
                //set the nextDate Gregorian calendar as the current timestamp 
                nextDate.setTime(curTimestamp);
                int nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextAppYear = nextDate.get(GregorianCalendar.YEAR);

                nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, 0, 0, 0);
                 //assign the next date of the subsequent app as the day after the current date
                nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                int timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                //if time difference is more than 120 seconds, assign it as 10 seconds
                if (timeDiff > 120) {
                    timeDiff = 10;
                }
                //add time difference to the total time 
                total += timeDiff;
            }
        }

        double usageDuration = (1.0 * total);
        //calculate average usage duration based on the number of days calculated 
        double averageUsageDuration = Math.round(usageDuration / (1.0 * numOfDays));

        double gamingDuration = retrieveGamingDuration(appList);
        //calculate average gaming duration hours based on the number of days calculated 
        double averageGamingDuration = Math.round(gamingDuration / (1.0 * numOfDays));

        double accessFrequency = retrieveAccessFrequency(appList);
        double averageAccessFrequency = 1.0 * accessFrequency / 24 / numOfDays;
        averageAccessFrequency = Math.round(averageAccessFrequency * 100) / 100.0;

        HashMap<String, Double> metrics = new HashMap<>();

        double gamingCategory = 0;

        //categorise gaming duration to specific numbering to be later interpreted in the jsp page 
        //average gaming duration in seconds 
        if (averageGamingDuration < 3600) {
            //3600 seconds = 1 hour 
            gamingCategory = 0;
        } else if (averageGamingDuration < 7200) {
            //7200 seconds = 2 hours 
            gamingCategory = 1;
        } else {
            gamingCategory = 2;
        }

        metrics.put("gamingCategory", gamingCategory);
        metrics.put("gamingDuration", averageGamingDuration);

        double usageCategory = 0;

        //categorise average usage duration to specific numbering to be later interpreted in the jsp page 
        //average usage duration in seconds 
        if (averageUsageDuration < 10800) {
            //10800 seconds = 3 hours 
            usageCategory = 0;
        } else if (averageUsageDuration < 18000) {
            //18000 seconds = 5 hours 
            usageCategory = 1;
        } else {
            usageCategory = 2;
        }

        metrics.put("usageCategory", usageCategory);
        metrics.put("usageDuration", averageUsageDuration);

        double accessFrequencyCategory = 0;

        //categorise average access frequency to specific numbering to be later interpreted in the jsp page 
        if (averageAccessFrequency < 3) {
            accessFrequencyCategory = 0;
        } else if (averageAccessFrequency < 5) {
            accessFrequencyCategory = 1;
        } else {
            accessFrequencyCategory = 2;
        }

        metrics.put("accessFrequencyCategory", accessFrequencyCategory);
        metrics.put("accessFrequency", averageAccessFrequency);

        String overuseIndex = "";

        //categorise the overall overuse index based on the assigned index of the three individual metrics 
        if (gamingCategory == 2 || usageCategory == 2 || accessFrequencyCategory == 2) {
            overuseIndex = "Overusing";
        } else if (gamingCategory == 0 && usageCategory == 0 && accessFrequencyCategory == 0) {
            overuseIndex = "Normal";
        } else {
            overuseIndex = "ToBeCautious";
        }
        
        SmartphoneOveruse smartphoneOveruse = new SmartphoneOveruse(overuseIndex, metrics);

        return smartphoneOveruse;
    }

    /**
     * Retrieve total gaming duration in seconds from games app category for the
     * specific logged-in user
     *
     * @param appList List of App 
     * @return int total gaming duration of game app 
     */
    public int retrieveGamingDuration(List<App> appList) {

        int total = 0;
        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            //retrieve category, macaddress and timestamp of the current app 
            String category = curApp.getAppLookup().getCategory();
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();

            //check if game app category 
            if (category.equals("games")) {
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
                    int nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                    int nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
                    int nextAppYear = nextDate.get(GregorianCalendar.YEAR);

                    nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, 0, 0, 0);
                    int timeDiff = 0;
                    
                    //check if same user for two consecutive apps
                    if (curDate.equals(nextDate)) {
                        timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);
                    } else {
                        timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    }

                    //if time difference is more than 120 seconds, assign it as 10 seconds
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }
                    
                    //add time difference to the total time 
                    total += timeDiff;

                //account for last app in the list to see if it is game category 
                } else if (category.equals("games")) {
                    GregorianCalendar nextDate = new GregorianCalendar();
                    nextDate.setTime(curTimestamp);
                    int nextDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                    int nextMonth = nextDate.get(GregorianCalendar.MONTH);
                    int nextYear = nextDate.get(GregorianCalendar.YEAR);

                    nextDate = new GregorianCalendar(nextYear, nextMonth, nextDay, 0, 0, 0);
                    nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);

                    int timeDiff = (int) ((nextDate.getTime().getTime() - curTimestamp.getTime()) / 1000);
                    
                    //if time difference is more than 120 seconds, assign it as 10 seconds
                    if (timeDiff > 120) {
                        timeDiff = 10;
                    }

                    //add time difference to the total time 
                    total += timeDiff;

                }
            }
        }
        return total;
    }

    /**
     * Retrieve total number of times the specific user has used the smartphone
     * on an hourly basis
     *
     * @param appList List of App 
     * @return int access frequency count, where it is the number of ‘phone use sessions’ per hour
     */
    public double retrieveAccessFrequency(List<App> appList) {

        double counter = 0;

        for (int i = 0; i < appList.size(); i++) {
            App curApp = appList.get(i);
            //retrieve category, macaddress and timestamp of the current app 
            String category = curApp.getAppLookup().getCategory();
            String curMacAddress = curApp.getAppPK().getDemographic().getMacAddress();
            Date curTimestamp = curApp.getAppPK().getTimestamp();

            GregorianCalendar curDate = new GregorianCalendar();

            curDate.setTime(curTimestamp);
            int curDay = curDate.get(GregorianCalendar.DAY_OF_MONTH);
            int curMonth = curDate.get(GregorianCalendar.MONTH);
            int curYear = curDate.get(GregorianCalendar.YEAR);
            int curHour = curDate.get(GregorianCalendar.HOUR_OF_DAY);

            curDate = new GregorianCalendar(curYear, curMonth, curDay, curHour, 0, 0);

            //ensure retrieval of app is up to the last app to prevent indexOutOfBoundException
            if (i + 1 < appList.size()) {
                App nextApp = appList.get(i + 1);
                String nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
                Date nextTimestamp = nextApp.getAppPK().getTimestamp();

                GregorianCalendar nextDate = new GregorianCalendar();
                nextDate.setTime(nextTimestamp);
                int nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                int nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
                int nextAppYear = nextDate.get(GregorianCalendar.YEAR);
                int nextAppHour = nextDate.get(GregorianCalendar.HOUR_OF_DAY);

                nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, nextAppHour, 0, 0);
                int timeDiff = 0;

                //check if current app date is equal to the next app date 
                if (curDate.equals(nextDate)) {

                    timeDiff = (int) ((nextTimestamp.getTime() - curTimestamp.getTime()) / 1000);

                    //if time difference is more than 120 seconds, assign it as 10 seconds
                    //since a phone use session is defined as a group of continuous interactions with apps where the interactions are within 2 minutes apart
                    //and above 120 seconds, means an additional count of access frequency 
                    if (timeDiff > 120) {
                        counter++;
                    }
                } else {
                   //add counter if different date for the two app 
                    counter++;
                }
            } else {
                //account for the last app of the list 
                counter++;
            }
        }
        return counter;

    }
}
