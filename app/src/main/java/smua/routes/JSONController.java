/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.routes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import is203.JWTException;
import is203.JWTUtility;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import smua.controllers.AdvancedSmartphoneOveruseController;
import smua.controllers.BasicAppController;
import smua.controllers.BootstrapController;
import smua.controllers.HeatmapController;
import smua.controllers.LoginController;
import smua.controllers.SmartphoneOveruseController;
import smua.controllers.SocialActivenessController;
import smua.controllers.TopKController;
import smua.models.DAO.DemographicDAO;
import smua.models.DAO.LocationDAO;
import smua.models.DAO.LocationLookupDAO;
import smua.models.Entities.Demographic;
import smua.models.Entities.Location;
import smua.models.Entities.LocationLookup;
import smua.models.JSON.BasicAppUsage;
import smua.models.JSON.BootstrapError;
import smua.models.JSON.SmartphoneOveruse;
import smua.models.JSON.TopKApp;
import smua.models.JSON.TopKSchool;
import smua.models.JSON.TopKStudent;
import smua.models.JSON.UsageHeatmap;
import smua.util.SMUAUtility;

/**
 *
 * @author shtang.2014
 */
@Controller
public class JSONController {

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
    SMUAUtility smuaUtility;

    @Autowired
    DemographicDAO demographicDAO;

    @Autowired
    LocationDAO locationDAO;

    @Autowired
    LocationLookupDAO locationLookupDAO;

    @Autowired
    AdvancedSmartphoneOveruseController advSmartphoneOveruseController;

    /**
     * Routes JSON login requests and authenticates credentials entered. Returns
     * an Authenticate object
     *
     * @param email email of user
     * @param password stored password of the user
     * @param request HttpServletRequest
     * @return an ObjectNode representing authenticate login
     */
    @RequestMapping("/json/authenticate")
    @ResponseBody
    public ObjectNode executeJSONLogin(String username, String password, HttpServletRequest request) {
        ArrayList<String> errorList = new ArrayList<String>();

        if (username == null) {
            errorList.add("missing username");
        }
        if (password == null) {
            errorList.add("missing password");
        }
        if (username != null && username.equals("")) {
            errorList.add("blank username");
        }
        if (password != null && password.equals("")) {
            errorList.add("blank password");
        }

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();

        Collections.sort(errorList);

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        Demographic demo = loginController.executeLogin(username, password);
        //check if email or password is null 
        if (demo == null) {
            //return a failed authenticate object
            node.put("status", "error");
            errorList.add("invalid username/password");
            node.putPOJO("messages", errorList);
            return node;
        }

        String token = JWTGenerator(username);

        node.put("status", "success");
        node.put("token", token);
        return node;
    }

    /**
     * Routes JSON bootstrap requests and performs bootstrap Returns a JSON
     * object in the form of ObjectNode
     *
     * @param fileToUpload file to upload
     * @param token admin token
     * @return an ObjectNode representing the bootstrap with app usage &
     * demographics data
     */
    @RequestMapping(value = {"/json/bootstrap"}, method = RequestMethod.POST)
    @ResponseBody
    public ObjectNode bootstrap(@RequestParam("bootstrap-file") MultipartFile fileToUpload, String token) {

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ArrayList<String> errorList = new ArrayList<>();

        //checks if bootstrap-file is blank
        if (fileToUpload != null && fileToUpload.isEmpty()) {
            errorList.add("blank bootstrap-file");
        }

        //checks if current token is valid
        validateToken(errorList, token, true);
        for (String s : errorList) {
            System.out.println(s);
        }
        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }
        BootstrapError bootstrapError = bootstrapController.bootstrap(fileToUpload);

        if (bootstrapError == null) {
            node = nodeFactory.objectNode();
            node.put("status", "error");
            node.put("messages", "invalid bootstrap-file");
            return node;
        }

        node = nodeFactory.objectNode();

        if (bootstrapError.getErrors().isEmpty()) {
            node.put("status", "success");
        } else {
            node.put("status", "error");
        }
        TreeMap<String, Integer> numLoadedMap = bootstrapError.getNumRecordLoaded();
        Iterator<String> numLoadedMapIter = numLoadedMap.keySet().iterator();

        ArrayList<HashMap<String, Integer>> numLoadedList = new ArrayList<>();
        while (numLoadedMapIter.hasNext()) {
            String file = numLoadedMapIter.next();
            if (!file.equals("invalidDelete") && !file.equals("validDelete")) {
                int numLoaded = numLoadedMap.get(file);
                HashMap<String, Integer> map = new HashMap<>();
                map.put(file, numLoaded);
                numLoadedList.add(map);
            }

        }

        node.putPOJO("num-record-loaded", numLoadedList);
        if (!bootstrapError.getErrors().isEmpty()) {
            node.putPOJO("error", bootstrapError.getErrors());
        }

