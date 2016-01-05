/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.routes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import smua.controllers.AdvancedSmartphoneOveruseController;
import smua.controllers.BasicAppController;
import smua.controllers.BootstrapController;
import smua.controllers.LoginController;
import smua.controllers.SmartphoneOveruseController;
import smua.controllers.TopKController;
import smua.models.Entities.Demographic;
import smua.models.JSON.BasicAppUsage;
import smua.models.JSON.BootstrapError;
import smua.models.JSON.SmartphoneOveruse;
import smua.models.JSON.TopKApp;
import smua.models.JSON.TopKSchool;
import smua.models.JSON.TopKStudent;
import smua.controllers.HeatmapController;
import smua.controllers.SocialActivenessController;
import smua.models.DAO.DemographicDAO;
import smua.models.DAO.LocationDAO;
import smua.models.DAO.LocationLookupDAO;
import smua.models.Entities.LocationLookup;
import smua.models.JSON.UsageHeatmap;

/**
 *
 * @author shtang.2014
 */
@Controller
public class ViewController {

    @Autowired
    LoginController loginController;

    @Autowired
    BootstrapController bootstrapController;

    @Autowired
    BasicAppController appController;

    @Autowired
    TopKController topKController;

    @Autowired
    SmartphoneOveruseController smartphoneOveruseController;

    @Autowired
    HeatmapController heatmapController;

    @Autowired
    SocialActivenessController socialActivenessController;

    @Autowired
    LocationDAO locationDAO;

    @Autowired
    DemographicDAO demographicDAO;

    @Autowired
    LocationLookupDAO locationLookupDAO;

    @Autowired
    AdvancedSmartphoneOveruseController advSmartphoneOveruseController;

    /**
     * Directs user to login page
     *
     * @return String
     */
    @RequestMapping(value = {"/login", "/"}, method = RequestMethod.GET)
    public String loginView() {
        return "login";
    }

