/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import smua.models.DAO.AppLookupDAO;
import smua.models.Entities.*;
import smua.models.DAO.*;
import smua.models.JSON.*;
import smua.models.PK.*;
import au.com.bytecode.opencsv.*;

/**
 *
 * @author r128
 */
@Controller
public class BootstrapController {

    @Autowired
    private AppLookupDAO appLookupDAO;

    @Autowired
    private DemographicDAO demographicDAO;

    @Autowired
    private AppDAO appDAO;

    @Autowired
    private LocationLookupDAO locationLookupDAO;

    @Autowired
    private LocationDAO locationDAO;

    /**
     * Bootstraps a zip file and uploads its contents into database for files
     * that match the required filename, this methods takes in only 3 csv files,
     * which are demographics, app-lookup and csv
     *
     * @param fileToUpload file to upload
     * @return BootstrapError object
     */
    public BootstrapError bootstrap(MultipartFile fileToUpload) {
        boolean filesUploaded = false;
        ZipEntry entry = null;

        //stores the csv files into an array
        String[] fileNames = {"demographics.csv", "app-lookup.csv", "app.csv"};
        HashMap<String, Demographic> demographicMap = new HashMap<>();
        HashMap<Integer, AppLookup> appLookupMap = new HashMap<>();
        HashMap<String, App> appMap = new HashMap<>();
        HashMap<String, Integer> appIndexMap = new HashMap<>();
        TreeMap<String, Integer> fileSuccessMap = new TreeMap<>();

        //The HashMap within errorMap has been defined as a LinkedHashMap in order to preserve order of insertion of data
        TreeMap<String, TreeMap<Integer, String[]>> errorMap = new TreeMap<>();

        //flush away data for every bootstrap 
        appDAO.deleteData();
        demographicDAO.deleteData();
        appLookupDAO.deleteData();

        /*Iterate through the fileNames in the specified order in order to ensure
         that data is uploaded in the correct order to prevent violating foreign key constraints
         (i.e app.csv, location.csv)*/
        for (int i = 0; i < fileNames.length; i++) {
            boolean fileFound = false;
            //Get inputstream for Multipart type fileToUpload variable
            //Put zip file inputstream into ZipInputStream in order to allow
            //program to read zip file entries
            try (InputStream is = fileToUpload.getInputStream();
                    ZipInputStream zis = new ZipInputStream(is)) {

                //Iterate through each ZipEntry in ZipInputStream
                while ((entry = zis.getNextEntry()) != null) {

                    //get file name of current ZipEntry
                    String curFileName = entry.getName();

                    //fileFound is used to stop searching through zip stream the moment one file has been found
                    if (!fileFound && fileNames[i].equals(curFileName)) {

                        if (curFileName.equals("demographics.csv")) { //If demographics.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate demographic.csv and put all error messages into error map
                            errorMap.put(curFileName, validateDemographic(zis, demographicMap));
                            fileSuccessMap.put(curFileName, demographicMap.size());

                            //insert demographics records into database
                            demographicDAO.batchCommit(demographicMap);

                        } else if (curFileName.equals("app-lookup.csv")) { //If app-lookup.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate app-lookup.csv and put all error messages into error map
                            errorMap.put(curFileName, validateAppLookup(zis, appLookupMap));
                            fileSuccessMap.put(curFileName, appLookupMap.size());

                            //insert appLookUp records into database
                            appLookupDAO.batchCommit(appLookupMap);
                        } else if (curFileName.equals("app.csv")) { //if app.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate app.csv and put all error messages into error map
                            errorMap.put(curFileName, validateApp(zis, appMap, appIndexMap, demographicMap, appLookupMap, null));
                            fileSuccessMap.put(curFileName, appMap.size());

                            //insert app recoards into database
                            appDAO.batchCommit(appMap);

                        }
                    }
                }
            } catch (IOException e) {

            }
        }
        if (!filesUploaded) {
            return null;
        }
        //Convert current errorMap into a JSON object
        return bootstrapObjectConverter(fileSuccessMap, errorMap);
    }

