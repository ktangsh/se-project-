/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.AppDAO;
import smua.models.DAO.AppLookupDAO;
import smua.models.DAO.DemographicDAO;
import smua.models.Entities.App;
import smua.models.Entities.AppLookup;
import smua.models.Entities.Demographic;
import smua.models.JSON.BasicAppUsage;
import smua.models.PK.AppPK;

/**
 *
 * @author ChuQian
 */
@Controller
public class BasicAppController {

    @Autowired
    AppDAO appDAO;

    @Autowired
    DemographicDAO demoDAO;

    @Autowired
    AppLookupDAO appLookUpDAO;

    private int minYear;
    private int maxYear;
    private int totalUniqueMacAddress;
    private ArrayList<String> ccaList;
    private String startDate;
    private String endDate;

    /**
     * This method takes in List of App and categorise them into different usage
     * intensities
     * @param numOfDays Number of days in the period 
     * @param appList List of App 
     * @return an ArrayList of LinkedHashMap<String, Integer> with macAddresses as the unique key and the average time for respective user 
     */
    public ArrayList<LinkedHashMap<String, Integer>> usageCategorizer(List<App> appList, int numOfDays) {

        LinkedHashMap<String, Integer> usageTimeHashMap = new LinkedHashMap<String, Integer>();
        double totalTimeForCurUser = 0; //total time per user
        for (int i = 0; i < appList.size(); i++) {

            App firstApp = appList.get(i);
            App nextApp = null;
            String nextMacAddress = null;
            //retrieve the next app till the end of appList to prevent indexOutOfBoundException
            if (i + 1 < appList.size()) {
                nextApp = appList.get(i + 1);
                nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
            }
            //Calculate the app usage time between two subsequent app
            int curAppUsageTime = calculateAppUsageTime(firstApp, nextApp);
            String curMacAddress = firstApp.getAppPK().getDemographic().getMacAddress();

            //check if same macAddress, i.e. same user 
            if (curMacAddress.equals(nextMacAddress)) {
                totalTimeForCurUser += curAppUsageTime;
            } else {
                totalTimeForCurUser += curAppUsageTime;
                usageTimeHashMap.put(curMacAddress, (int) (Math.ceil(totalTimeForCurUser / numOfDays)));
                //reset the total time per user to zero 
                totalTimeForCurUser = 0;
            }

        }

        //category-intense,mild,normal int: no of users
        //Returns HashMap of usage intensities and the count of users
        HashMap<String, Integer> categoryHashMap = retrieveUsageCategory(usageTimeHashMap);

        //Initialise LinkedHashMap for each usage category 
        LinkedHashMap<String, Integer> intenseHashMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> normalHashMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> mildHashMap = new LinkedHashMap<>();
        int intenseCount = categoryHashMap.get("Intense");
        intenseHashMap.put("intense-count", intenseCount);
        intenseHashMap.put("intense-percent", (int) (Math.round(100.0 * intenseCount / totalUniqueMacAddress)));

        int normalCount = categoryHashMap.get("Normal");
        normalHashMap.put("normal-count", normalCount);
        normalHashMap.put("normal-percent", (int) (Math.round(100.0 * normalCount / totalUniqueMacAddress)));

        int mildCount = categoryHashMap.get("Mild");
        mildHashMap.put("mild-count", mildCount);
        mildHashMap.put("mild-percent", (int) (Math.round(100.0 * mildCount / totalUniqueMacAddress)));

        ArrayList<LinkedHashMap<String, Integer>> resultArrayListMap = new ArrayList<>();
        resultArrayListMap.add(intenseHashMap);
        resultArrayListMap.add(normalHashMap);
        resultArrayListMap.add(mildHashMap);

        return resultArrayListMap;
    }

    /**
     * This method takes in BasicAppUsage and List of App It calculates the
     * number of unique users in the appList provided
     *
     * @param appList List of App
     * @return the number of unique users 
     */
    public int countUniqueUsers(List<App> appList) {
        //get number of unique mac address
        HashMap<String, String> usersMap = new HashMap<String, String>();
        for (App app : appList) {
            Demographic demo = app.getAppPK().getDemographic();
            String macAddress = demo.getMacAddress();
            usersMap.put(macAddress, "unique");
        }
        return usersMap.size();
    }

