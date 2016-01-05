/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import smua.models.Entities.Location;
import smua.models.Entities.LocationLookup;

/**
 *
 * @author r128
 */
@Repository
@Transactional
public class LocationDAO {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Get current Hibernate session
     *
     * @return Session
     */
    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Commits all data within appLookupMap into database
     * @param locationMap <HashMap<String,Location>>
     */
    public void batchCommit(HashMap<String, Location> locationMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<Location> locationMapIter = locationMap.values().iterator();
        int counter = 1;

        while (locationMapIter.hasNext()) {

            Location curLocation = locationMapIter.next();

            session.insert(curLocation);

        }
        tx.commit();
        session.close();

    }

    /**
     * Wipe all data in Location table from database
     */
    public void deleteData() {
        getSession().createQuery("delete from Location").executeUpdate();
    }

    /**
     * Retrieve all location records from the database
     * @return List<Location>
     */
    public List<Location> retrieveAll() {
        return getSession().createQuery("from Location").list();

    }

    /**
     * Retrieve all location with the specified criteria
     * @param startDate start date of the deletion period
     * @param endDate end date of the deletion period
     * @param macAddress user mac address to be deleted
     * @param locationID locationID to be deleted
     * @param semanticPlace semanticPlace to be deleted
     * @return List<Location>
     */
    public List<Location> retrieveAllLocation(Date startDate, Date endDate, String macAddress, int locationID, String semanticPlace) {
        String macQuery = "and location.locationPK.macAddress=:macAddress ";
        String locationIDQuery = "and location.locationLookup.locationID=:locationID ";
        String semanticQuery = "and location.locationLookup.semanticPlace=:semanticPlace";
        if(macAddress.equals("")){
            macQuery = "";
        }
        if(locationID==0){
            locationIDQuery = "";
        }
        if(semanticPlace.equals("")){
            semanticQuery = "";
        }
        String hql = "from Location as location where location.locationPK.timestamp between :startDate and :endDate "
                + macQuery
                + locationIDQuery
                + semanticQuery;
   
        System.out.println(hql);
        System.out.println(startDate);
        System.out.println(endDate);
        System.out.println(macAddress);
        Query q = getSession().createQuery(hql);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        if(!macAddress.equals("")){
        q.setParameter("macAddress", macAddress);
        }
        if(locationID!=0){
            q.setParameter("locationID", locationID);
        }
        if(!semanticPlace.equals("")){
            q.setParameter("semanticPlace", semanticPlace);
        }

       
        
        return q.list();
    }

    /**
     * Deletion location records
     * @param locationList List<Location>
     */
    public void deleteLocation(List<Location> locationList) {

        for (int i = 0; i < locationList.size(); i++) {
            getSession().delete(locationList.get(i));
        }
    }

    /**
     * This method returns a list of location from between a specified start
     * date, end date and floor
     *
     * @param startDate Start Date
     * @param endDate End Date
     * @param floor Specified floor in SIS, L2, L3 etc
     * @return List of Location objects
     */
    public List<Location> retrieveLocationByStartEndDatePlace(Date startDate, Date endDate, String floor) {
        String hql = "from Location as location"
                + " where location.locationPK.timestamp between :startDate and :endDate and location.locationLookup.semanticPlace like :floor"
                + " order by location.locationPK.timestamp";

        Query q = getSession().createQuery(hql);

        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("floor", "%" + floor + "%");

        return q.list();
    }

    /**
     * Retrieves location by the start date and end date the user inputs
     * @param startDate startdate of the deletion period
     * @param endDate enddate of the deletion period
     * @return List<Location>
     */
    public List<Location> retrieveLocationByStartEndDate(Date startDate, Date endDate) {
        String hql = "from Location as location"
                + " where location.locationPK.timestamp between :startDate and :endDate"
                + " order by location.locationPK.macAddress, location.locationPK.timestamp";

        Query q = getSession().createQuery(hql);

        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);

        return q.list();
    }

    /**
     * Delete valid location records 
     * @param deleteLocationMap HashMap<String, Location>
     */
    public void deleteLocationData(HashMap<String, Location> deleteLocationMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<Location> deleteLocationMapIter = deleteLocationMap.values().iterator();

        while (deleteLocationMapIter.hasNext()) {

            Location curLocation = deleteLocationMapIter.next();

            session.delete(curLocation);

        }
        tx.commit();
        session.close();

    }

}