    /**
     * Directs login requests to LoginController and redirects users to Home
     * page if credentials are valid for user or Bootstrap page if credentials
     * are valid for admin. Invalid credentials entered would result in a
     * redirect back to login page
     *
     * @param email
     * @param password
     * @param request
     * @param model
     * @return String
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String executeLogin(String email, String password, HttpServletRequest request, Model model) {
        //check if email or password is empty

        if (!email.matches("[0-9a-zA-Z.]+")) {
            return null;
        }

        if (email == null || password == null) {
            //add error msg
            model.addAttribute("msg", "Invalid Username or Password!");
            //redirect to login
            return "login";
        }

        //Retrieve demo object
        Demographic demo = loginController.executeLogin(email, password);

        if (demo == null) {
            model.addAttribute("msg", "Invalid Username or Password!");
            return "login";
        }

        //check if admin
        if (demo.getEmail().equals("admin") && demo.getPassword().equals("password")) {
            HttpSession session = request.getSession();
            session.setAttribute("currUser", demo);
            return "redirect:/admin";
        } else {
            //creates session and stores user details into session
            HttpSession session = request.getSession();
            session.setAttribute("currUser", demo);
            //forward to home page
            return "redirect:/home";
        }
    }

    /**
     * Logout user
     *
     * @param request
     * @return String
     */
    //logout functionality
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String executeLogout(HttpServletRequest request) {
        //Invalidate session upon logout
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Directs admin to bootstrap
     *
     * @param request
     * @return String
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String bootstrapView(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Demographic currUser = (Demographic) session.getAttribute("currUser");
        if (currUser == null) {
            return "redirect:/login";
        }
        //Prevent admin from accessing home pages
        if (!currUser.getEmail().equals("admin")) {
            return "redirect:/home";
        }
        return "admin/home";
    }

    /**
     * Direct admin to bootstram
     * @param request
     * @return 
     */
    @RequestMapping(value = "/bootstrap", method = RequestMethod.GET)
    public String bootstrap(HttpServletRequest request) {
        return "redirect:/admin";
    }

    /**
     * Directs files to bootstrap to BootstrapController
     *
     * @param fileToUpload 
     * @param model 
     * @return String
     */
    @RequestMapping(value = "/bootstrap", method = RequestMethod.POST)
    public String bootstrap(MultipartFile fileToUpload, Model model) {
        BootstrapError bootstrapError = bootstrapController.locationUpload(fileToUpload);
        if (bootstrapError == null) {
            model.addAttribute("invalidExtension", "invalid file extension");
            return "admin/home";
        }
        if (bootstrapError.getErrors().isEmpty()) {
            model.addAttribute("status", "Success");
        } else {
            model.addAttribute("status", "Error");
        }
        model.addAttribute("type", "Bootstrap");
        model.addAttribute("bootstrapError", bootstrapError);
        return "admin/home";
    }

    /**
     * Directs files to be added to BootstrapController
     * @return String
     */
    @RequestMapping(value = "/addData", method = RequestMethod.GET)
    public String addDataView() {
        return "redirect:/admin";
    }

    /**
     * Directs files to be added to BootstrapController
     *
     * @param fileToUpload 
     * @param model
     * @return String
     */
    @RequestMapping(value = "/addData", method = RequestMethod.POST)
    public String addData(MultipartFile fileToUpload, Model model) {

        BootstrapError addBootstrapError = bootstrapController.addData(fileToUpload);

        if (addBootstrapError == null) {
            model.addAttribute("invalidExtension", "invalid file extension");
            return "admin/home";
        }

        if (addBootstrapError.getErrors().isEmpty() && (addBootstrapError.getNumRecordLoaded().get("invalidDelete") == null || addBootstrapError.getNumRecordLoaded().get("invalidDelete") == 0)) {
            model.addAttribute("status", "Success");
        } else {
            model.addAttribute("status", "Error");
        }
        model.addAttribute("type", "Add Data");
        model.addAttribute("bootstrapError", addBootstrapError);
        return "admin/home";
    }

    @RequestMapping(value = {"/test"}, method = RequestMethod.GET)
    public String bootstrapTestView() {
        return "fileUploadJSONTester";
    }

    /**
     * Redirects user to the UI for deleting location data
     *
     * @return String
     */
    @RequestMapping(value = {"/admin/deleteLocation"}, method = RequestMethod.GET)
    public String deleteLocationView() {
        return "admin/deleteLocation";
    }

    /**
     * Redirects user to the UI for deleting location data
     *
     * @return String
     */
    @RequestMapping(value = {"/deleteLocation"}, method = RequestMethod.GET)
    public String deleteLocation() {
        return "redirect:/admin/deleteLocation";
    }

    /**
     * Directs the parameters required for deletion of location data to the
     * BootstrapController for deletion of relevant records. After which, users
     * will be redirected back to the same page.
     *
     * @param macAddress user's macaddress to be deleted
     * @param startDate startdate of the deletion period
     * @param starthours start hour of the deletion period
     * @param startminutes start minutes of the deletion period
     * @param startseconds start seconds of the deletion period
     * @param endDate enddate of the deletion period
     * @param endhours end hours of the deletion period
     * @param endminutes end minutes of the deletion period
     * @param endseconds end seconds of the deletion period
     * @param locationID locationID records to be deleted
     * @param semanticPlace semanticPlace records to be deleted
     * @param model
     * @return String
     */
    @RequestMapping(value = "/deleteLocation", method = RequestMethod.POST)
    public String deleteLocation(String macAddress, String startDate, String starthours, String startminutes, String startseconds, String endDate, String endhours, String endminutes, String endseconds,
            String locationID, String semanticPlace, Model model) {

        ArrayList<String> errorList = new ArrayList<>();

        Date startDateTime = null;
        Date endDateTime = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startDateTime = sdf.parse(startDate);
        } catch (ParseException ex) {
            errorList.add("invalid startdate");
        }

        
        if (!endDate.equals("")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                endDateTime = sdf.parse(endDate);
            } catch (ParseException ex) {
                errorList.add("invalid enddate");
            }
        }
        if (endDateTime != null && startDateTime != null && endDateTime.before(startDateTime)) {
            errorList.add("invalid startdate");
        }

        if (endDate.equals("")) {
            endDate += startDate + " 23:59:59";
        } else {
            endDate += " " + endhours + ":" + endminutes + ":" + endseconds;
        }

        startDate += " " + starthours + ":" + startminutes + ":" + startseconds;