    /**
     * Bootstraps a zip file and uploads its contents into database for files
     * that match the required filename for location-upload, this methods can
     * take in up to 6 csv files which consists of demographics, app-lookup,
     * location-lookup, location, app and location-delete
     *
     * @param fileToUpload
     * @return BootstrapError object
     */
    public BootstrapError locationUpload(MultipartFile fileToUpload) {
        boolean filesUploaded = false;
        ZipEntry entry = null;

        //stores the csv files into an array
        String[] fileNames = {"demographics.csv", "app-lookup.csv", "location-lookup.csv", "location.csv", "app.csv", "location-delete.csv"};

        //valid records are saved into hashmap
        HashMap<String, Demographic> demographicMap = new HashMap<>();
        HashMap<Integer, AppLookup> appLookupMap = new HashMap<>();
        HashMap<String, App> appMap = new HashMap<>();
        //appIndexMap is used to know the duplicated app line record
        HashMap<String, Integer> appIndexMap = new HashMap<>();
        TreeMap<String, Integer> fileSuccessMap = new TreeMap<>();

        HashMap<Integer, LocationLookup> locationLookupMap = new HashMap<>();
        HashMap<String, Location> locationMap = new HashMap<>();
        //locationIndexMap is used to know the duplicated location line record
        HashMap<String, Integer> locationIndexMap = new HashMap<>();
        HashMap<String, Location> deleteLocationMap = new HashMap<String, Location>();

        //The HashMap within errorMap has been defined as a LinkedHashMap in order to preserve order of insertion of data
        TreeMap<String, TreeMap<Integer, String[]>> errorMap = new TreeMap<>();

        //flush the database
        appDAO.deleteData();
        demographicDAO.deleteData();
        appLookupDAO.deleteData();
        locationDAO.deleteData();
        locationLookupDAO.deleteData();

        /*Iterate through the fileNames in the specified order in order to ensure
         that data is uploaded in the correct order to prevent violating foreign key constraints
         (i.e app.csv, location.csv)*/
        for (int i = 0; i < fileNames.length; i++) {
            boolean fileFound = false;
            //Get inputstream for Multipart type fileToUpload variable
            //Put zip file inputstream into ZipInputStream in order to allow
            //program to read zip file entries
            try (InputStream is = fileToUpload.getInputStream();
                    ZipInputStream zis = new ZipInputStream(is)) {

                //Iterate through each ZipEntry in ZipInputStream
                while ((entry = zis.getNextEntry()) != null) {

                    //get file name of current ZipEntry
                    String curFileName = entry.getName();

                    //fileFound is used to stop searching through zip stream the moment one file has been found
                    if (!fileFound && fileNames[i].equals(curFileName)) {

                        if (curFileName.equals("demographics.csv")) { //If demographics.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate demographic.csv and put all error messages into error map
                            errorMap.put(curFileName, validateDemographic(zis, demographicMap));
                            fileSuccessMap.put(curFileName, demographicMap.size());

                            //insert demographics records
                            demographicDAO.batchCommit(demographicMap);

                        } else if (curFileName.equals("app-lookup.csv")) { //If app-lookup.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate app-lookup.csv and put all error messages into error map
                            errorMap.put(curFileName, validateAppLookup(zis, appLookupMap));
                            fileSuccessMap.put(curFileName, appLookupMap.size());

                            //insert applookup records
                            appLookupDAO.batchCommit(appLookupMap);
                        } else if (curFileName.equals("app.csv")) { //if app.csv is found
                            filesUploaded = true;
                            fileFound = true;

                            //validate app.csv and put all error messages into error map
                            errorMap.put(curFileName, validateApp(zis, appMap, appIndexMap, demographicMap, appLookupMap, null));
                            fileSuccessMap.put(curFileName, appMap.size());

                            //insert app records
                            appDAO.batchCommit(appMap);

                        } else if (curFileName.equals("location-lookup.csv")) {
                            fileFound = true;
                            filesUploaded = true;

                            //valid location-lookup.csv and put all error messages into error map
                            errorMap.put(curFileName, validateLocationLookup(zis, locationLookupMap));
                            fileSuccessMap.put(curFileName, locationLookupMap.size());

                            //insert locationLookup records
                            locationLookupDAO.batchCommit(locationLookupMap);

                        } else if (curFileName.equals("location.csv")) {
                            fileFound = true;
                            filesUploaded = true;

                            //validate location.csv and put all error messages into error map
                            errorMap.put(curFileName, validateLocation(zis, locationMap, locationIndexMap, locationLookupMap, null));
                            fileSuccessMap.put(curFileName, locationMap.size());

                            //insert location records
                            locationDAO.batchCommit(locationMap);
                        } else if (curFileName.equals("location-delete.csv")) {
                            fileFound = true;
                            filesUploaded = true;
                            int invalidCount = 0;
                            int validCount = 0;

                            //validate location-delete.csv and put all error messages into error map
                            errorMap.put(curFileName, validateDeleteLocation(zis, deleteLocationMap, locationLookupMap));
                            Iterator<String> deleteLocationMapIter = deleteLocationMap.keySet().iterator();

                            while (deleteLocationMapIter.hasNext()) {
                                String curKey = deleteLocationMapIter.next();
                                if (!locationMap.containsKey(curKey)) {
                                    deleteLocationMapIter.remove();
                                    invalidCount++;
                                } else {
                                    validCount++;
                                }
                            }

                            //delete the valid records
                            locationDAO.deleteLocationData(deleteLocationMap);
                            fileSuccessMap.put("validDelete", validCount);
                            fileSuccessMap.put("invalidDelete", invalidCount);
                        }
                    }
                }

            } catch (IOException e) {

            }
        }
        if (!filesUploaded) {
            return null;
        }
        //Convert current errorMap into a JSON object
        return bootstrapObjectConverter(fileSuccessMap, errorMap);
    }

