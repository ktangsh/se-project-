/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.AppDAO;
import smua.models.DAO.DemographicDAO;
import smua.models.DAO.LocationDAO;
import smua.models.DAO.LocationLookupDAO;
import smua.models.Entities.App;
import smua.models.Entities.Location;
import smua.models.Entities.LocationLookup;
import smua.models.JSON.UsageHeatmap;

/**
 *
 * @author shtang.2014
 */
@Controller
public class HeatmapController {

    @Autowired
    AppDAO appDAO;

    @Autowired
    DemographicDAO demographicDAO;

    @Autowired
    LocationDAO locationDAO;
    
    @Autowired
    LocationLookupDAO locationLookupDAO;
    /**
     * Retrieve and categorize the smartphone usage for a particular timestamp
     * (15 minutes prior to the specified date and time) per semantic place. The
     * density count is based on the number of people using smartphones (for all
     * location_ids in the semantic place)
     *
     * @param timestampDate Date object of interest
     * @param floor Floor in SIS, e.g L2, L3
     * @return List of UsageHeatMap objects
     */
    public List<UsageHeatmap> getSmartphoneHeatmap(Date timestampDate, String floor) {

        //calculate startDate 15min before specified date, inclusive
        GregorianCalendar startDate = new GregorianCalendar();
        startDate.setTime(timestampDate);
        startDate.add(GregorianCalendar.SECOND, -15 * 60);

        //calculate endDate, exclusive
        GregorianCalendar endDate = new GregorianCalendar();
        endDate.setTime(timestampDate);
        endDate.add(GregorianCalendar.SECOND, -1);


        //retrieve location given specified startDate, endDate and floor
        List<Location> locationList = locationDAO.retrieveLocationByStartEndDatePlace(startDate.getTime(), endDate.getTime(), floor);
        //Use hashmap to get latest location for each macAddress
        HashMap<String, String> lastKnownLocation = new HashMap<String, String>();
        HashMap<String, String> uniqueLocationMap = new HashMap<String, String>();
        for (Location location : locationList) {
            String macAddress = location.getLocationPK().getMacAddress();
            String semanticPlace = location.getLocationLookup().getSemanticPlace();
            lastKnownLocation.put(macAddress, semanticPlace);

        }
        //set last known locations into a list of unique locations
        List<LocationLookup> locationLookups = locationLookupDAO.retrieveByFloor(floor);
        System.out.println(locationLookups.size());
        for(LocationLookup location:locationLookups) {
            String semanticPlace = location.getSemanticPlace();
            uniqueLocationMap.put(semanticPlace, "unique");
        }
        ArrayList<String> semanticPlaces = new ArrayList<String>(uniqueLocationMap.keySet());
        Collections.sort(semanticPlaces);

        List<UsageHeatmap> outputList = new ArrayList<UsageHeatmap>();

        //check if user in hashmap also has app usage in the specified time
        //get appList given specified startDate and endDate
        List<App> appList = appDAO.retrieveAppByStartEndDate(startDate.getTime(), endDate.getTime());
        ArrayList<String> macAddressList = new ArrayList<String>(lastKnownLocation.keySet());

        for (String semanticPlace : semanticPlaces) {
            int usageCount = 0;
            int density = 0;
            Iterator<String> iter = lastKnownLocation.keySet().iterator();
            while (iter.hasNext()) {
                String macAddress = iter.next();
                String place = lastKnownLocation.get(macAddress);
                if (place.equals(semanticPlace)) {
                    boolean isFound = false;
                    for (App app : appList) {
                        String appMacAddress = app.getAppPK().getDemographic().getMacAddress();
                        //check if user address has app usage
                        if (appMacAddress.equals(macAddress)) {
                            isFound = true;
                        }
                    }
                    //increment UsageCount if user is also found to be using an app
                    if (isFound) {
                        usageCount++;
                    }
                }

            }
            if (usageCount >= 1 && usageCount <= 3) {
                density = 1;
            } else if (usageCount >= 4 && usageCount <= 7) {
                density = 2;
            } else if (usageCount >= 8 && usageCount <= 13) {
                density = 3;
            } else if (usageCount >= 14 && usageCount <= 20) {
                density = 4;
            } else if (usageCount >= 21) {
                density = 5;
            }
            outputList.add(new UsageHeatmap(semanticPlace, usageCount, density));
        }

        return outputList;
    }

}