    /**
     * This method sorts the appList into the various CCAs and create a
     * respective sub-category within the BasicAppUsage object.
     *
     * @param basicAppUsage BasicAppUsage object
     * @param appList List of App
     * @param curLevel Current level of categorisation
     * @param maxLevel Maximum level of categorisation
     * @param numOfDays Number of Days based on start date and end date
     */
    public void sortByCCA(BasicAppUsage basicAppUsage, List<App> appList, int curLevel, int maxLevel, int numOfDays) {

        if (basicAppUsage.getBasicAppUsageList() == null) {
            List<BasicAppUsage> basicAppUsageList = new ArrayList<>();
            //ensure cca list is sorted in alphabetical order
            Collections.sort(ccaList);
            Iterator<String> ccaIter = ccaList.iterator();
            while (ccaIter.hasNext()) {
                String key = ccaIter.next();
                List<App> ccaAppList = new ArrayList<>();
                BasicAppUsage ccaAppUsage = new BasicAppUsage();
                for (App app : appList) {
                    Demographic demo = app.getAppPK().getDemographic();
                    String curCCA = demo.getCca();
                    if (key.equals(curCCA)) {
                        ccaAppList.add(app);
                    }
                }

                ccaAppUsage.setAppList(ccaAppList);
                ccaAppUsage.setCategory("cca");
                ccaAppUsage.setSortCategory(key);
                int count = countUniqueUsers(ccaAppList);
                ccaAppUsage.setCount(count);
                ccaAppUsage.setPercentage((int) Math.round(100.0 * count / totalUniqueMacAddress));
                basicAppUsageList.add(ccaAppUsage);
                if (curLevel == maxLevel) {
                    ArrayList<LinkedHashMap<String, Integer>> ccaUsageCategorizer = usageCategorizer(ccaAppList, numOfDays);
                    ccaAppUsage.setCategoryListResult(ccaUsageCategorizer);
                }
            }
            basicAppUsage.setBasicAppUsageList(basicAppUsageList);

        } else {
            List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
            for (BasicAppUsage curBasicAppUsage : basicAppUsageList) {
                List<App> curAppList = curBasicAppUsage.getAppList();
                sortByCCA(curBasicAppUsage, curAppList, curLevel, maxLevel, numOfDays);
            }
        }
    }

    /**
     * This method sort the appList into the various school and create a
     * respective sub-category within the BasicAppUsage object.
     *
     * @param basicAppUsage BasicAppUsage object
     * @param appList List of App
     * @param curLevel Current level of categorisation
     * @param maxLevel Maximum level of categorisation
     * @param numOfDays Number of Days based on start date and end date 
     */
    public void sortBySchool(BasicAppUsage basicAppUsage, List<App> appList, int curLevel, int maxLevel, int numOfDays) {
        if (basicAppUsage.getBasicAppUsageList() == null) {
            List<BasicAppUsage> basicAppUsageList = new ArrayList<>();

            String[] school = {"business", "socsc", "sis", "economics", "accountancy", "law"};
            //Convert array into arrayList and sort them in alphabetical order 
            ArrayList<String> schoolList = new ArrayList<String>(Arrays.asList(school));
            Collections.sort(schoolList);
            for (int i = 0; i < schoolList.size(); i++) {
                BasicAppUsage schoolAppUsage = new BasicAppUsage();
                List<App> schoolAppList = new ArrayList<App>();
                for (App app : appList) {
                    Demographic demo = app.getAppPK().getDemographic();
                    String email = demo.getEmail();
                    if (email.indexOf(school[i]) > -1) {
                        schoolAppList.add(app);
                    }
                }

                schoolAppUsage.setAppList(schoolAppList);
                schoolAppUsage.setCategory("school");
                schoolAppUsage.setSortCategory(school[i]);
                int count = countUniqueUsers(schoolAppList);

                schoolAppUsage.setCount(count);
                schoolAppUsage.setPercentage((int) Math.round(100.0 * count / totalUniqueMacAddress));
                basicAppUsageList.add(schoolAppUsage);
                if (curLevel == maxLevel) {
                    ArrayList<LinkedHashMap<String, Integer>> schoolUsageCategorizer = usageCategorizer(schoolAppList, numOfDays);
                    schoolAppUsage.setCategoryListResult(schoolUsageCategorizer);
                }

            }
            basicAppUsage.setBasicAppUsageList(basicAppUsageList);

        } else {
            List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
            for (BasicAppUsage curBasicAppUsage : basicAppUsageList) {
                List<App> curAppList = curBasicAppUsage.getAppList();
                sortBySchool(curBasicAppUsage, curAppList, curLevel, maxLevel, numOfDays);
            }
        }

    }