    /**
     * Add data within a zip file and uploads its contents into database for
     * files that match the required filename. Returns a BootstrapError entity
     *
     * @param fileToUpload file to upload
     * @return BootstrapError object
     */
    public BootstrapError addData(MultipartFile fileToUpload) {
        boolean filesUploaded = false;
        //stores csv files into an array
        String[] fileNames = {"demographics.csv", "app.csv", "location.csv", "location-delete.csv"};
        //stores all valid records into hashmap
        HashMap<String, Demographic> demographicMap = new HashMap<>();
        HashMap<String, App> appMap = new HashMap<>();
        HashMap<String, Integer> appIndexMap = new HashMap<>();
        HashMap<Integer, AppLookup> appLookupMap = new HashMap<>();
        HashMap<Integer, LocationLookup> locationLookupMap = new HashMap<>();
        HashMap<String, Location> locationMap = new HashMap<>();
        HashMap<String, Location> daoLocationMap = new HashMap<>();
        TreeMap<String, Integer> fileSuccessMap = new TreeMap<>();
        TreeMap<String, TreeMap<Integer, String[]>> errorMap = new TreeMap<>();
        HashMap<String, Integer> locationIndexMap = new HashMap<>();
        HashMap<String, Location> deleteLocationMap = new HashMap<>();

        ZipEntry entry = null;

        for (int i = 0; i < fileNames.length; i++) {
            //Get inputstream for Multipart type fileToUpload variable
            //Put zip file inputstream into ZipInputStream in order to allow
            //program to read zip file entries
            try (InputStream is = fileToUpload.getInputStream();
                    ZipInputStream zis = new ZipInputStream(is)) {
                //Iterate through each ZipEntry in ZipInputStream
                while ((entry = zis.getNextEntry()) != null) {
                    boolean fileFound = false;
                    String curFileName = entry.getName();
                    //fileFound is used to stop searching through zip stream the moment one file has been found
                    if (!fileFound && fileNames[i].equals(curFileName)) {

                        if (curFileName.equals("demographics.csv")) {
                            filesUploaded = true;
                            fileFound = true;

                            errorMap.put(curFileName, validateDemographic(zis, demographicMap));
                            fileSuccessMap.put(curFileName, demographicMap.size());
                            try {
                                demographicDAO.batchCommit(demographicMap);
                            } catch (Exception e) {

                            }

                        } else if (curFileName.equals("app.csv")) {

                            filesUploaded = true;
                            fileFound = true;

                            List<AppLookup> appLookupList = appLookupDAO.retrieveAll();
                            for (AppLookup appLookup : appLookupList) {
                                int appID = appLookup.getAppID();
                                appLookupMap.put(appID, appLookup);
                            }

                            List<Demographic> demographicList = demographicDAO.retrieveAll();

                            for (Demographic demographic : demographicList) {
                                String macAddress = demographic.getMacAddress();
                                demographicMap.put(macAddress, demographic);
                            }

                            HashMap<String, App> daoAppMap = new HashMap<>();
                            List<App> daoAppList = appDAO.retrieveAll();
                            Iterator<App> appIter = daoAppList.iterator();
                            while (appIter.hasNext()) {
                                App curApp = appIter.next();
                                String macAddress = curApp.getAppPK().getDemographic().getMacAddress();
                                Date dateTimestamp = curApp.getAppPK().getTimestamp();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String strTimestamp = sdf.format(dateTimestamp);
                                //timestamp and macaddress will form the key of the hashmap as it will be unique
                                daoAppMap.put(strTimestamp + macAddress, curApp);
                            }

                            errorMap.put(curFileName, validateApp(zis, appMap, appIndexMap, demographicMap, appLookupMap, daoAppMap));

                            //insert app records into database
                            appDAO.batchCommit(appMap);
                            fileSuccessMap.put(curFileName, appMap.size());

                        } else if (curFileName.equals("location.csv")) {
                            fileFound = true;
                            filesUploaded = true;

                            List<LocationLookup> locationLookupList = locationLookupDAO.retrieveAll();
                            for (LocationLookup locationLookup : locationLookupList) {
                                int locationID = locationLookup.getLocationID();
                                locationLookupMap.put(locationID, locationLookup);
                            }

                            List<Location> locationList = locationDAO.retrieveAll();
                            for (Location location : locationList) {
                                String macAddress = location.getLocationPK().getMacAddress();
                                Date dateTimestamp = location.getLocationPK().getTimestamp();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String strTimestamp = sdf.format(dateTimestamp);
                                daoLocationMap.put(strTimestamp + macAddress, location);
                            }

                            //validate location.csv and put all error messages into error map
                            errorMap.put(curFileName, validateLocation(zis, locationMap, locationIndexMap, locationLookupMap, daoLocationMap));

                            //insert location records into database
                            locationDAO.batchCommit(locationMap);
                            fileSuccessMap.put(curFileName, locationMap.size());
                            
                        } else if (curFileName.equals("location-delete.csv")) {
                            fileFound = true;
                            filesUploaded = true;

                            int validCount = 0;
                            int invalidCount = 0;

                            List<LocationLookup> locationLookupList = locationLookupDAO.retrieveAll();
                            for (LocationLookup locationLookup : locationLookupList) {
                                int locationID = locationLookup.getLocationID();
                                locationLookupMap.put(locationID, locationLookup);
                            }

                            List<Location> locationList = locationDAO.retrieveAll();

                            for (Location location : locationList) {
                                String macAddress = location.getLocationPK().getMacAddress();
                                Date dateTimestamp = location.getLocationPK().getTimestamp();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String strTimestamp = sdf.format(dateTimestamp);
                                locationMap.put(strTimestamp + macAddress, location);
                            }

                            //validate location-delete.csv and put all error messages into error map
                            errorMap.put(curFileName, validateDeleteLocation(zis, deleteLocationMap, locationLookupMap));

                            Iterator<String> deleteLocationMapIter = deleteLocationMap.keySet().iterator();

                            while (deleteLocationMapIter.hasNext()) {
                                String curKey = deleteLocationMapIter.next();

                                if (!locationMap.containsKey(curKey)) {
                                    deleteLocationMapIter.remove();
                                    invalidCount++;
                                } else {
                                    validCount++;
                                }
                            }

                            //delete the validated location records
                            locationDAO.deleteLocationData(deleteLocationMap);

                            fileSuccessMap.put("validDelete", validCount);
                            fileSuccessMap.put("invalidDelete", invalidCount);
                        }
                    }
                }

            } catch (IOException e) {

            }
        }

        if (!filesUploaded) {
            return null;
        }

        return bootstrapObjectConverter(fileSuccessMap, errorMap);
    }

