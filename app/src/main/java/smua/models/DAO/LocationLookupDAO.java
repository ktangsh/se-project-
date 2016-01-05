/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.DAO;

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
import smua.models.Entities.LocationLookup;

/**
 *
 * @author r128
 */
@Repository
@Transactional
public class LocationLookupDAO {

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
     * Commits all data within locationLookupMap into database
     *
     * @param locationLookupMap
     */
    public void batchCommit(HashMap<Integer, LocationLookup> locationLookupMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<LocationLookup> locationLookupMapIter = locationLookupMap.values().iterator();

        while (locationLookupMapIter.hasNext()) {

            LocationLookup curLocationLookup = locationLookupMapIter.next();
            session.insert(curLocationLookup); //Save data into session
        }
        tx.commit();
        session.close();

    }

    /**
     * Wipe all data in LocationLookup table from database
     */
    public void deleteData() {
        getSession().createQuery("delete from LocationLookup").executeUpdate();
    }

    /**
     * This methods return a list of LocationLookup
     *
     * @return List<LocationLookup>
     */
    public List<LocationLookup> retrieveAll() {
        return getSession().createQuery("from LocationLookup").list();
    }

    /**
     * This method return LocationLookup object with the specified location ID
     *
     * @param locationID
     * @return LocationLookup
     */
    public LocationLookup retrieveByID(int locationID) {
        return (LocationLookup) getSession().createQuery("from LocationLookup where locationID=:locationID").setParameter("locationID", locationID).uniqueResult();
    }
    
    /**
     * This method return LocationLookup object with the specified location ID
     *
     * @param locationID
     * @return LocationLookup
     */
    public List<LocationLookup> retrieveByFloor(String floor) {
        return getSession().createQuery("from LocationLookup where semanticPlace like :semanticPlace order by semanticPlace").setParameter("semanticPlace", "SMUSIS"+floor+"%").list();
    }

    /**
     * This methods retrieves a list of LocationLookup based on the specified
     * semantic place
     *
     * @param semanticPlace
     * @return List<LocationLookup>
     */
    public List<LocationLookup> retrieveBySemanticPlace(String semanticPlace) {
        return getSession().createQuery("from LocationLookup where semanticPlace=:semanticPlace").setParameter("semanticPlace", semanticPlace).list();
    }
}