    /**
     * Sort the appList into the various year and creates the respective
     * sub-category within the BasicAppUsage Object
     *
     * @param basicAppUsage BasicAppUsage object
     * @param appList List of App
     * @param curLevel Current level of categorisation
     * @param maxLevel Maximum level of categorisation 
     */
    public void sortByYear(BasicAppUsage basicAppUsage, List<App> appList, int curLevel, int maxLevel, int numOfDays) {
        if (basicAppUsage.getBasicAppUsageList() == null) {
            List<BasicAppUsage> basicAppUsageList = new ArrayList<>();

            for (int i = minYear; i <= maxYear; i++) {
                BasicAppUsage yearAppUsage = new BasicAppUsage();
                List<App> yearAppList = new ArrayList<>();
                for (App app : appList) {
                    Demographic demo = app.getAppPK().getDemographic();
                    String email = demo.getEmail();
                    if (email.indexOf("" + i) > -1) {
                        yearAppList.add(app);
                    }
                }

                yearAppUsage.setAppList(yearAppList);
                yearAppUsage.setCategory("year");
                yearAppUsage.setSortCategory(i + "");
                //if there are no previous levels

                basicAppUsageList.add(yearAppUsage);
            }

            for (int i = 0; i < basicAppUsageList.size(); i++) {

                BasicAppUsage curBasicAppUsage = basicAppUsageList.get(i);
                List<App> curAppList = curBasicAppUsage.getAppList();
                int count = countUniqueUsers(curAppList);
                curBasicAppUsage.setPercentage((int) Math.round(100.0 * count / totalUniqueMacAddress));
                curBasicAppUsage.setCount(count);
                if (curLevel == maxLevel) {
                    ArrayList<LinkedHashMap<String, Integer>> yearUsageCategorizer = usageCategorizer(curAppList, numOfDays);
                    curBasicAppUsage.setCategoryListResult(yearUsageCategorizer);
                }
            }

            basicAppUsage.setBasicAppUsageList(basicAppUsageList);
        } else {
            List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
            for (BasicAppUsage curBasicAppUsage : basicAppUsageList) {
                List<App> curAppList = curBasicAppUsage.getAppList();
                sortByYear(curBasicAppUsage, curAppList, curLevel, maxLevel, numOfDays);
            }
        }
    }

    /**
     * This method sorts the appList into various gender and create a respective
     * sub-category within the BasicAppUsage object.
     *
     * @param basicAppUsage BasicAppUsage object 
     * @param appList List of app 
     * @param curLevel Current level of categorisation 
     * @param maxLevel Maximum level of categorisation 
     */
    public void sortByGender(BasicAppUsage basicAppUsage, List<App> appList, int curLevel, int maxLevel, int numOfDays) {

        BasicAppUsage maleAppUsage = null;
        BasicAppUsage femaleAppUsage = null;
        //check if its first level of categorisation
        //otherwise retrieve the basicAppUsageList that has been previously initialised 
        if (basicAppUsage.getBasicAppUsageList() == null) {
            maleAppUsage = new BasicAppUsage();
            femaleAppUsage = new BasicAppUsage();
            List<App> maleAppList = new ArrayList<>();
            List<App> femaleAppList = new ArrayList<>();
            //sorting to male and female
            for (App app : appList) {
                Demographic demographic = app.getAppPK().getDemographic();
                if (demographic.getGender() == 'm') {
                    maleAppList.add(app);
                } else {
                    femaleAppList.add(app);
                }
            }

            //setting BasicAppUsage attributes
            maleAppUsage.setCategory("gender");
            maleAppUsage.setSortCategory("M");
            femaleAppUsage.setCategory("gender");
            femaleAppUsage.setSortCategory("F");
            maleAppUsage.setAppList(maleAppList);
            femaleAppUsage.setAppList(femaleAppList);

            List<BasicAppUsage> basicAppUsageList = new ArrayList<>();
            basicAppUsageList.add(femaleAppUsage);
            basicAppUsageList.add(maleAppUsage);
            basicAppUsage.setBasicAppUsageList(basicAppUsageList);

            int maleCount = countUniqueUsers(maleAppList);
            maleAppUsage.setCount(maleCount);
            //set the male app usage based on the total number of mac addresses
            maleAppUsage.setPercentage((int) Math.round(100.0 * maleCount / totalUniqueMacAddress));

            int femaleCount = countUniqueUsers(femaleAppList);
            femaleAppUsage.setCount(femaleCount);
            //set the female app usage based on the total number of mac addresses
            femaleAppUsage.setPercentage((int) Math.round(100.0 * femaleCount / totalUniqueMacAddress));
            
            //check if current level of categorisation reaches the maximum level 
            if (curLevel == maxLevel) {
                //store the result into the arrayList of LinkedHashMap of macAddresses and average number of time 
                ArrayList<LinkedHashMap<String, Integer>> maleUsageCategorizer = usageCategorizer(maleAppList, numOfDays);
                ArrayList<LinkedHashMap<String, Integer>> femaleUsageCategorizer = usageCategorizer(femaleAppList, numOfDays);
                maleAppUsage.setCategoryListResult(maleUsageCategorizer);
                femaleAppUsage.setCategoryListResult(femaleUsageCategorizer);
            }
        } else {
            List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
            for (BasicAppUsage curBasicAppUsage : basicAppUsageList) {
                List<App> curAppList = curBasicAppUsage.getAppList();
                //sort by gender if app list had been previously sorted 
                sortByGender(curBasicAppUsage, curAppList, curLevel, maxLevel, numOfDays);
            }
        }
    }