    /**
     * Validates demographic.csv data passed in and validates it for common
     * validations and file specific validations Returns a TreeMap
     * containing all the errors for the current file. Returns error messages
     * via a LinkedHashMap.
     *
     * @param zis zipinputstream
     * @param demographicMap hashmap
     * @return LinkedHashMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateDemographic(ZipInputStream zis, HashMap<String, Demographic> demographicMap) {

        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        //Put ZipInputStream into an InputReader which is required for CSVReader constructor
        ArrayList<String> requiredFields = new ArrayList<String>();
        int macAddressIndex = 0;
        int nameIndex = 0;
        int passwordIndex = 0;
        int genderIndex = 0;
        int emailIndex = 0;
        int ccaIndex = 0;

        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            int lineCount = 1; //lineCount to keep track of current line number in csv file

            while ((curLine = csvReader.readNext()) != null) {

                ArrayList<String> errorList = new ArrayList<String>();

                //skip header line of csv
                if (lineCount == 1) {

                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());

                        if (curLine[i].equalsIgnoreCase("mac-address")) {
                            macAddressIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("name")) {
                            nameIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("password")) {
                            passwordIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("gender")) {
                            genderIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("email")) {
                            emailIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("cca")) {
                            ccaIndex = i;
                        }
                    }
                    //if there is record in the csv files, csvReader will then read the record
                    if ((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                    }
                }

                if (lineCount == 1) {
                    return errorMap;
                }

                //define fields of csv to run a loop through to validate if fields are null
                //loop through each required field
                for (int i = 0; i < requiredFields.size(); i++) {
                    if (curLine[i].equals("")) {
                        errorList.add("blank " + requiredFields.get(i));
                    }
                }

                //if line did not fail common file validations
                if (errorList.isEmpty()) {

                    //trim all lines to remove white spaces from the front and end
                    String macAddress = curLine[macAddressIndex].trim();
                    String name = curLine[nameIndex].trim();
                    String password = curLine[passwordIndex].trim();
                    String email = curLine[emailIndex].trim();
                    String gender = curLine[genderIndex].trim().toLowerCase();
                    String cca = curLine[ccaIndex];

                    //regex expression to validate that mac address is hexadecimal and 40 characters long
                    if (!macAddress.matches("[a-f0-9A-F]{40}")) {
                        errorList.add("invalid mac address");
                    }

                    //check that password is at least 8 characters long and contains no white spaces
                    if ((password.length() < 8) || password.indexOf(" ") != -1) {
                        errorList.add("invalid password");
                    }

                    //regex expression to validate that email is valid 
                    if (!email.matches("([a-zA-Z0-9.])+(2011|2012|2013|2014|2015)+@+(sis|economics|socsc|accountancy|business|law)+(.smu.edu.sg)")) {
                        errorList.add("invalid email");
                    }

                    //check that gender is only either 'f' or 'm'
                    if (!gender.equals("f") && !gender.equals("m")) {
                        errorList.add("invalid gender");
                    }

                    if (cca.length() > 63) {
                        errorList.add("cca record too long");
                    }

                    //if error list is empty, it means that the current line being read has no errors
                    if (errorList.isEmpty()) {

                        // put current line into demographic  map
                        demographicMap.put(macAddress, new Demographic(macAddress, name, password, email, gender.charAt(0), cca));
                    }
                }

                //current line contains some errors
                if (!errorList.isEmpty()) {

                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }

                lineCount++;
            }

        } catch (IOException e) {

        }
        return errorMap;
    }

    /**
     * Validates applookup.csv data passed in and validates it for common
     * validations and file specific validations Returns a TreeMap
     * containing all the errors for the current file.Returns error messages via
     * a TreeMap.
     *
     * @param zis zipinputstream
     * @param appLookupMap hashmap
     * @return TreeMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateAppLookup(ZipInputStream zis, HashMap<Integer, AppLookup> appLookupMap) {

        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        //Put ZipInputStream into an InputReader which is required for CSVReader constructor
        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            int lineCount = 1;  //lineCount to keep track of current line number in csv file
            ArrayList<String> requiredFields = new ArrayList<String>();
            int appIDIndex = 0;
            int nameIndex = 0;
            int categoryIndex = 0;

            while ((curLine = csvReader.readNext()) != null) {

                ArrayList<String> errorList = new ArrayList<String>();

                if (lineCount == 1) {

                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());

                        if (curLine[i].equalsIgnoreCase("app-id")) {
                            appIDIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("app-name")) {
                            nameIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("app-category")) {
                            categoryIndex = i;
                        }

                    }
                    
                    if((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                    }
                }
                
                if(lineCount == 1) {
                    return errorMap;
                }
                //define fields of csv to run a loop through to validate if fields are null
                for (int i = 0; i < requiredFields.size(); i++) {
                    if (curLine[i].equals("")) {
                        errorList.add("blank " + requiredFields.get(i));
                    }
                }
                //if line did not fail common file validations
                if (errorList.isEmpty()) {

                    //trim all lines to remove white spaces from the front and end
                    int appID = Integer.parseInt(curLine[appIDIndex].trim());
                    String name = curLine[nameIndex].trim();
                    String category = curLine[categoryIndex].trim().toLowerCase();

                    //check that app id is a positive non-zero integer
                    if (appID <= 0) {
                        errorList.add("invalid app id");
                    }

                    //regex expression to validate that category is valid
                    if (!category.matches("(books|social|education|entertainment|information|library|local|tools|fitness|games|others)")) {
                        errorList.add("invalid app category");
                    }

                    //if error list is empty, it means that the current line being read has no errors
                    if (errorList.isEmpty()) {

                        //put current line into app-lookup map
                        appLookupMap.put(appID, new AppLookup(appID, name, category));
                    }
                }

                //current line contains some errors
                if (!errorList.isEmpty()) {

                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }

                lineCount++;
            }

        } catch (IOException e) {

        }
        return errorMap;
    }

    /**
     * Validates app.csv and validates it for common validations and file
     * specific validations by checking it against the current uploaded
     * demographic.csv and app.csv. In the event that validateApp was triggered
     * via add data, the app data will be checked against the app data in
     * database and the data will be validated for duplicates. Returns error
     * messages via a LinkedHashMap.
     *
     * @param zis zipinputstream
     * @param appMap app hashmap
     * @param demographicMap demographic hashmap
     * @param appLookupMap applookup hashmap
     * @param daoAppMap dao apphashmap
     * @return LinkedHashMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateApp(ZipInputStream zis, HashMap<String, App> appMap, HashMap<String, Integer> appIndexMap, HashMap<String, Demographic> demographicMap, HashMap<Integer, AppLookup> appLookupMap, HashMap<String, App> daoAppMap) {

        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        //Put ZipInputStream into an InputReader which is required for CSVReader constructor
        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            ArrayList<String> requiredFields = new ArrayList<String>();
            int lineCount = 1;  //lineCount to keep track of current line number in csv file

            int timestampIndex = 0;
            int macAddressIndex = 0;
            int appIDIndex = 0;

            while ((curLine = csvReader.readNext()) != null) {

                boolean csvDuplicate = false;
                ArrayList<String> errorList = new ArrayList<>();

                //skip header line of csv
                if (lineCount == 1) {
                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());

                        if (curLine[i].equalsIgnoreCase("timestamp")) {
                            timestampIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("mac-address")) {
                            macAddressIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("app-id")) {
                            appIDIndex = i;
                        }
                    }
                    
                   if((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                   }
                }
                
                if(lineCount == 1) {
                    return errorMap;
                }
                String timestamp = curLine[timestampIndex].trim();
                String macAddress = curLine[macAddressIndex].trim();
                int appID = -1;
                if(!curLine[appIDIndex].trim().equals("")) {
                    appID = Integer.parseInt(curLine[appIDIndex].trim());
                }

                //concatenate timestamp from app-lookup with mac address from demographics
                String curKey = timestamp + macAddress;

                if (errorList.isEmpty()) {

                    //loop through each required field
                    for (int i = 0; i < requiredFields.size(); i++) {
                        if (curLine[i].equals("")) {
                            errorList.add("blank " + requiredFields.get(i));
                        }
                    }

                    //if line did not fail common file validations
                    if (errorList.isEmpty()) {

                        //trim all lines to remove white spaces from the front and end
                        //check that app id exists in app-lookup
                        if (!appLookupMap.containsKey(appID)) {
                            errorList.add("invalid app");
                        }

                        //regex expression to validate that mac address is hexadecimal and 40 characters long
                        if (!macAddress.matches("[a-f0-9A-F]{40}")) {
                            errorList.add("invalid mac address");
                        } else //check that mac address exists in demographics
                        if (!demographicMap.containsKey(macAddress)) {
                            errorList.add("no matching mac address");
                        }

                        //regex expression to validate that timestamp is valid
                        if (!timestamp.matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})")) {
                            errorList.add("invalid timestamp");
                        } else {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                sdf.setLenient(false);
                                Date date = sdf.parse(timestamp);
                            } catch (ParseException e) {
                                errorList.add("invalid timestamp");
                            }
                        }

                        if (daoAppMap != null) {
                            if (daoAppMap.containsKey(curKey)) {
                                errorList.add("duplicate row");
                            }
                        }

                        try {
                            //if a duplicate exists and has a valid app id
                            if (appMap.containsKey(curKey) && errorList.isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = sdf.parse(timestamp);
                                Demographic demographic = demographicMap.get(macAddress);
                                AppLookup appLookup = appLookupMap.get(appID);
                                appMap.put(curKey, new App(new AppPK(date, demographic), appLookup));
                                errorList.add("duplicate row");
                                csvDuplicate = true;

                            } else if (errorList.isEmpty()) {   //if current app is error free
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = sdf.parse(timestamp);
                                Demographic demographic = demographicMap.get(macAddress);
                                AppLookup appLookup = appLookupMap.get(appID);
                                appMap.put(curKey, new App(new AppPK(date, demographic), appLookup));
                                appIndexMap.put(curKey, lineCount);
                            }
                        } catch (ParseException e) {

                        }
                    }
                }

                if (!errorList.isEmpty() && csvDuplicate) { //If there is a duplicate in the same csv

                    int previousLineCount = appIndexMap.get(curKey);

                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(previousLineCount, errorArray);
                    appIndexMap.put(curKey, lineCount);

                } else if (!errorList.isEmpty()) {//if not duplicate, it means that the entry contains errors
                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }
                lineCount++;
            }

        } catch (IOException e) {

        }
        return errorMap;
    }

    /**
     * Validates locationlookup.csv data passed in and validates it for common
     * validations and file specific validations Returns a TreeMap containing
     * all the errors for the current file.
     *
     * @param zis
     * @param locationLookupMap
     * @return TreeMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateLocationLookup(ZipInputStream zis, HashMap<Integer, LocationLookup> locationLookupMap) {
        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            ArrayList<String> requiredFields = new ArrayList<String>();
            int lineCount = 1;  //lineCount to keep track of current line number in csv file

            int locationIDIndex = 0;
            int semanticPlaceIndex = 0;
            while ((curLine = csvReader.readNext()) != null) {

                ArrayList<String> errorList = new ArrayList<String>();

                if (lineCount == 1) {
                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());
                        if (curLine[i].equalsIgnoreCase("semantic-place")) {
                            semanticPlaceIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("location-id")) {
                            locationIDIndex = i;
                        }
                    }
                    if ((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                    }
                }

                if (lineCount == 1) {
                    return errorMap;
                }

                //define fields of csv to run a loop through to validate if fields are null
                for (int i = 0; i < requiredFields.size(); i++) {
                    if (curLine[i].equals("")) {
                        errorList.add("blank " + requiredFields.get(i));
                    }
                }
                //if line did not fail common file validations
                if (errorList.isEmpty()) {

                    //trim all lines to remove white spaces from the front and end
                    String strLocationID = curLine[locationIDIndex].trim();
                    String semanticPlace = curLine[semanticPlaceIndex].trim();

                    //check that location id is a positive non-zero integer
                    if (!strLocationID.matches("[0-9]+") || Integer.parseInt(strLocationID) <= 0) {
                        errorList.add("invalid location id");
                    }

                    //regex expression to validate that semantic place is valid
                    if (!semanticPlace.matches("(SMUSIS)+(L|B)+([1-9])+([a-zA-Z-0-9]{1,})")) {
                        errorList.add("invalid semantic place");
                    }

                    //if data is valid
                    if (errorList.isEmpty()) {
                        int locationID = Integer.parseInt(strLocationID);
                        locationLookupMap.put(locationID, new LocationLookup(locationID, semanticPlace));
                    }

                }

                //current line contains some errors
                if (!errorList.isEmpty()) {

                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }

                lineCount++;
            }

        } catch (IOException e) {

        }

        return errorMap;
    }

    /**
     * Validates location.csv and validates it for common validations and file
     * specific validations by checking it against the current uploaded
     * locationLookup.csv. In the event that validateLocation was triggered via
     * add data, the location data will be checked against the location data in
     * database and the data will be validated for duplicates. Returns error
     * messages via a TreeMap.
     *
     * @param zis zipinputsteam
     * @param locationMap location hashmap
     * @param locationIndexMap locationIndex hashmap
     * @param locationLookupMap locationLookup hashmap
     * @param daoLocationMap locationDAO hashmap
     * @return TreeMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateLocation(ZipInputStream zis, HashMap<String, Location> locationMap, HashMap<String, Integer> locationIndexMap, HashMap<Integer, LocationLookup> locationLookupMap, HashMap<String, Location> daoLocationMap) {
        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            ArrayList<String> requiredFields = new ArrayList<String>();
            int lineCount = 1;  //lineCount to keep track of current line number in csv file

            //define variable columns index no.
            int timestampIndex = 0;
            int macAddressIndex = 0;
            int locationIDIndex = 0;
            while ((curLine = csvReader.readNext()) != null) {

                ArrayList<String> errorList = new ArrayList<String>();

                if (lineCount == 1) {
                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());

                        if (curLine[i].equalsIgnoreCase("timestamp")) {
                            timestampIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("mac-address")) {
                            macAddressIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("location-id")) {
                            locationIDIndex = i;
                        }
                    }

                   if ((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                    }
                }

                if (lineCount == 1) {
                    return errorMap;
                }

                //define fields of csv to run a loop through to validate if fields are null
                for (int i = 0; i < requiredFields.size(); i++) {
                    if (curLine[i].equals("")) {
                        errorList.add("blank " + requiredFields.get(i));
                    }
                }
                //trim all lines to remove white spaces from the front and end
                String timestamp = curLine[timestampIndex].trim();
                String macAddress = curLine[macAddressIndex].trim();
                String strLocationID = curLine[locationIDIndex].trim();

                String curKey = timestamp + macAddress;
                boolean csvDuplicate = false;
                //if line did not fail common file validations
                if (errorList.isEmpty()) {

                    //check that location id is a positive non-zero integer
                    if (!strLocationID.matches("[0-9]+") || !locationLookupMap.containsKey(Integer.parseInt(strLocationID))) {
                        errorList.add("invalid location");
                    }

                    //regex expression to validate that mac address is valid
                    if (!macAddress.matches("[a-f0-9A-F]{40}")) {
                        errorList.add("invalid mac address");
                    }

                    //regex expression to validate that timestamp is valid
                    if (!timestamp.matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})")) {
                        errorList.add("invalid timestamp");
                    } else {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sdf.setLenient(false);
                            Date date = sdf.parse(timestamp);
                        } catch (ParseException e) {
                            errorList.add("invalid timestamp");
                        }
                    }

                    //check if location data is already in database
                    if (daoLocationMap != null) {
                        if (daoLocationMap.containsKey(curKey)) {
                            errorList.add("duplicate row");
                        }
                    }

                    try {
                        //check for duplicate rows
                        if (locationMap.containsKey(curKey) && errorList.isEmpty()) {

                            csvDuplicate = true;
                            int locationID = Integer.parseInt(strLocationID);
                            LocationLookup locationLookup = locationLookupMap.get(locationID);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sdf.parse(timestamp);
                            LocationPK locationPK = new LocationPK(macAddress, date);
                            locationMap.put(curKey, new Location(locationPK, locationLookup));
                            errorList.add("duplicate row");

                        } else if (errorList.isEmpty()) {
                            int locationID = Integer.parseInt(strLocationID);
                            LocationLookup locationLookup = locationLookupMap.get(locationID);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sdf.parse(timestamp);
                            LocationPK locationPK = new LocationPK(macAddress, date);
                            locationMap.put(curKey, new Location(locationPK, locationLookup));
                            locationIndexMap.put(curKey, lineCount);
                        }

                    } catch (ParseException e) {

                    }

                }
                //if csv contains a duplicate
                if (csvDuplicate) {
                    int previousLineCount = locationIndexMap.get(curKey);
                    locationIndexMap.put(curKey, lineCount);
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(previousLineCount, errorArray);

                } else if (!errorList.isEmpty()) {

                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }

                lineCount++;
            }

        } catch (IOException e) {

        }

        return errorMap;
    }

    /**
     * Validates location.csv and validates it for common validations and file
     * specific validations by checking it against database records. In the
     * event that the record is invalid, the error messages associated with the
     * record will be added to a TreeMap Returns error messages via a TreeMap.
     *
     * @param zis zipinputstream
     * @param deleteLocationMap deletelocation hashmap
     * @param locationLookupMap locationLookUp hashmap
     * @return TreeMap<Integer, String[]>
     */
    public TreeMap<Integer, String[]> validateDeleteLocation(ZipInputStream zis, HashMap<String, Location> deleteLocationMap, HashMap<Integer, LocationLookup> locationLookupMap) {
        TreeMap<Integer, String[]> errorMap = new TreeMap<>();

        try (InputStreamReader inputReader = new InputStreamReader(zis);
                CSVReader csvReader = new CSVReader(inputReader, ',', '\"')) {

            String[] curLine = null;
            ArrayList<String> requiredFields = new ArrayList<String>();
            int lineCount = 1;  //lineCount to keep track of current line number in csv file

            int timestampIndex = 0;
            int macAddressIndex = 0;
            int locationIDIndex = 0;
            while ((curLine = csvReader.readNext()) != null) {

                ArrayList<String> errorList = new ArrayList<String>();

                if (lineCount == 1) {
                    for (int i = 0; i < curLine.length; i++) {
                        requiredFields.add(curLine[i].trim());

                        if (curLine[i].equalsIgnoreCase("timestamp")) {
                            timestampIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("mac-address")) {
                            macAddressIndex = i;
                        }
                        if (curLine[i].equalsIgnoreCase("location-id")) {
                            locationIDIndex = i;
                        }

                    }
                   if ((curLine = csvReader.readNext()) != null) {
                        lineCount++;
                    }
                }

                if (lineCount == 1) {
                    return errorMap;
                }

                //define fields of csv to run a loop through to validate if fields are null
                for (int i = 0; i < requiredFields.size(); i++) {

                    //location-id can be empty
                    if (i != locationIDIndex && curLine[i].equals("")) {
                        errorList.add("blank " + requiredFields.get(i));
                    }

                }
                //trim all lines to remove white spaces from the front and end
                String timestamp = curLine[timestampIndex].trim();
                String macAddress = curLine[macAddressIndex].trim();
                String strLocationID = curLine[locationIDIndex].trim();

                String curKey = timestamp + macAddress;

                //if line did not fail common file validations
                if (errorList.isEmpty()) {

                    //check that location id is a positive non-zero integer and is found in location lookup
                    if (strLocationID.equals("")) {
                        strLocationID = "0";
                    } else if (!strLocationID.matches("[0-9]+") || !locationLookupMap.containsKey(Integer.parseInt(strLocationID))) {
                        errorList.add("invalid location");
                    }

                    //regex expression to validate that mac address is valid
                    if (!macAddress.matches("[a-f0-9A-F]{40}")) {
                        errorList.add("invalid mac address");
                    }

                    //regex expression to validate that timestamp is valid
                    if (!timestamp.matches("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})")) {
                        errorList.add("invalid timestamp");
                    } else {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sdf.setLenient(false);
                            Date date = sdf.parse(timestamp);
                        } catch (ParseException e) {
                            errorList.add("invalid timestamp");
                        }
                    }

                    try {
                        //check for duplicate rows
                        if (errorList.isEmpty()) {

                            int locationID = Integer.parseInt(strLocationID);
                            LocationLookup locationLookup = locationLookupMap.get(locationID);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sdf.parse(timestamp);
                            LocationPK locationPK = new LocationPK(macAddress, date);
                            deleteLocationMap.put(curKey, new Location(locationPK, locationLookup));
                        }

                    } catch (ParseException e) {

                    }

                }

                if (!errorList.isEmpty()) {
                    //convert errorList into an array and put into errorMap together with its line number
                    String[] errorArray = errorList.toArray(new String[0]);
                    errorMap.put(lineCount, errorArray);
                }

                lineCount++;
            }

        } catch (IOException e) {

        }