        return node;
    }

    /**
     * Routes JSON bootstrap requests and performs location-upload bootstrap
     * Returns a JSON object in the form of ObjectNode
     *
     * @param fileToUpload file to upload
     * @param token admin token
     * @return an ObjectNode representing the bootstrap with location data
     */
    @RequestMapping(value = {"/json/location-upload"}, method = RequestMethod.POST)
    @ResponseBody
    public ObjectNode locationUpload(@RequestParam("bootstrap-file") MultipartFile fileToUpload, String token) {

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ArrayList<String> errorList = new ArrayList<>();

        //checks if bootstrap-file is blank
        if (fileToUpload != null && fileToUpload.isEmpty()) {
            errorList.add("blank bootstrap-file");
        }

        //checks if current token is valid
        validateToken(errorList, token, true);
        for (String s : errorList) {
            System.out.println(s);
        }
        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }
        BootstrapError bootstrapError = bootstrapController.locationUpload(fileToUpload);

        if (bootstrapError == null) {
            node = nodeFactory.objectNode();
            node.put("status", "error");
            node.put("messages", "invalid bootstrap-file");
            return node;
        }

        node = nodeFactory.objectNode();

        if (bootstrapError.getErrors().isEmpty()) {
            node.put("status", "success");
        } else {
            node.put("status", "error");

        }
        TreeMap<String, Integer> numLoadedMap = bootstrapError.getNumRecordLoaded();
        Iterator<String> numLoadedMapIter = numLoadedMap.keySet().iterator();

        ArrayList<HashMap<String, Integer>> numLoadedList = new ArrayList<>();
        while (numLoadedMapIter.hasNext()) {
            String file = numLoadedMapIter.next();
            if (!file.equals("invalidDelete") && !file.equals("validDelete")) {
                int numLoaded = numLoadedMap.get(file);
                HashMap<String, Integer> map = new HashMap<>();
                map.put(file, numLoaded);
                numLoadedList.add(map);
            }

        }

        node.putPOJO("num-record-loaded", numLoadedList);

        if (!bootstrapError.getErrors().isEmpty()) {
            node.putPOJO("error", bootstrapError.getErrors());
        }
        int numDelete = 0;
        int numNotFound = 0;
        if (numLoadedMap.containsKey("validDelete") && numLoadedMap.containsKey("invalidDelete")) {
            numDelete = numLoadedMap.get("validDelete");
            numNotFound = numLoadedMap.get("invalidDelete");

            node.put("num-record-deleted", numDelete);
            node.put("num-record-not-found", numNotFound);
        }

        return node;

    }

    /**
     * Directs files to be added to BootstrapController Returns a JSON object at
     * the end which shows the number of records loaded for each file and the
     * number of records deleted if location-delete.csv is present.
     *
     * @param fileToUpload file to upload
     * @param token admin token
     * @return an ObjectNode representing the bootstrap with upload additional
     * app usage & demographics data
     */
    @RequestMapping(value = "/json/update", method = RequestMethod.POST)
    @ResponseBody
    public ObjectNode addData(@RequestParam("bootstrap-file") MultipartFile fileToUpload, String token) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();
        BootstrapError bootstrapError = bootstrapController.addData(fileToUpload);
        TreeMap<String, Integer> numLoadedMap = bootstrapError.getNumRecordLoaded();
        Iterator<String> numLoadedMapIter = numLoadedMap.keySet().iterator();

        if (bootstrapError.getErrors().isEmpty()) {
            node.put("status", "success");
        } else {
            node.put("status", "error");
        }

        ArrayNode nodeArr = nodeFactory.arrayNode();
        while (numLoadedMapIter.hasNext()) {
            String file = numLoadedMapIter.next();
            int numLoaded = numLoadedMap.get(file);

            ObjectNode child = nodeFactory.objectNode();
            child.put(file, numLoaded);
            nodeArr.addPOJO(child);
        }

        node.putPOJO("num-record-loaded", nodeArr);

        if (!bootstrapError.getErrors().isEmpty()) {
            node.putPOJO("error", bootstrapError.getErrors());
        }

        return node;
    }

    /**
     * Routes JSON topKApp request and performs calculation for Top-k most used
     * apps (given a school) Returns a JSON object in the form of ObjectNode
     *
     * @param k top K Value input by user
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param school selected school (SIS, Business, Economics, Accountancy,
     * SOCSC, Law)
     * @param token token generated based on logged in user
     * @return an ObjectNode representing the Top-k most used apps (given a
     * school) report
     */
    @RequestMapping(value = "/json/top-k-most-used-apps", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getTopKApp(String k, @RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, String school, String token) {
        String username = null;
        ArrayList<String> errorList = new ArrayList<>();

        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();

        commonValidationDate(errorList, startDate, endDate);

        if (school == null) {
            errorList.add("missing school");
        }

        if (k != null && k.equals("")) {
            errorList.add("blank k");
        }
        if (school != null && school.equals("")) {
            errorList.add("blank school");
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        int numKValue = 0;
        if (k != null && !k.equals("")) {
            try {
                numKValue = Integer.parseInt(k);

                if (numKValue < 1 || numKValue > 10) {
                    errorList.add("invalid k");
                }
            } catch (NumberFormatException e) {
                errorList.add("invalid k");
            }

        } else {
            k = "3";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        try {
            parsedStartDate = sdf.parse(startDate + " 00:00:00");
        } catch (ParseException e) {
            errorList.add("invalid startdate");
        }

        try {
            parsedEndDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            errorList.add("invalid enddate");
        }

        if (parsedEndDate != null && parsedStartDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        if (!school.matches("sis|economics|business|socsc|law|accountancy")) {
            errorList.add("invalid school");
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("errors", errorList);
            return node;
        }
        ArrayList<TopKApp> topKList = topKController.retrieveTopKApps(parsedStartDate, parsedEndDate, school, k);

        node.put("status", "success");

        ArrayNode arrNode = nodeFactory.arrayNode();

        for (TopKApp topKApp : topKList) {
            ObjectNode objNode = nodeFactory.objectNode();
            objNode.put("rank", topKApp.getRank());
            objNode.put("app-name", topKApp.getName());
            objNode.put("duration", topKApp.getDuration());
            arrNode.add(objNode);
        }

        node.putPOJO("results", arrNode);

        return node;

    }

    /**
     * Routes JSON topKStudent request and performs calculation for Top-k
     * students with most app usage (given an app category) Returns a JSON
     * object in the form of ObjectNode
     *
     * @param k top K Value input by user
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param appCategory selected App Category from the list: (Books, Social,
     * Education, Entertainment, Information, Library, Local, Tools, Fitness,
     * Games, Others)
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing the Top-k students with most app usage
     * (given an app category) report
     */
    @RequestMapping(value = "/json/top-k-most-used-students", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getTopKStudent(String k, @RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, @RequestParam(value = "appcategory", required = false) String appCategory, String token) {

        String username = null;

        ArrayList<String> errorList = new ArrayList<>();

        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();

        commonValidationDate(errorList, startDate, endDate);

        if (appCategory == null) {
            errorList.add("missing appcategory");
        }

        if (k != null && k.equals("")) {
            errorList.add("blank k");
        }
        if (appCategory != null && appCategory.equals("")) {
            errorList.add("blank appcategory");
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        int numKValue = 0;
        if (k != null && !k.equals("")) {
            try {
                numKValue = Integer.parseInt(k);

                if (numKValue < 1 || numKValue > 10) {
                    errorList.add("invalid k");
                }
            } catch (NumberFormatException e) {
                errorList.add("invalid k");
            }

        } else {
            k = "3";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        try {
            parsedStartDate = sdf.parse(startDate + " 00:00:00");
        } catch (ParseException e) {
            errorList.add("invalid startdate");
        }

        try {
            parsedEndDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            errorList.add("invalid enddate");
        }

        if (parsedEndDate != null && parsedStartDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        if (!appCategory.matches("(books|social|education|entertainment|information|library|local|tools|fitness|games|others)")) {
            errorList.add("invalid app category");
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("errors", errorList);
            return node;
        }
        ArrayList<TopKStudent> topKList = topKController.retrieveTopKStudents(parsedStartDate, parsedEndDate, appCategory, k);

        node = nodeFactory.objectNode();

        node.put("status", "success");
        node.putPOJO("results", topKList);

        return node;

    }

    /**
     * Routes JSON top-k-most-used-schools request and performs calculation for
     * top K schools with most app usage (given an app category). Returns a JSON
     * object in the form of ObjectNode
     *
     * @param k top K Value input by user
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param appCategory selected App Category from the list: (Books, Social,
     * Education, Entertainment, Information, Library, Local, Tools, Fitness,
     * Games, Others)
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing the Top-k schools with most app usage
     * (given an app category) report
     */
    @RequestMapping(value = "/json/top-k-most-used-schools", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getTopKSchool(String k, @RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, @RequestParam(value = "appcategory", required = false) String appCategory, String token) {

        String username = null;

        ArrayList<String> errorList = new ArrayList<>();

        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();

        if (appCategory == null) {
            errorList.add("missing appcategory");
        }

        commonValidationDate(errorList, startDate, endDate);

        if (k != null && k.equals("")) {
            errorList.add("blank k");
        }
        if (appCategory != null && appCategory.equals("")) {
            errorList.add("blank appcategory");
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }
        int numKValue = 0;
        if (k != null && !k.equals("")) {
            try {
                numKValue = Integer.parseInt(k);

                if (numKValue < 1 || numKValue > 10) {
                    errorList.add("invalid k");
                }
            } catch (NumberFormatException e) {
                errorList.add("invalid k");
            }

        } else {
            k = "3";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        try {
            parsedStartDate = sdf.parse(startDate + " 00:00:00");
        } catch (ParseException e) {
            errorList.add("invalid startdate");
        }

        try {
            parsedEndDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            errorList.add("invalid enddate");
        }

        if (parsedEndDate != null && parsedStartDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        if (!appCategory.matches("(books|social|education|entertainment|information|library|local|tools|fitness|games|others)")) {
            errorList.add("invalid app category");
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("errors", errorList);
            return node;
        }

        ArrayList<TopKSchool> schoolList = topKController.retrieveTopKSchools(parsedStartDate, parsedEndDate, appCategory, k);

        node = nodeFactory.objectNode();

        node.put("status", "success");
        node.putPOJO("results", schoolList);

        return node;

    }

    /**
     * This method returns the JSON output for Basic App Usage Breakdown by
     * usage time category.
     *
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing the basic app breakdown by usage time
     * category report
     */
    @RequestMapping(value = "/json/basic-usetime-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode basicApp(@RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, String token) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();

        ArrayList<String> errorList = new ArrayList<>();
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        String username = null;
        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }
        if (startDate == null) {
            errorList.add("missing startdate");
        }
        if (endDate == null) {
            errorList.add("missing enddate");
        }

        if (startDate != null && startDate.equals("")) {
            errorList.add("blank startdate");
        }
        if (endDate != null && endDate.equals("")) {
            errorList.add("blank enddate");
        }

        //return error messages if either token invalid, startDate null or endDate null
        if (errorList.size() > 0) {
            Collections.sort(errorList);
            return node.put("status", "error").putPOJO("messages", errorList);
        }

        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid startdate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedStartDate = sdf.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
                errorList.add("invalid startdate");
            }
        }

        if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid enddate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedEndDate = sdf.parse(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                errorList.add("invalid enddate");
            }
        }

        if (parsedStartDate != null && parsedEndDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        if (errorList.size() > 0) {
            return node.put("status", "error").putPOJO("messages", errorList);
        }

        HashMap<String, Integer> appHashMap = appController.retrieveAppByStartEndDate(startDate, endDate);

        HashMap<String, Integer> usageCategoryHashMap = appController.retrieveUsageCategory(appHashMap);

        int numberOfUserIntense = 0;
        int numberOfUserNormal = 0;
        int numberOfUserMild = 0;
        if (usageCategoryHashMap.containsKey("Intense")) {
            numberOfUserIntense = usageCategoryHashMap.get("Intense");
        }

        if (usageCategoryHashMap.containsKey("Normal")) {
            numberOfUserNormal = usageCategoryHashMap.get("Normal");
        }

        if (usageCategoryHashMap.containsKey("Mild")) {
            numberOfUserMild = usageCategoryHashMap.get("Mild");
        }

        double total = numberOfUserIntense + numberOfUserNormal + numberOfUserMild;
        nodeArr.addObject().put("intense-count", numberOfUserIntense)
                .put("intense-percent", (int) Math.round(numberOfUserIntense / total * 100));

        nodeArr.addObject().put("normal-count", numberOfUserNormal)
                .put("normal-percent", (int) Math.round(numberOfUserNormal / total * 100));

        nodeArr.addObject().put("mild-count", numberOfUserMild)
                .put("mild-percent", (int) Math.round(numberOfUserMild / total * 100));

        node.put("status", "success");
        node.putPOJO("breakdown", nodeArr);

        return node;
    }

    /**
     * This method returns the JSON output for Basic App Usage Report Breakdown
     * by usage time category and demographics
     *
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param order order of year/gender/school/cca category selected
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing the basic usage time category and
     * demographics report
     */
    @RequestMapping(value = "/json/basic-usetime-demographics-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode basicAppBreakdown(@RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, String order, String token) {

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();

        ArrayList<String> errorList = new ArrayList<>();
        GregorianCalendar calendar = new GregorianCalendar();

        Date parsedStartDate = null;
        Date parsedEndDate = null;

        String username = null;

        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }
        if (startDate == null) {
            errorList.add("missing startdate");
        }
        if (endDate == null) {
            errorList.add("missing enddate");
        }

        if (startDate != null && startDate.equals("")) {
            errorList.add("blank startdate");
        }
        if (endDate != null && endDate.equals("")) {
            errorList.add("blank enddate");
        }

        if (order == null) {
            errorList.add("missing order");
        }

        if (order != null && order.equals("")) {
            errorList.add("blank order");
        }

        //return error messages if either token invalid, startDate null or endDate null
        if (errorList.size() > 0) {
            Collections.sort(errorList);
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid startdate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedStartDate = sdf.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
                errorList.add("invalid startdate");
            }
        }
        if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid enddate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedEndDate = sdf.parse(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                errorList.add("invalid enddate");
            }
        }

        if (parsedStartDate != null && parsedEndDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        boolean orderValid = true;

        int schoolCount = 0;
        int genderCount = 0;
        int yearCount = 0;
        int ccaCount = 0;
        String[] catArray = order.split(",");
        for (String s : catArray) {

            s = s.toLowerCase();
            if (!s.equals("gender") && !s.equals("school") && !s.equals("year") && !s.equals("cca")) {
                orderValid = false;
            }
            if (s.equals("year")) {
                yearCount++;
            }
            if (s.equals("gender")) {
                genderCount++;
            }
            if (s.equals("school")) {
                schoolCount++;
            }
            if (s.equals("cca")) {
                ccaCount++;
            }

        }

        if (yearCount > 1 || genderCount > 1 || schoolCount > 1 || ccaCount > 1) {
            orderValid = false;
        }
        int comma = 0;
        for (int i = 0; i < order.length(); i++) {
            char curChar = order.charAt(i);
            if (curChar == ',') {
                comma++;
            }
        }

        if (catArray.length - 1 != comma) {
            orderValid = false;
        }

        if (!orderValid) {
            errorList.add("invalid order");
        }

        if (errorList.size() > 0) {
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        System.out.println(order);
        BasicAppUsage basicAppUsage = appController
                .retrieveAppUsageByDemographicBreakdown(startDate, endDate, order);
        List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();

        for (BasicAppUsage currBasicAppUsage : basicAppUsageList) {
            putBasicApp(currBasicAppUsage, nodeArr);
        }

        node.put("status", "success");
        node.putPOJO("breakdown", nodeArr);
        return node;
    }

    /**
     * This method creates the sub-categories for the Basic App Usage Report
     * Breakdown by usage time category and demographics JSON return object.
     *
     * @param basicAppUsage BasicAppUsage Object
     * @param nodeArr ArrayNode to store child object nodes
     *
     */
    public void putBasicApp(BasicAppUsage basicAppUsage, ArrayNode nodeArr) {

        List<BasicAppUsage> basicAppUsageList = basicAppUsage.getBasicAppUsageList();
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        if (basicAppUsageList == null) {
            String category = basicAppUsage.getCategory();
            String sortCategory = basicAppUsage.getSortCategory();
            int count = basicAppUsage.getCount();
            int percent = basicAppUsage.getPercentage();
            List<LinkedHashMap<String, Integer>> categoryListResult = basicAppUsage.getCategoryListResult();

            ObjectNode child = nodeFactory.objectNode();

            child.put(category, sortCategory);
            child.put("count", count);
            child.put("percent", percent);
            child.putPOJO("breakdown", categoryListResult);
            nodeArr.addPOJO(child);

        } else {

            ArrayNode currNodeArr = nodeFactory.arrayNode();
            ObjectNode child = nodeFactory.objectNode();
            String category = basicAppUsage.getCategory();
            String sortCategory = basicAppUsage.getSortCategory();
            int count = basicAppUsage.getCount();
            int percent = basicAppUsage.getPercentage();
            if (category.equals("year")) {
                child.put(category, Integer.parseInt(sortCategory));
            } else {
                child.put(category, sortCategory);
            }
            child.put("count", count);
            child.put("percent", percent);

            for (BasicAppUsage currBasicAppUsage : basicAppUsageList) {

                putBasicApp(currBasicAppUsage, currNodeArr);

            }

            child.putPOJO("breakdown", currNodeArr);
            nodeArr.addPOJO(child);

        }

    }

    /**
     * This method returns the JSON output for Basic App Usage Report Breakdown
     * by App Category
     *
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing the basic app category report
     */
    @RequestMapping(value = "/json/basic-appcategory-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode basicAppUsageAppCat(@RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate, String token) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();

        ArrayList<String> errorList = new ArrayList<String>();
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        String username = null;
        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }

        if (startDate == null) {
            errorList.add("missing startdate");
        }
        if (endDate == null) {
            errorList.add("missing enddate");
        }

        if (startDate != null && startDate.equals("")) {
            errorList.add("blank startdate");
        }
        if (endDate != null && endDate.equals("")) {
            errorList.add("blank enddate");
        }

        //return error messages if either token invalid, startDate null or endDate null
        if (errorList.size() > 0) {
            Collections.sort(errorList);
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid startdate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedStartDate = sdf.parse(startDate);
            } catch (ParseException e) {
                errorList.add("invalid startdate");
            }

        }
        if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid enddate");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedEndDate = sdf.parse(endDate);
            } catch (ParseException e) {
                errorList.add("invalid enddate");
            }
        }

        if (parsedStartDate != null && parsedEndDate != null && parsedStartDate.after(parsedEndDate)) {
            errorList.add("invalid startdate");
        }

        if (errorList.size() > 0) {
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        LinkedHashMap<String, Double> appCategoryResult = appController.retrieveAppUsageByCategory(startDate, endDate);
        Iterator<String> iter = appCategoryResult.keySet().iterator();
        ArrayList<Double> valueList = new ArrayList<Double>(appCategoryResult.values());
        double sum = 0;
        for (double value : valueList) {
            sum += value;
        }
        while (iter.hasNext()) {
            String category = iter.next();
            ObjectNode childNode = nodeFactory.objectNode();
            double appDuration = appCategoryResult.get(category);
            childNode.put("category-name", category);
            childNode.put("category-duration", (int) Math.round(appDuration));

            int avgTime = 0;

            if (sum != 0) {
                avgTime = (int) Math.round(100.0 * appCategoryResult.get(category) / sum);
            }
            childNode.put("category-percent", avgTime);
            nodeArr.addPOJO(childNode);

        }
        node.put("status", "success");
        node.putPOJO("breakdown", nodeArr);

        return node;
    }

    /**
     * This method returns the JSON output for Basic App Usage Report Diurnal
     * Pattern of app usage time.
     *
     * @param date date in yyyy-mm-dd format
     * @param genderFilter Gender input by the user, e.g. Male (M) or Female (F)
     * @param schoolFilter selected school (SIS, Business, Economics,
     * Accountancy, SOCSC, Law)
     * @param yearFilter selected year (2011, 2012, 2013, 2014, 2015) etc.
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing basic diurnal pattern report
     */
    @RequestMapping(value = "/json/basic-diurnalpattern-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode basicAppDiurnalUsage(String date, @RequestParam(value = "genderfilter", required = false) String genderFilter, @RequestParam(value = "schoolfilter", required = false) String schoolFilter, @RequestParam(value = "yearfilter", required = false) String yearFilter, String token) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();

        ArrayList<String> errorList = new ArrayList<String>();
        Date parsedDate = null;

        String username = null;
        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }

        if (date == null) {
            errorList.add("missing date");
        }

        if (date != null && date.equals("")) {
            errorList.add("blank date");
        }

        if (schoolFilter == null) {
            errorList.add("missing schoolfilter");
        }

        if (schoolFilter != null && schoolFilter.equals("")) {
            errorList.add("blank schoolfilter");
        }

        if (yearFilter == null) {
            errorList.add("missing yearfilter");
        }

        if (yearFilter != null && yearFilter.equals("")) {
            errorList.add("blank yearfilter");
        }

        if (genderFilter == null) {
            errorList.add("missing genderfilter");
        }

        if (genderFilter != null && genderFilter.equals("")) {
            errorList.add("blank genderfilter");
        }

        //return error messages if either token invalid, startDate null or endDate null
        if (errorList.size() > 0) {
            Collections.sort(errorList);
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errorList.add("invalid date");
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                parsedDate = sdf.parse(date);
            } catch (ParseException e) {
                errorList.add("invalid date");
            }
        }

        List<Demographic> demographicList = demographicDAO.retrieveAll();

        int minYear = 0;
        int maxYear = 0;

        if (!yearFilter.equals("NA")) {

            for (int i = 0; i < demographicList.size(); i++) {
                Demographic curDemographic = demographicList.get(i);
                String email = curDemographic.getEmail();
                int indexAt = email.indexOf("@");
                String year = email.substring(indexAt - 4, indexAt);
                int intYear = Integer.parseInt(year);

                if (i == 0) {
                    minYear = intYear;
                    maxYear = intYear;
                }

                if (intYear < minYear) {
                    minYear = intYear;
                }

                if (intYear > maxYear) {
                    maxYear = intYear;
                }
            }
        }

        if (!yearFilter.equals("NA") && !yearFilter.matches("\\d{4}")) {
            errorList.add("invalid year filter");
        } else if (!yearFilter.equals("NA")) {
            int intYearFilter = Integer.parseInt(yearFilter);
            if (intYearFilter > maxYear || intYearFilter < minYear) {
                errorList.add("invalid year filter");
            }
        }

        if (!genderFilter.matches("m|f|M|F|NA")) {
            errorList.add("invalid gender filter");
        }

        if (!schoolFilter.matches("sis|economics|business|socsc|law|accountancy|NA")) {
            errorList.add("invalid school filter");
        }

        //return error messages
        if (errorList.size() > 0) {
            return node.put("status", "error")
                    .putPOJO("messages", errorList);
        }

        if (genderFilter.equals("NA")) {
            genderFilter = null;
        }

        if (schoolFilter.equals("NA")) {
            schoolFilter = null;
        }
        if (yearFilter.equals("NA")) {
            yearFilter = null;
        }

        LinkedHashMap<String, Integer> diurnalResult = appController.getBasicAppDiurnalUsage(date, genderFilter, schoolFilter, yearFilter);
        Iterator<String> iter = diurnalResult.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            nodeArr.addObject().put("period", key)
                    .put("duration", diurnalResult.get(key));
        }
        node.put("status", "success");
        node.putPOJO("breakdown", nodeArr);
        return node;
    }

    /**
     * Routes JSON overuse-report request and performs smartphone overuse
     * calculation based on user's mac address. Returns a JSON object in the
     * form of ObjectNode.
     *
     * @param startDate the start date of the input field
     * @param endDate the end date of the input field
     * @param macAddress MAC Address of user
     * @param token token generated based on logged-in user
     * @return an ObjectNode representing smartphone overuse report
     */
    @RequestMapping(value = "/json/overuse-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getSmartphoneOveruse(@RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate,
            @RequestParam(value = "macaddress", required = false) String macAddress, @RequestParam(value = "token", required = false) String token) {

        ArrayList<String> errorList = new ArrayList<>();

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();

        validateToken(errorList, token, false);
        commonValidationDate(errorList, startDate, endDate);
        commonValidationMacAddress(errorList, macAddress);

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        validateDate(errorList, startDate, endDate);
        Demographic demographic = null;
        if (macAddress.matches("[a-f0-9A-F]{40}")) {
            demographic = demographicDAO.getDemographicByMacAddress(macAddress);
        }
        if (demographic == null) {
            errorList.add("invalid macaddress");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        Date parsedStartDate = null;
        Date parsedEndDate = null;

        try {
            parsedStartDate = sdf.parse(startDate + " 00:00:00");
            parsedEndDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {

        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        SmartphoneOveruse smartphoneOveruse = smartphoneOveruseController.retrieveSmartphoneOveruse(parsedStartDate, parsedEndDate, demographic.getEmail());
        HashMap<String, Double> metrics = smartphoneOveruse.getMetrics();

        String[] index = {"Light", "Moderate", "Severe"};

        double gamingCategory = metrics.get("gamingCategory");
        int gamingCategoryInt = (int) gamingCategory;
        double gamingDuration = metrics.get("gamingDuration");
        int gamingDurationInt = (int) gamingDuration;

        double usageCategory = metrics.get("usageCategory");
        int usageCategoryInt = (int) usageCategory;
        double usageDuration = metrics.get("usageDuration");
        int usageDurationInt = (int) usageDuration;

        double accessFrequencyCategory = metrics.get("accessFrequencyCategory");
        int accessCategoryInt = (int) accessFrequencyCategory;
        double accessFrequency = metrics.get("accessFrequency");

        node = nodeFactory.objectNode();

        ObjectNode childNode = nodeFactory.objectNode();

        childNode.put("overuse-index", smartphoneOveruse.getOveruseIndex());

        node.put("status", "success");

        ArrayNode arrNode = nodeFactory.arrayNode();
        ObjectNode grandChildNode1 = nodeFactory.objectNode();
        grandChildNode1.put("usage-category", index[usageCategoryInt]);
        grandChildNode1.put("usage-duration", usageDurationInt);
        arrNode.add(grandChildNode1);

        ObjectNode grandChildNode2 = nodeFactory.objectNode();
        grandChildNode2.put("gaming-category", index[gamingCategoryInt]);
        grandChildNode2.put("gaming-duration", gamingDurationInt);
        arrNode.add(grandChildNode2);

        ObjectNode grandChildNode3 = nodeFactory.objectNode();
        grandChildNode3.put("accessfrequency-category", index[accessCategoryInt]);
        grandChildNode3.put("accessfrequency", accessFrequency);
        arrNode.add(grandChildNode3);

        childNode.putPOJO("metrics", arrNode);

        node.putPOJO("results", childNode);

        return node;

    }

    /**
     * This method returns the JSON output for usage-heatmap given the
     * respective parameters
     *
     * @param date date in yyyy-mm-dd format
     * @param time time in hh:mm:ss format
     * @param floor floors in SIS, L2, L3 etc
     * @param token token of authenticated user
     * @return an ObjectNode representing the usage-heatmap output
     */
    @RequestMapping(value = "/json/usage-heatmap", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getUsageHeatmap(String date, String time, String floor, String token) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ArrayList<String> errorList = new ArrayList<String>();

        ObjectNode node = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();
        commonValidationDate(errorList, date);
        String username = null;

        if (time != null && time.equals("")) {
            errorList.add("blank time");
        } else if (time == null) {
            errorList.add("missing time");
        }

        if (floor != null && floor.equals("")) {
            errorList.add("blank floor");
        } else if (floor == null) {
            errorList.add("missing floor");
        }

        if (token != null && token.equals("smuaAPP")) {
            username = "smuaAPP";
        } else {
            validateToken(errorList, token, false);
        }
        //common-validation messages
        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        String selectedFloor = null;
        switch (floor) {
            case "0":
                selectedFloor = "B1";
                break;
            case "1":
                selectedFloor = "L1";
                break;
            case "2":
                selectedFloor = "L2";
                break;
            case "3":
                selectedFloor = "L3";
                break;
            case "4":
                selectedFloor = "L4";
                break;
            case "5":
                selectedFloor = "L5";
                break;
        }

        if (selectedFloor == null) {
            errorList.add("invalid floor");
        }
        validateDate(errorList, date);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//yyyy-MM-dd HH:mm:ss  HH:mm:ss
        if (time.matches("\\d{2}:\\d{2}:\\d{2}")) {
            try {
                sdf.setLenient(false);
                sdf.parse(time);
            } catch (ParseException e) {
                errorList.add("invalid time");
            }
        }else{
            errorList.add("invalid time");
        }

        if (!errorList.isEmpty()) {

            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss  HH:mm:ss
        Date timestamp = null;

        try {
            timestamp = sdf.parse(date + " " + time);
        } catch (ParseException e) {

        }

        List<UsageHeatmap> usageHeatmapList = heatmapController.getSmartphoneHeatmap(timestamp, selectedFloor);
        //formats the JSON for output
        for (UsageHeatmap usageHeatmap : usageHeatmapList) {
            nodeArr.addObject().put("semantic-place", usageHeatmap.getSemanticPlace())
                    .put("num-people-using-phone", usageHeatmap.getNumberOfpeopleUsingPhone())
                    .put("crowd-density", usageHeatmap.getCrowdDensity());
        }
        node.put("status", "success");
        node.putPOJO("heatmap", nodeArr);

        return node;
    }

    /**
     * This method returns the JSON output for social-activeness-report
     *
     * @param date date in yyyy-mm-dd object
     * @param token token of authenticated user
     * @param macAddress MAC Address of user
     * @return an ObjectNode representing the social-activeness-report output
     */
    @RequestMapping(value = "/json/social-activeness-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getSocialActivenessReport(String date, String token, @RequestParam(value = "macaddress", required = false) String macAddress) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ObjectNode childNode = nodeFactory.objectNode();
        ArrayNode nodeArr = nodeFactory.arrayNode();
        ArrayList<String> errorList = new ArrayList<String>();

        validateToken(errorList, token, false);

        if (date == null) {
            errorList.add("missing date");
        } else if (date.equals("")) {
            errorList.add("blank date");
        }

        if (macAddress == null) {
            errorList.add("missing macaddress");
        } else if (macAddress.equals("")) {
            errorList.add("blank macaddress");
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//yyyy-MM-dd HH:mm:ss  HH:mm:ss
        Date dateFormat = null;

        try {
            sdf.setLenient(false);
            dateFormat = sdf.parse(date);
        } catch (ParseException e) {
            errorList.add("invalid date");
        }

        if (!macAddress.matches("[a-f0-9A-F]{40}")) {
            errorList.add("invalid macaddress");
        } else {
            if (!smuaUtility.validateMacAddress(macAddress)) {
                errorList.add("invalid macaddress");
            }
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        String startDateTime = date + " 00:00:00";
        String endDateTime = date + " 23:59:59";

        Date startDate = null;
        Date endDate = null;
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            startDate = sdf.parse(startDateTime);
            endDate = sdf.parse(endDateTime);
        } catch (ParseException e) {
            System.out.println("Error in formatting dates");
        }

        System.out.println("DATE : " + startDate + " eDATE : " + endDate + " macAddress : " + macAddress);
        LinkedHashMap<String, Integer> socialActivenessReport = socialActivenessController.getSocialActivenessReport(startDate, endDate, macAddress);
        TreeMap<String, Integer> socialAppUsageTime = socialActivenessController.getSocialAppUsageTime(startDate, endDate, macAddress);

        int totalAppUsage = 0;
        for (int i : socialAppUsageTime.values()) {
            totalAppUsage += i;
        }

        Iterator<String> socialAppIter = socialAppUsageTime.keySet().iterator();
        while (socialAppIter.hasNext()) {
            String key = socialAppIter.next();
            int duration = socialAppUsageTime.get(key);
            nodeArr.addObject().put("app-name", key).put("percent", Math.round(1.0f * duration / totalAppUsage * 100));
        }
        childNode.put("total-social-app-usage-duration", totalAppUsage);
        childNode.putPOJO("individual-social-app-usage", nodeArr);

        childNode.put("total-time-spent-in-sis", socialActivenessReport.get("total-time-spent-in-sis"));
        childNode.put("group-percent", socialActivenessReport.get("group-percent"));
        childNode.put("solo-percent", socialActivenessReport.get("solo-percent"));

        node.put("status", "success");
        node.putPOJO("results", childNode);

        return node;
    }

    /**
     * This method delete records with the specified criteria, start date is
     * mandatory
     *
     * @param macAddress macaddress to be deleted
     * @param startDate startdate of the deletion period
     * @param startTime startTime of the deletion period
     * @param endDate endDate of the deletion period
     * @param endTime endTime of the deletion period
     * @param locationID locationID to be deleted
     * @param semanticPlace semanticPlace to be deleted
     * @param token token value
     * @return an ObjectNode representing location delete
     */
    @RequestMapping(value = "/json/location-delete", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode locationDelete(@RequestParam(value = "macaddress", required = false) String macAddress, @RequestParam(value = "startdate", required = false) String startDate,
            @RequestParam(value = "starttime", required = false) String startTime, @RequestParam(value = "enddate", required = false) String endDate,
            @RequestParam(value = "endtime", required = false) String endTime, @RequestParam(value = "location-id", required = false) String locationID, @RequestParam(value = "semantic-place", required = false) String semanticPlace,
            String token) {

        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        ArrayList<String> errorList = new ArrayList<>();

        validateToken(errorList, token, true);

        if (startDate == null) {
            errorList.add("missing startdate");
        }

        if (startDate != null && startDate.equals("")) {
            errorList.add("blank startdate");
        }
        if (endDate != null && endDate.equals("")) {
            errorList.add("blank enddate");
        }
        if (macAddress != null && macAddress.equals("")) {
            errorList.add("blank macaddress");
        }
        if (startTime != null && startTime.equals("")) {
            errorList.add("blank starttime");
        }
        if (endTime != null && endTime.equals("")) {
            errorList.add("blank endtime");
        }
        if (locationID != null && locationID.equals("")) {
            errorList.add("blank location-id");
        }
        if (semanticPlace != null && semanticPlace.equals("")) {
            errorList.add("blank semantic place");
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        if (startTime == null) {
            startTime = "00:00:00";
        }

        if (semanticPlace == null) {
            semanticPlace = "";
        }

        if (macAddress == null) {
            macAddress = "";
        }

        if (locationID == null) {
            locationID = "";
        }

        Date checkStartDate = null;
        Date checkEndDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            checkStartDate = sdf.parse(startDate);
        } catch (ParseException ex) {
            errorList.add("invalid startdate");
        }

        Date checkStartTime = null;
        Date checkEndTime = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setLenient(false);
            checkStartTime = sdf.parse(startTime);
        } catch (ParseException ex) {
            errorList.add("invalid starttime");
        }

        if (endDate != null) {

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                checkEndDate = sdf.parse(endDate);
            } catch (ParseException ex) {
                errorList.add("invalid enddate");
            }

            if (endTime == null) {
                endTime = "23:59:59";
            }

            try {
                endDate += " " + endTime;
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
                sdfTime.setLenient(false);
                checkEndTime = sdfTime.parse(endTime);
            } catch (ParseException ex) {
                errorList.add("invalid endtime");
            }
        } else {
            if (endTime != null) {
                errorList.add("invalid endtime");
            } else {
                endDate = startDate;
                endTime = "23:59:59";

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    checkEndDate = sdf.parse(endDate);
                } catch (ParseException ex) {

                }

                try {
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
                    sdfTime.setLenient(false);
                    checkEndTime = sdfTime.parse(endTime);
                } catch (ParseException ex) {
                }
            }
        }

        if (checkStartDate != null && checkStartTime != null) {
            try {
                startDate += " " + startTime;
                SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdfTime.setLenient(false);
                checkStartDate = sdfTime.parse(startDate);
            } catch (ParseException ex) {
            }

        }
        if (checkEndDate != null && checkEndTime != null) {
            try {
                endDate += " " + endTime;
                SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdfTime.setLenient(false);
                checkEndDate = sdfTime.parse(endDate);
            } catch (ParseException ex) {
            }
        }

        if (checkEndDate != null && checkStartDate != null && checkEndDate.before(checkStartDate)) {
            errorList.add(0, "invalid startdate");
            errorList.add(1, "invalid starttime");
        }

        //regex expression to validate that mac address is valid
        if (!macAddress.equals("") && !macAddress.matches("[a-f0-9A-F]{40}")) {
            errorList.add("invalid macaddress");
        } else if (!macAddress.equals("")) {
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

        if (locationLookup != null && !semanticPlace.equals("")) {
            if (!locationLookup.getSemanticPlace().equals(semanticPlace)) {
                errorList.add("invalid location-id");
            }
        }

        if (!semanticPlace.equals("")) {
            List<LocationLookup> locationLookupList = locationLookupDAO.retrieveBySemanticPlace(semanticPlace);
            if (locationLookupList.isEmpty()) {
                errorList.add("invalid semantic place");
            }
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        List<Location> locationList = locationDAO.retrieveAllLocation(checkStartDate, checkEndDate, macAddress, intLocationID, semanticPlace);

        locationDAO.deleteLocation(locationList);
        int rowDeleted = locationList.size();

        ArrayNode arrNode = nodeFactory.arrayNode();

        for (Location location : locationList) {
            ObjectNode objNode = nodeFactory.objectNode();
            objNode.put("location-id", location.getLocationLookup().getLocationID());
            objNode.put("mac-address", location.getLocationPK().getMacAddress());
            Date timestamp = location.getLocationPK().getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objNode.put("timestamp", sdf.format(timestamp));
            arrNode.add(objNode);
        }

        node = nodeFactory.objectNode();
        node.put("status", "success");
        node.put("num-record-deleted", rowDeleted);

        node.putPOJO("rows-deleted", arrNode);
        return node;

    }

    /**
     * This method returns an advanced smartphone overuse report based on the
     * input from the user
     *
     * @param startDate start date of the app specified for the advanced
     * smartphone report
     * @param endDate end date of the app specified for the advanced smartphone
     * report
     * @param macAddress user's macaddres specified to retrieve the advanced
     * smartphone report
     * @param token user's token
     * @return an ObjectNode representing an advanced overuse report
     */
    @RequestMapping(value = "/json/advanced-overuse-report", method = RequestMethod.GET)
    @ResponseBody
    public ObjectNode getAdvancedSmartphoneOveruseReport(@RequestParam(value = "startdate", required = false) String startDate, @RequestParam(value = "enddate", required = false) String endDate,
            @RequestParam(value = "macaddress", required = false) String macAddress, @RequestParam(value = "token", required = false) String token) {

        //common validations;
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode node = nodeFactory.objectNode();
        ArrayList<String> errorList = new ArrayList<>();

        if (startDate == null) {
            errorList.add("missing startdate");

        } else if (startDate.equals("")) {
            errorList.add("blank startdate");
        }

        if (endDate == null) {
            errorList.add("missing enddate");

        } else if (endDate.equals("")) {
            errorList.add("blank enddate");
        }

        if (macAddress == null) {
            errorList.add("missing macaddress");

        } else if (macAddress.equals("")) {
            errorList.add("blank macaddress");
        }

        String username = null;
        if (token == null) {
            errorList.add("missing token");
        } else if (token.equals("")) {
            errorList.add("blank token");
        } else {
            try {
                username = JWTUtility.verify(token, "pongpongpongpong");
            } catch (JWTException e) {

            }
            if (username == null) {
                errorList.add("invalid token");
            }
        }

        if (!errorList.isEmpty()) {
            Collections.sort(errorList);
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        Date startingDate = null;
        Date endingDate = null;

        try {
            startingDate = sdf.parse(startDate);
        } catch (ParseException e) {
            errorList.add("invalid startdate");
        }

        try {
            endingDate = sdf.parse(endDate);
        } catch (ParseException e) {
            errorList.add("invalid enddate");
        }

        if (!macAddress.matches("[a-f0-9A-F]{40}") || !smuaUtility.validateMacAddress(macAddress)) {
            errorList.add("invalid macaddress");
        }

        if (!errorList.isEmpty()) {
            node.put("status", "error");
            node.putPOJO("messages", errorList);
            return node;
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            startingDate = sdf.parse(startDate + " 00:00:00");
            endingDate = sdf.parse(endDate + " 23:59:59");
        } catch (ParseException e) {
            errorList.add("invalidate enddate");
        }
        macAddress = macAddress.toLowerCase();
        HashMap<String, String> outputMap = advSmartphoneOveruseController.getAdvSmartphoneReport(startingDate, endingDate, macAddress);
        ObjectNode child = nodeFactory.objectNode();
        child.put("overuse-index", outputMap.get("overuseIndex"));
        child.put("total-class-duration", Integer.parseInt(outputMap.get("classTime")));
        child.put("total-small-group-duration", Integer.parseInt(outputMap.get("smallGroupTime")));
        child.put("non-productive-app-duration", Integer.parseInt(outputMap.get("unproductiveAppUsage")));
        node.put("status", "success");
        node.putPOJO("results", child);

        return node;

    }

    /**
     * Creates a new token based on the username provided
     *
     * @param username Username of the logged-in user
     * @return String token generated based on sharedSecret and username
     */
    public String JWTGenerator(String username) {
        String sharedSecret = "pongpongpongpong";
        return JWTUtility.sign(sharedSecret, username);
    }

    /**
     * This method is a common method to validate if the token is missing or
     * invalid.
     *
     * @param errorList Arraylist that stores the error msgs
     * @param token token of the user
     * @param isAdmin true if the user is admin, false is it is a normal user
     */
    public void validateToken(ArrayList<String> errorList, String token, boolean isAdmin) {
        String username = null;
        System.out.println("Validate Token");
        if (token == null) {
            System.out.println("MISSING TOKEN");
            errorList.add("missing token");
        } else if (token.equals("")) {
            System.out.println("BLANK TOKEN");
            errorList.add("blank token");
        } else {
            System.out.println("VERIFY TOKEN");
            try {
                username = JWTUtility.verify(token, "pongpongpongpong");
            } catch (JWTException e) {
            }
            if (username == null) {
                errorList.add("invalid token");
            }
            System.out.println(username);
            if (isAdmin && username != null && !username.equals("admin")) {
                errorList.add("invalid token");
            }
        }
    }

    /**
     * This methods is a common validation methods that checks for any empty
     * date field
     *
     * @param errorList Arraylist that stores error messages
     * @param startDate start date of the app data specified by the user
     * @param endDate end date of the app data specified by the user
     */
    public void commonValidationDate(ArrayList<String> errorList, String startDate, String endDate) {
        if (startDate == null) {
            errorList.add("missing startdate");

        } else if (startDate.equals("")) {
            errorList.add("blank startdate");
        }

        if (endDate == null) {
            errorList.add("missing enddate");

        } else if (endDate.equals("")) {
            errorList.add("blank enddate");
        }
    }

    /**
     * This is a common method to validate if the date field is missing
     *
     * @param errorList ArrayList that stores error messages
     * @param date date of the app data specified by the user
     */
    public void commonValidationDate(ArrayList<String> errorList, String date) {
        if (date != null && date.equals("")) {
            errorList.add("blank date");
        } else if (date == null) {
            errorList.add("missing date");
        }
    }

    /**
     * This method validates if the date input is invalid
     *
     * @param errorList Arraylist that stores the error messages
     * @param date date of app specified by the user
     */
    public void validateDate(ArrayList<String> errorList, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//yyyy-MM-dd HH:mm:ss  HH:mm:ss
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                sdf.setLenient(false);
                sdf.parse(date);
            } catch (ParseException e) {
                errorList.add("invalid date");
            }
        } else {
            errorList.add("invalid date");
        }
    }

    /**
     * This methods validates if the start date and end date input by the user
     * is valid
     *
     * @param errorList Arraylist that stores the error messages
     * @param startDate start date of the app specified by the user
     * @param endDate end date of the app specified by the user
     */
    public void validateDate(ArrayList<String> errorList, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        Date formattedStartDate = null;
        Date formattedEndDate = null;
        try {
            formattedStartDate = sdf.parse(startDate);
        } catch (ParseException e) {
            errorList.add("invalid startdate");
        }
        try {
            formattedEndDate = sdf.parse(endDate);
        } catch (ParseException e) {
            errorList.add("invalid enddate");
        }
        if (formattedStartDate != null && formattedEndDate != null && formattedStartDate.after(formattedEndDate)) {
            errorList.add("invalid startdate");
        }
    }

    /**
     * This method validates if the macaddress specified by the user is present
     *
     * @param errorList Arraylist that stores the error message
     * @param macAddress user's macaddress to be validated
     */
    public void commonValidationMacAddress(ArrayList<String> errorList, String macAddress) {
        if (macAddress == null) {
            errorList.add("missing macaddress");
        } else if (macAddress != null && macAddress.equals("")) {
            errorList.add("blank macaddress");
        }
    }

}