    /**
     * This method returns an output of BasicAppUsage given the order of which
     * it is to be broken down into.
     *
     * @param startDate Input start date in the format of yyyy/mm/dd
     * @param endDate Input end date in the format of yyyy/mm/dd
     * @param categories Input categories as Gender, Year, School or CCA 
     * @return a BasicAppUsage object 
     */
    public BasicAppUsage retrieveAppUsageByDemographicBreakdown(String startDate, String endDate, String categories) {
        startDate += " 00:00:00";
        endDate += " 23:59:59";

        this.startDate = startDate;
        this.endDate = endDate;

        Date startDateTime = null;
        Date endDateTime = null;
        int numOfDays = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);
            
            //calculate the number of days based on the milliseconds difference between endDateTime and startDateTime 
            double numDaysInDouble = (endDateTime.getTime() - startDateTime.getTime()) / (1000.0 * 60 * 60 * 24);
            numOfDays = (int) Math.ceil(numDaysInDouble);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        
        //check if enddate is before startdate 
        //if true, return null
        if (endDateTime.before(startDateTime)) {
            return null;
        }
        
        int counter = 0;
        minYear = 0;
        maxYear = 0;
        
        //retrieve all years in demographicDAO 
        List<Demographic> demoList = demoDAO.retrieveAll();
        HashMap<String, String> uniqueCCA = new HashMap<String, String>();
        for (Demographic demographic : demoList) {
            String email = demographic.getEmail();
            String cca = demographic.getCca();
            //retrieve the email ID of the user based on the user's email address 
            int year = Integer.parseInt(email.substring(email.indexOf("@") - 4, email.indexOf("@")));
            uniqueCCA.put(cca, "unique");
            if (counter == 0) {
                minYear = year;
                maxYear = year;
                counter++;
            }

            if (year > maxYear) {
                maxYear = year;
            }

            if (year < minYear) {
                minYear = year;
            }

        }
        //assign all cca's to global variable
        ccaList = new ArrayList<>(uniqueCCA.keySet());
        String[] categoryOrder = categories.split(",");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (endDateTime.before(startDateTime)) {
            return null;
        }
        BasicAppUsage basicAppUsage = new BasicAppUsage();
        //retrieve all apps based on the startDateTime and endDateTime
        List<App> appList = appDAO.retrieveAppByStartEndDate(startDateTime, endDateTime);

        //get number of unique mac address
        HashMap<String, String> usersMap = new HashMap<String, String>();
        for (App app : appList) {
            Demographic demo = app.getAppPK().getDemographic();
            String macAddress = demo.getMacAddress();
            usersMap.put(macAddress, "unique");
        }
        