        return errorMap;
    }

    /**
     * Converts error messages from all validations and combine it to form a
     * BootstrapError entity
     * @param fileSuccessMap TreeMap<String, Integer> which stores the success csv file and the number of records loaded
     * @param errorMap TreeMap<Integer, String[]>> which stores error line number, the error csv file-names and a String array which contains the error messages
     * @return BootstrapError object
     */
    public BootstrapError bootstrapObjectConverter(TreeMap<String, Integer> fileSuccessMap, TreeMap<String, TreeMap<Integer, String[]>> errorMap) {
        Iterator<String> errorMapIter = errorMap.keySet().iterator();
        ArrayList<ErrorMsg> errorList = new ArrayList<>();

        while (errorMapIter.hasNext()) {
            String curFileName = errorMapIter.next();

            TreeMap<Integer, String[]> fileErrorMap = errorMap.get(curFileName);

            Iterator<Integer> fileErrorMapIter = fileErrorMap.keySet().iterator();

            while (fileErrorMapIter.hasNext()) {
                int curLine = fileErrorMapIter.next();
                String[] curLineErrors = fileErrorMap.get(curLine);

                ErrorMsg errorMsg = new ErrorMsg(curFileName, curLine, curLineErrors);
                errorList.add(errorMsg);
            }
        }

        return new BootstrapError(fileSuccessMap, errorList);
    }

    
    /**
     * Deletes records from the database according to inputs user specified. All records that fulfill the criteria will be
     * deleted. The number of records that were deleted from database will be
     * returned to the user.
     * @param macAddress the macaddresss of the user
     * @param startDate the startdate of the deletion period
     * @param endDate the enddate of the deletion period
     * @param locationID the locationID to be deleted
     * @param semanticPlace the semanticPlace to be deleted
     * @return 
     */
    public int bootstrapDelete(String macAddress, String startDate, String endDate, int locationID, String semanticPlace) {

        int rowDeleted = 0;
        //   ArrayList<String> errorList = new ArrayList<String>();

        Date startDateTime = null;
        Date endDateTime = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);
            startDateTime = sdf.parse(startDate);
            endDateTime = sdf.parse(endDate);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        List<Location> locationList = locationDAO.retrieveAllLocation(startDateTime, endDateTime, macAddress, locationID, semanticPlace);

        locationDAO.deleteLocation(locationList);
        rowDeleted = locationList.size();
        return rowDeleted;
    }
}