        Date checkStartDate = null;
        Date checkEndDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            checkStartDate = sdf.parse(startDate);
            checkEndDate = sdf.parse(endDate);
        } catch (ParseException ex) {

        }
        if (checkStartDate != null && checkEndDate != null && checkEndDate.before(checkStartDate)) {
            errorList.add("invalid starttime");
        }
        
        //regex expression to validate that mac address is valid
        if (!macAddress.equals("") && !macAddress.matches("[a-f0-9A-F]{40}")) {
            errorList.add("invalid macaddress");
        }else if(!macAddress.equals("")){
            Demographic demo = demographicDAO.getDemographicByMacAddress(macAddress);
            if (demo == null) {
                errorList.add("invalid macaddress");
            }
        }

        int intLocationID = 0;
        LocationLookup locationLookup = null;
        if (!locationID.equals("")) {
            try {
                intLocationID = Integer.parseInt(locationID);
                locationLookup = locationLookupDAO.retrieveByID(intLocationID);
                if (locationLookup == null) {
                    errorList.add("invalid location-id");
                }
            } catch (NumberFormatException ex) {
                errorList.add("invalid location-id");
            }
        }
        
        if (!semanticPlace.equals("")) {
            List<LocationLookup> locationLookupList = locationLookupDAO.retrieveBySemanticPlace(semanticPlace);
            if (locationLookupList.isEmpty()) {
                errorList.add("invalid semantic place");
            }
        }

        if (locationLookup != null && !semanticPlace.equals("")) {
            if (!locationLookup.getSemanticPlace().equals(semanticPlace)) {
                errorList.add("invalid location-id");
            }
        }
        if (!errorList.isEmpty()) {
            model.addAttribute("errorList", errorList);
            return "admin/deleteLocation";
        }

        //returns the successful number of rows deleted
        int rowsDeleted = bootstrapController.bootstrapDelete(macAddress, startDate, endDate, intLocationID, semanticPlace);

        model.addAttribute("rowsDeleted", rowsDeleted);

        return "admin/deleteLocation";
    }

    /**
     * Directs user to Home page
     *
     * @param request
     * @return String
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Demographic currUser = (Demographic) session.getAttribute("currUser");
        //Prevent admin from accessing home pages
        if (currUser.getEmail().equals("admin")) {
            return "redirect:/admin";
        }

        return "home";
    }

    /**
     * This method returns the BasicAppUsageTime view.
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/basicAppUsageTime", method = RequestMethod.GET)
    public String getBasicAppUsageTimeView(HttpServletRequest request) {
        return "basicAppUsageTime";
    }

    /**
     * This method gets the Basic App Usage Breakdown by usage time category
     * output and returns it to the BasicAppUsageTime view.
     *
     * @param startDate
     * @param endDate
     * @param model
     * @return
     */
    @RequestMapping(value = {"/basicAppUsageTime"}, method = RequestMethod.POST)
    public String getBasicAppStartEnd(String startDate, String endDate, Model model) {
        if (startDate == null || startDate.equals("") || endDate == null || endDate.equals("")) {
            model.addAttribute("error", "Please enter a valid date");
            return "basicAppUsageTime";
        }
        HashMap<String, Integer> appHashMap = appController.retrieveAppByStartEndDate(startDate, endDate);

        if (appHashMap == null) {
            model.addAttribute("error", "Start date cannot be after End Date!");
            return "basicAppUsageTime";
        }
        HashMap<String, Integer> usageCategoryHashMap = appController.retrieveUsageCategory(appHashMap);
        model.addAttribute("usageCategoryList", usageCategoryHashMap);

        return "basicAppUsageTime";
    }

    /**
     * This method returns the BasicAppUsageTimeBreakdown view.
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/basicAppUsageTimeBreakdown", method = RequestMethod.GET)
    public String getBasicAppUsageTimeBreakdownView(HttpServletRequest request) {
        return "basicAppUsageTimeBreakdown";
    }

    /**
     * This method gets the Basic App Usage Breakdown by usage time category and
     * demographics output and returns it to the basicAppUsageTimeBreakdown
     * view.
     *
     * @param startDate
     * @param endDate
     * @param category
     * @param model
     * @return
     */
    @RequestMapping(value = "/basicAppUsageTimeBreakdown", method = RequestMethod.POST)
    public String getBasicAppUsageTimeBreakdown(String startDate, String[] category, String endDate, Model model) {

        //TODO;
        Date startDateTime = null;
        Date endDateTime = null;
        ArrayList<String> errorList = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        if (startDate.equals("") || endDate.equals("")) {
            errorList.add("Dates cannot be empty!");
        }

        List<String> duplicateCategoryList = new ArrayList<String>();
        List<String> categoryList = new ArrayList<String>();
        for (int i = 0; i < category.length; i++) {
            duplicateCategoryList.add(category[i]);
        }

        for (int i = 0; i < category.length; i++) {
            categoryList.add(category[i]);
        }

        Collections.sort(duplicateCategoryList);

        if (endDateTime != null && endDateTime.before(startDateTime)) {
            errorList.add("Start date cannot be after End Date!");
        }
        boolean repeat = false;

        for (int i = 0; i < duplicateCategoryList.size(); i++) {
            String curCategory = duplicateCategoryList.get(i);
            if (!curCategory.equals("") && i + 1 < duplicateCategoryList.size() && duplicateCategoryList.get(i + 1).equals(curCategory)) {
                repeat = true;
            }
        }

        if (repeat) {
            errorList.add("Please select each sort category only once");

        }

        String categories = "";

        for (int i = 0; i < categoryList.size(); i++) {
            String curCategory = categoryList.get(i);
            if (!curCategory.equals("")) {
                categories += curCategory + ",";
            }
        }
        System.out.println(categories);
        if (categories.length() > 0) {
            categories = categories.substring(0, categories.length() - 1);
        }

        if (categories.equals("")) {
            errorList.add("Please enter at least one category");
        }

        if (!errorList.isEmpty()) {
            model.addAttribute("errorList", errorList);
            return "basicAppUsageTimeBreakdown";
        }

        BasicAppUsage basicAppUsage = appController.retrieveAppUsageByDemographicBreakdown(startDate, endDate, categories);

        model.addAttribute("basicAppUsage", basicAppUsage);

        return "basicAppUsageTimeBreakdown";
    }

    /**
     * This method returns the basicAppUsageDiurnal view
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/basicAppUsageDiurnal"}, method = RequestMethod.GET)
    public String getBasicAppDiurnalView(HttpServletRequest request) {
        return "basicAppUsageDiurnal";
    }

    /**
     * This method gets the output for the Diurnal pattern of app usage time and
     * returns it to the basicAppUsageDiurnal view.
     *
     * @param date
     * @param gender
     * @param school
     * @param year
     * @param model
     * @return
     */
    @RequestMapping(value = {"/basicAppUsageDiurnal"}, method = RequestMethod.POST)
    public String getBasicAppDiurnalUsage(String date, String gender, String school, String year, Model model) {

        if (date == null || date.equals("")) {
            model.addAttribute("error", "Please enter a valid date");
            return "basicAppUsageDiurnal";
        }
        LinkedHashMap<String, Integer> result = appController.getBasicAppDiurnalUsage(date, gender, school, year);
        model.addAttribute("basicAppDiurnalResult", result);
        return "basicAppUsageDiurnal";
    }

    /**
     * This method returns the basicAppUsageAppCat view.
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/basicAppUsageAppCat"}, method = RequestMethod.GET)
    public String getBasicAppAppCategoryView(HttpServletRequest request) {
        return "basicAppUsageAppCat";
    }

    /**
     * This method gets the output for the Basic App Usage breakdown by app
     * category and returns it to the basicAppUsageAppCat view.
     *
     * @param startDate
     * @param endDate
     * @param model
     * @return
     */
    @RequestMapping(value = {"/basicAppUsageAppCat"}, method = RequestMethod.POST)
    public String getBasicAppAppCategory(String startDate, String endDate, Model model) {

        if (startDate == null || startDate.equals("") || endDate == null || endDate.equals("")) {
            model.addAttribute("error", "Please enter a valid date");
            return "basicAppUsageAppCat";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        sdf.setLenient(false);
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = sdf.parse(startDate + " 00:00:00");
            endDateTime = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {

        }
        if (startDateTime.after(endDateTime)) {
            model.addAttribute("error", "Start date cannot be after end date");
        }

        LinkedHashMap<String, Double> categoryLinkedHashMap = appController.retrieveAppUsageByCategory(startDate, endDate);
        model.addAttribute("categoryList", categoryLinkedHashMap);
        return "basicAppUsageAppCat";
    }

    /**
     * Directs user to topKApp page
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param school
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKApp"}, method = RequestMethod.GET)
    public String getTopKAppView(String startDate, String endDate, Model model, String school, String topKValue) {
        return "topKApp";
    }

    /**
     * Directs fields to be passed to TopKController from topKApp
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param school
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKApp"}, method = RequestMethod.POST)
    public String getTopKApp(String startDate, String endDate, Model model, String school, String topKValue) {
        if (startDate.equals("") || endDate.equals("")) {
            model.addAttribute("errorMessage", "Please enter valid dates");
            return "/topKApp";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = sdf.parse(startDate + " 00:00:00");
            endDateTime = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }
        if (startDateTime.after(endDateTime)) {
            model.addAttribute("errorMessage", "Start date cannot be after end date");
        }
        ArrayList<TopKApp> appList = topKController.retrieveTopKApps(startDateTime, endDateTime, school, topKValue);
        model.addAttribute("appList", appList);
        return "topKApp";
    }

    /**
     * Directs user to topKStudent page
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param school
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKStudent"}, method = RequestMethod.GET)
    public String getTopKStudentView(String startDate, String endDate, Model model, String school, String topKValue) {
        return "topKStudent";
    }

    /**
     * Directs fields to be passed to TopKController from topKStudent
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param category
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKStudent"}, method = RequestMethod.POST)
    public String getTopKStudent(String startDate, String endDate, Model model, String category, String topKValue) {
        if (startDate.equals("") || endDate.equals("")) {
            model.addAttribute("errorMessage", "Please enter valid dates");
            return "/topKStudent";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = sdf.parse(startDate + " 00:00:00");
            endDateTime = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }
        if (startDateTime.after(endDateTime)) {
            model.addAttribute("errorMessage", "Start date cannot be after end date");
        }
        ArrayList<TopKStudent> topKList = topKController.retrieveTopKStudents(startDateTime, endDateTime, category, topKValue);
        model.addAttribute("topKList", topKList);
        return "topKStudent";

    }

    /**
     * Directs user to topKSchool
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param category
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKSchool"}, method = RequestMethod.GET)
    public String getTopKSchoolsView(String startDate, String endDate, Model model, String category, String topKValue) {
        return "topKSchool";

    }

    /**
     * Directs fields to be passed to TopKController from topKSchool
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param category
     * @param topKValue
     * @return String
     */
    @RequestMapping(value = {"/topKSchool"}, method = RequestMethod.POST)
    public String getTopKSchool(String startDate, String endDate, Model model, String category, String topKValue) {
        if (startDate.equals("") || endDate.equals("")) {
            model.addAttribute("errorMessage", "Please enter valid dates");
            return "/topKSchool";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = sdf.parse(startDate + " 00:00:00");
            endDateTime = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }
        if (startDateTime.after(endDateTime)) {
            model.addAttribute("errorMessage", "Start date cannot be after end date");
        }
        ArrayList<TopKSchool> topKSchoolList = topKController.retrieveTopKSchools(startDateTime, endDateTime, category, topKValue);
        model.addAttribute("topKSchoolList", topKSchoolList);
        return "topKSchool";

    }

    /**
     * Directs fields to be passed to SmartphoneOveruseController from
     * smartphoneOveruse
     *
     * @param startDate 
     * @param endDate 
     * @param model  
     * @param email
     * @return String
     */
    @RequestMapping(value = {"/smartphoneOveruse"}, method = RequestMethod.POST)
    public String getSmartphoneOveruse(String startDate, String endDate, Model model, String email) {
        if (startDate.equals("") || endDate.equals("")) {
            return "redirect:/smartphoneOveruse";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date startDateTime = null;
        Date endDateTime = null;
        System.out.println(email);
        try {
            startDateTime = sdf.parse(startDate + " 00:00:00");
            endDateTime = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }
        if (startDateTime.after(endDateTime)) {
            model.addAttribute("errorMessage", "Start date is after end date");
        }
        SmartphoneOveruse smartphoneOveruse = smartphoneOveruseController.retrieveSmartphoneOveruse(startDateTime, endDateTime, email);
        model.addAttribute("smartphoneOveruse", smartphoneOveruse);

        return "smartphoneOveruse";

    }

    /**
     *
     * Directs user to smartphoneOveruse view 
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param email
     * @return String
     */
    @RequestMapping(value = {"/smartphoneOveruse"}, method = RequestMethod.GET)
    public String getSmartphoneOveruseView(String startDate, String endDate, Model model, String email) {
        return "smartphoneOveruse";

    }

    /**
     * This method returns the smartphoneHeatmap view
     *
     * @param startDate
     * @param endDate
     * @param model
     * @param email
     * @return smartphoneHeatmap
     */
    @RequestMapping(value = {"/smartphoneHeatmap"}, method = RequestMethod.GET)
    public String getSmartphoneUsageView(String startDate, String endDate, Model model, String email) {
        return "smartphoneHeatmap";
    }

    /**
     * This method generates the smartphoneHeatmap output given the respective
     * parameters
     *
     * @param date Date of interest
     * @param hours hh: value range from 00 - 23
     * @param minutes mm : value range from 00 - 59
     * @param seconds ss : value range from 00 - 59
     * @param model org.springframework.ui.model object
     * @param floor Specified floor in SIS, L2, L3 etc
     * @return smartphoneHeatmap view
     */
    @RequestMapping(value = {"/smartphoneHeatmap"}, method = RequestMethod.POST)
    public String getSmartphoneUsageHeatMap(String date, String hours, String minutes, String seconds, Model model, String floor) {
        if (date.equals("")) {
            model.addAttribute("errorMsg", "Please enter a date.");
            return "smartphoneHeatmap";
        }
        String timestamp = date + " " + hours + ":" + minutes + ":" + seconds;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date timestampDate = null;

        try {
            timestampDate = sdf.parse(timestamp);
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }

        List<UsageHeatmap> usageHeatMapList = heatmapController.getSmartphoneHeatmap(timestampDate, floor);
        for (UsageHeatmap u : usageHeatMapList) {
            System.out.println("PLACE : " + u.getSemanticPlace() + " Num User" + u.getNumberOfpeopleUsingPhone()
                    + " Density : " + u.getCrowdDensity());
        }
        model.addAttribute("usageHeatMapList", usageHeatMapList);

        return "smartphoneHeatmap";
    }

    /**
     * Gets the socialActivenessReport view
     *
     * @return
     */
    @RequestMapping(value = {"/socialActivenessReport"}, method = RequestMethod.GET)
    public String getSocialActivenessReportView() {
        return "socialActivenessReport";
    }

    /**
     * Gets the Social Activeness Report
     *
     * @param date Date object
     * @param model org.springframework.ui.model object
     * @param macAddress MAC Address
     * @return socialActivenessReport view
     */
    @RequestMapping(value = {"/socialActivenessReport"}, method = RequestMethod.POST)
    public String getSocialActivenessReport(String date, Model model, String macAddress) {
        if (date.equals("")) {
            model.addAttribute("errorMsg", "Please enter a date.");
            return "socialActivenessReport";
        }
        String startDateTime = date + " 00:00:00";
        String endDateTime = date + " 23:59:59";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        Date startDate = null;
        Date endDate = null;

        try {
            startDate = sdf.parse(startDateTime);
            endDate = sdf.parse(endDateTime);
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }
        System.out.println("MAC ADDRESS IS" + macAddress);
        LinkedHashMap<String, Integer> socialActivenessReport = socialActivenessController.getSocialActivenessReport(startDate, endDate, macAddress);
        TreeMap<String, Integer> socialAppUsageTime = socialActivenessController.getSocialAppUsageTime(startDate, endDate, macAddress);
        model.addAttribute("socialActivenessReport", socialActivenessReport);
        model.addAttribute("socialAppUsageTime", socialAppUsageTime);
        return "socialActivenessReport";
    }

    /**
     * This method gets the advanced smartphone overuse webpage
     *
     * @return String
     */
    @RequestMapping(value = {"/advancedSmartphoneOveruse"}, method = RequestMethod.GET)
    public String getAdvancedSmartphoneOveruseView() {
        return "advancedSmartphoneOveruse";
    }

    /**
     * This method returns the advanced smartphone overuse report of the logged in user
     *
     * @param startDate
     * @param endDate
     * @param macAddress
     * @param model
     * @return String
     */
    @RequestMapping(value = {"/advancedSmartphoneOveruse"}, method = RequestMethod.POST)
    public String getAdvancedSmartphoneOveruse(String startDate, String endDate, String macAddress, Model model) {

        if (startDate.equals("") && endDate.equals("")) {
            model.addAttribute("errorMsg", "Please enter a start date and/or an end date");
            return "advancedSmartphoneOveruse";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        sdf.setLenient(false);

        boolean invalidDate = false;
        Date startingDate = null;
        Date endingDate = null;

        try {
            startingDate = sdf.parse(startDate + " 00:00:00");
            endingDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            model.addAttribute("errorMsg", "Please valid dates");
            return "advancedSmartphoneOveruse";
        }

        if (startingDate.after(endingDate)) {
            model.addAttribute("errorMsg", "End date must be after Start date");
            return "advancedSmartphoneOveruse";
        }

        HashMap<String, String> outputMap = advSmartphoneOveruseController.getAdvSmartphoneReport(startingDate, endingDate, macAddress);
        model.addAttribute("overuseIndex", outputMap.get("overuseIndex"));
        model.addAttribute("classTime", outputMap.get("classTime"));
        model.addAttribute("smallGroupTime", outputMap.get("smallGroupTime"));
        model.addAttribute("unproductiveAppUsage", outputMap.get("unproductiveAppUsage"));

        return "advancedSmartphoneOveruse";
    }
}