        //update the class variable totalUniqueMacAddress on the number of unique mac addresses
        totalUniqueMacAddress = usersMap.size();
        int maxLevel = categoryOrder.length;
        int curLevel = 1;
        for (String category : categoryOrder) {
            //sorting based on the category order as input by the user 
            //until the current level of categorisation reaches the maximum level
            if (category.equalsIgnoreCase("Gender")) {
                sortByGender(basicAppUsage, appList, curLevel, maxLevel, numOfDays);
            } else if (category.equalsIgnoreCase("Year")) {
                sortByYear(basicAppUsage, appList, curLevel, maxLevel, numOfDays);
            } else if (category.equalsIgnoreCase("School")) {
                sortBySchool(basicAppUsage, appList, curLevel, maxLevel, numOfDays);
            } else if (category.equalsIgnoreCase("CCA")) {
                sortByCCA(basicAppUsage, appList, curLevel, maxLevel, numOfDays);
            }
            curLevel++;
        }

        return basicAppUsage;
    }

    /**
     * This method returns a HashMap of mac addresses and their respective app
     * usage time
     *
     * @param startDate Input start date in the format of yyyy/mm/dd
     * @param endDate Input end date in the format of yyyy/mm/dd
     * @return HashMap<String, Integer> with mac addresses as the unique key and app usage time as the respective value 
     */
    public HashMap<String, Integer> retrieveAppByStartEndDate(String startDate, String endDate) {
        startDate += " 00:00:00";
        endDate += " 23:59:59";

        Date startDateTime = null;
        Date endDateTime = null;
        int numberOfDay = 0;

        //Macadress, timeUsage
        HashMap<String, Integer> usageTimeHashMap = new HashMap<String, Integer>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);

            double numDaysInDouble = (endDateTime.getTime() - startDateTime.getTime()) / (1000.0 * 60.0 * 60 * 24);
            numberOfDay = (int) (Math.ceil(numDaysInDouble)); //round up to int

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        if (endDateTime.before(startDateTime)) {
            return null;
        }

        List<App> appList = appDAO.retrieveAppByStartEndDate(startDateTime, endDateTime);

        double totalTimeForCurUser = 0; //total time per user

        for (int i = 0; i < appList.size(); i++) {
            App firstApp = appList.get(i);
            App nextApp = null;
            String nextMacAddress = null;
            if (i + 1 < appList.size()) {
                nextApp = appList.get(i + 1);
                nextMacAddress = nextApp.getAppPK().getDemographic().getMacAddress();
            }
            int curAppUsageTime = calculateAppUsageTime(firstApp, nextApp);
            String curMacAddress = firstApp.getAppPK().getDemographic().getMacAddress();

            if (curMacAddress.equals(nextMacAddress)) {
                totalTimeForCurUser += curAppUsageTime;

            } else {
                totalTimeForCurUser += curAppUsageTime;
                usageTimeHashMap.put(curMacAddress, (int) (Math.ceil(totalTimeForCurUser / numberOfDay)));
                totalTimeForCurUser = 0;
            }

        }

        return usageTimeHashMap;
    }

    /**
     * This method returns a HashMap of usage intensities and the count of users
     * that fall into that category.
     *
     * @param usageTimeByStartDate HashMap of macAddress as the unique key and usage time as the respective value
     * @return HashMap<String, Integer> with usage intensities (intense, normal or mild) as the key and the count of users as the respective value
     */
    public HashMap<String, Integer> retrieveUsageCategory(HashMap<String, Integer> usageTimeByStartDate) {
        HashMap<String, Integer> userCategoryHashMap = new HashMap<String, Integer>();
        int intenseCount = 0;
        int normalCount = 0;
        int mildCount = 0;

        if (usageTimeByStartDate != null) {
            for (String macAddress : usageTimeByStartDate.keySet()) {
                int usageTime = usageTimeByStartDate.get(macAddress); //usagetime in seconds

                double usageHour = usageTime / 3600.0; //convert into hours
                
                //if usage hour is more than or equal to 5 hours 
                if (usageHour >= 5) {
                    intenseCount++;
                //if usage hour is between 1 and 5 hours inclusive 
                } else if (usageHour >= 1 && usageHour < 5) {
                    normalCount++;
                //if usqage hour is less than 1 hour 
                } else {
                    mildCount++;
                }
            }

            //categorisation of usage time count based on the number of hours 
            //categorisation based on intense, normal or mild 
            userCategoryHashMap.put("Intense", intenseCount);
            userCategoryHashMap.put("Normal", normalCount);
            userCategoryHashMap.put("Mild", mildCount);
        }

        return userCategoryHashMap;
    }

    /**
     * This method returns a filtered list of apps given the respective filter
     * conditions e.g gender: "M", school: "SIS", year:"2015"
     *
     * @param appList List of App that is not filtered 
     * @param gender Gender input field as male (M) or female (F)
     * @param school School input field as sis, law, business, socsc, economics, accountancy
     * @param year Year input field as 2011, 2012, 2013, 2014 or 2015
     * @return list of App that matches the selected gender, school or year 
     */
    public List<App> diurnalUsageFilter(List<App> appList, String gender, String school, String year) {

        Iterator<App> iter = appList.iterator();
        while (iter.hasNext()) {
            boolean matches = true;
            App app = iter.next();
            Demographic demo = app.getAppPK().getDemographic();
            String email = demo.getEmail();
            String demoGender = demo.getGender() + "";
            String demoSchool = email.substring(email.indexOf("@") + 1, email.indexOf(".smu"));
            String demoYear = email.substring(email.indexOf("@") - 4, email.indexOf("@"));

            //if gender is specified and gender does not match
            if (gender != null && !gender.equalsIgnoreCase(demoGender)) {
                matches = false;
            }

            //if school is specified and school does not match
            if (school != null && !school.equalsIgnoreCase(demoSchool)) {
                matches = false;
            }

            //if year is specified and year does not match
            if (year != null && !year.equalsIgnoreCase(demoYear)) {
                matches = false;
            }

            if (!matches) {
                iter.remove();
            }
        }
        return appList;
    }

    /**
     * This method returns a LinkedHashMap of hourly durations and its
     * respective average usage in that hour.
     *
     * @param date Date input field in the format yyyy/mm/dd
     * @param gender Gender input field as male (M) or female (F)
     * @param school School input field as sis, law, business, socsc, economics, accountancy
     * @param year Year input field as 2011, 2012, 2013, 2014 or 2015
     * @return LinkedHashMap<String, Integer> with the hourly period time as the unique key and average usage time as the respective value
     */
    public LinkedHashMap<String, Integer> getBasicAppDiurnalUsage(String date, String gender, String school, String year) {
        LinkedHashMap<String, Integer> diurnalUsageOutput = new LinkedHashMap<>();
        if (gender == null || gender.equals("")) {
            gender = null;
        }
        if (school == null || school.equals("")) {
            school = null;
        }
        if (year == null || year.equals("")) {
            year = null;
        }
        String startDate = date + " 00:00:00";
        String endDate = date + " 23:59:59";
        String curDate = date + " 00:00:00";

        Date startDateTime = null;
        Date endDateTime = null;
        Date curDateTime = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);
            curDateTime = sdf.parse(curDate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        //retrieve the list of apps based on the input start time and end time 
        List<App> totalAppList = appDAO.retrieveAppByStartEndDate(startDateTime, endDateTime);
        HashMap<String, String> totalUniqueUsers = new HashMap<String, String>();

        //filter the list of app that matches the given gender, school or year 
        totalAppList = diurnalUsageFilter(totalAppList, gender, school, year);
        
        for (App app : totalAppList) {
            String macAddress = app.getAppPK().getDemographic().getMacAddress();
            totalUniqueUsers.put(macAddress, "unique");
        }

        int totalNumberOfUniqueUser = totalUniqueUsers.size();
        
        GregorianCalendar curCalendar = new GregorianCalendar();
        GregorianCalendar endCalendar = new GregorianCalendar();

        endCalendar.setTime(endDateTime);
        curCalendar.setTime(curDateTime);
        //setting the minutes and second under curCalendar as 59 
        curCalendar.add(GregorianCalendar.MINUTE, 59);
        curCalendar.add(GregorianCalendar.SECOND, 59);
        
        HashMap<Date, List<App>> timeAppUsageMap = new HashMap<Date, List<App>>();
        
        for(App app: totalAppList) {
            Date curTimestamp = app.getAppPK().getTimestamp();
            
            GregorianCalendar curTimeCalendar = new GregorianCalendar();
            curTimeCalendar.setTime(curTimestamp);
            curTimeCalendar.set(GregorianCalendar.MINUTE,0);
            curTimeCalendar.set(GregorianCalendar.SECOND,0);
            Date curCalDate = curTimeCalendar.getTime();

            //check if hashmap contains the unique curCalDate
            if(timeAppUsageMap.containsKey(curCalDate)) {
                List<App> appList = timeAppUsageMap.get(curCalDate);
                appList.add(app);
            }else {
                List<App> appList = new ArrayList<App>();
                appList.add(app);
                timeAppUsageMap.put(curCalDate, appList);
            }
        }

        curCalendar.setTime(curDateTime);
        endCalendar.setTime(curDateTime);
        endCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
        
        //check if curCalendar time is before endDateTime 
        while(curCalendar.getTime().before(endDateTime)) {
            
            List<App> curAppList = timeAppUsageMap.get(curCalendar.getTime());
            int totalTime = 0;
            
            if(curAppList != null) {
                for(int i = 0; i < curAppList.size(); i++) {
                    App curApp = curAppList.get(i);
                    if(i+1 < curAppList.size()) {
                        App nextApp = curAppList.get(i+1);
                        totalTime += calculateAppUsageTime(curApp, nextApp);
                    }else {
                        //accounts for the last app record in the particular period 
                        long timeDiff = (endCalendar.getTime().getTime() - curApp.getAppPK().getTimestamp().getTime())/1000;
                   
                        if(timeDiff > 10) {
                            timeDiff = 10;
                        }
                        
                        totalTime += timeDiff;
                    }
                }
            }
            
            
            String hour = curCalendar.get(GregorianCalendar.HOUR_OF_DAY)+"";
            if(hour.length() < 2) {
                hour = "0" +hour;
            }
            String minute = curCalendar.get(GregorianCalendar.MINUTE)+"";
            if(minute.length() < 2) {
                minute = "0" +minute;
            }
            
            String strTimePeriodTime =  hour+":"+minute;

            hour = endCalendar.get(GregorianCalendar.HOUR_OF_DAY)+"";
            if(hour.length() < 2) {
                hour = "0" +hour;
            }
            minute = endCalendar.get(GregorianCalendar.MINUTE)+"";
            if(minute.length() < 2) {
                minute = "0" +minute;
            }
            
            
            strTimePeriodTime +=  "-" + hour + ":" + minute;
            diurnalUsageOutput.put(strTimePeriodTime, (int)Math.round(totalTime*1.0/totalNumberOfUniqueUser));
            
            //add one hour for both endCalendar and curCalendar at the end of the loop 
            endCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
            curCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
        }
      
        return diurnalUsageOutput;
    }

    /**
     * This method will take the difference between two app usage time and return the calculation of the difference
     * while checking if the two app usage belongs to the same user (i.e. same macaddress) and verify if they are recorded within the same day 
     * E.g Duration in seconds for app_1 =
     * calculateAppUsageTime(app_1, app_2) where app_2 is the next app usage.
     *
     * @param app1 First App input
     * @param app2 Second App input
     * @return calculated time difference between the two app
     */
    public int calculateAppUsageTime(App app1, App app2) {

        int usageTime = 0;

        //for first app usage
        AppPK appPK1 = app1.getAppPK();
        String macAddress1 = appPK1.getDemographic().getMacAddress();
        Date timestamp1 = appPK1.getTimestamp();
        GregorianCalendar curDate = new GregorianCalendar();
        curDate.setTime(timestamp1);

        //obtain the day, month and year of the app and subsequently set it as current date 
        int day = curDate.get(GregorianCalendar.DAY_OF_MONTH);
        int month = curDate.get(GregorianCalendar.MONTH);
        int year = curDate.get(GregorianCalendar.YEAR);

        curDate = new GregorianCalendar(year, month, day, 0, 0, 0);

        AppPK appPK2 = null;
        String macAddress2 = null;
        Date timestamp2 = null;
        GregorianCalendar nextDate = null;
        int nextAppDay = 0;
        int nextAppMonth = 0;
        int nextAppYear = 0;
        //check if next app usage is not null
        if (app2 != null) {
            appPK2 = app2.getAppPK();
            macAddress2 = appPK2.getDemographic().getMacAddress();
            timestamp2 = appPK2.getTimestamp();
            nextDate = new GregorianCalendar();
            nextDate.setTime(timestamp2);
            
            //obtain the day, month and year of the next app and subsequently set it as next date 
            nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
            nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
            nextAppYear = nextDate.get(GregorianCalendar.YEAR);
            
            nextDate = new GregorianCalendar(nextAppYear, nextAppMonth, nextAppDay, 0, 0, 0);
        }
        //check if both instances of usage belong to the same user
        //otherwise two app macaddresses are not the same, i.e. different users and hence goes to else 
        if (macAddress1.equals(macAddress2)) {
            //if both app usage are within the same day,i.e before 00:00:00 the next day.
            //take the difference between the next timestamp and the current timestamp.
            if (curDate.equals(nextDate)) {
                usageTime = (int) (timestamp2.getTime() - timestamp1.getTime()) / 1000;
            } else {//else take the difference of 00:00:00 the next day less the timestamp.
                usageTime = (int) ((nextDate.getTime().getTime() - timestamp1.getTime()) / 1000);
            }

            //check if usage is more than 120 and both app usage are within the same day; set to 10 sec if so.
            if ((curDate.equals(nextDate) && usageTime > 120) || (!curDate.equals(nextDate) && usageTime > 10)) {
                usageTime = 10;
            }
        } else {
            //check if next date is null
            if (nextDate == null) {
                nextDate = new GregorianCalendar();
                nextDate.setTime(timestamp1);
                nextAppDay = nextDate.get(GregorianCalendar.DAY_OF_MONTH);
                nextAppMonth = nextDate.get(GregorianCalendar.MONTH);
                nextAppYear = nextDate.get(GregorianCalendar.YEAR);
            }
            nextDate = new GregorianCalendar(year, month, day, 0, 0, 0);
            nextDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
            
            usageTime = (int) (nextDate.getTime().getTime() - timestamp1.getTime()) / 1000;

            if (usageTime > 10) {
                usageTime = 10;
            }

        }
        return usageTime;
    }

    /**
     * This method returns a LinkedHashMap of average usage time for each app 
     * category.
     *
     * @param startDate Input start date in the format of yyyy/mm/dd
     * @param endDate Input end date in the format of yyyy/mm/dd
     * @return LinkedHashmap<String, Double> with app categories as the unique key and the average usage time as the respective value 
     */
    public LinkedHashMap<String, Double> retrieveAppUsageByCategory(String startDate, String endDate) {
        //Account for all app categories and sort them in alphabetical order 
        List<String> uniqueCategories = new ArrayList<String>(Arrays.asList("books", "social", "education", "entertainment", "information", "library", "local", "tools", "fitness", "games", "others"));
        Collections.sort(uniqueCategories);

        //Concatenate the start of time as 00:00:00 and end of time as 23:59:59
        startDate += " 00:00:00";
        endDate += " 23:59:59";

        Date startDateTime = null;
        Date endDateTime = null;
        int numberOfDay = 0;

        //Create a LinkedHashMap to place the app categories in insert order
        //with AppCategory as the key and  TotalTimeUsage as the the value 
        LinkedHashMap<String, Double> categoryUsageLinkedHashMap = new LinkedHashMap<String, Double>();
        for (String category : uniqueCategories) {
            categoryUsageLinkedHashMap.put(category, 0.0);
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);

            double numDaysInDouble = (endDateTime.getTime() - startDateTime.getTime()) / (1000.0 * 60.0 * 60 * 24);
            numberOfDay = (int) (Math.ceil(numDaysInDouble)); //round up to int

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        //check if endDate is before startDate, if yes return null
        if (endDateTime.before(startDateTime)) {
            return null;
        }

        //retrieve the list of relevant app usage based on the input startdate and endddate 
        List<App> appList = appDAO.retrieveAppByStartEndDate(startDateTime, endDateTime);

        for (int i = 0; i < appList.size(); i++) {
            App firstApp = appList.get(i);
            App nextApp = null;
            //retrieve the next app till the end of appList to prevent indexOutOfBoundException
            if (i + 1 < appList.size()) {
                nextApp = appList.get(i + 1);
            }
            double categoryUsageTime = 0;
            int usageTime = calculateAppUsageTime(firstApp, nextApp);
            //change to lowercase to retrieve lower-case app category as key to linked hashmap
            String firstAppCategory = firstApp.getAppLookup().getCategory().toLowerCase();
            //retrieve app category from linked hashmap 
            categoryUsageTime = categoryUsageLinkedHashMap.get(firstAppCategory);
            //add usage time to current category usage time if there is 
            categoryUsageLinkedHashMap.put(firstAppCategory, (categoryUsageTime + usageTime));
        }

        Iterator<String> iter = categoryUsageLinkedHashMap.keySet().iterator();
        while (iter.hasNext()) {
            String category = iter.next();
            double totalUsageTime = categoryUsageLinkedHashMap.get(category);
            //store the final usage time per app category divided by the number of days 
            categoryUsageLinkedHashMap.put(category, (1.0 * totalUsageTime / numberOfDay));

        }

        return categoryUsageLinkedHashMap;

    }
}
