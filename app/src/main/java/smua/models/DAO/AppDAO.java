/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.DAO;

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
import smua.models.Entities.App;

/**
 *
 * @author r128
 */
@Repository
@Transactional
public class AppDAO {

    @Autowired
    private SessionFactory sessionFactory;
    
    /**
     * Get current Hibernate session
     * @return Session
     */
    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    /**
     * Returns all the App data in database via a List
     * @return List<App>
     */
    public List<App> retrieveAll() {
        return getSession().createQuery("from App").list();
    }
    
    /**
     * Commits all data within appMap into database
     * @param appMap 
     */
    public void batchCommit(HashMap<String, App> appMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<App> appMapIter = appMap.values().iterator();

        
        while (appMapIter.hasNext()) {
            App curApp = appMapIter.next();
            session.insert(curApp);
            
        }
        tx.commit();
        
        session.close();

    }
    
    /**
     * Wipe all data in App table from database
     */
    public void deleteData () {
        getSession().createQuery("delete from App").executeUpdate();
        
    }
    
    public List<App> retrieveTopKAppBySchool(Date startDate, Date endDate, String school) {
        //Return: Timestamp, Mac address, App Id, App Name -> List of App array 
        //need to retrieve the macAddress from the current user? -> session
        String hql
                = //                "select app.appPK.timestamp, demo.macAddress, appLookUp.appId, appLookUp.appName" + 
                "from App as app"
                + " where app.appPK.timestamp between :startDateTime and :endDateTime and app.appPK.demographic.email LIKE :school"
                + " order by app.appPK.demographic.macAddress, app.appPK.timestamp";

        Query q = getSession().createQuery(hql);

        q.setParameter("startDateTime", startDate);
        q.setParameter("endDateTime", endDate);
        q.setParameter("school", "%@" + school + "%");

        return q.list();
    }
    

    /**
     * This method returns a list of app usage from between a specified startdate and enddate
     * @param startDate
     * @param endDate
     * @return List<App> List of App that are between the specified startDate and endDate
     */
    public List<App> retrieveAppByStartEndDate(Date startDate, Date endDate) {
        String hql = "from App where timestamp between :startDate and :endDate order by macAddress,timestamp";
        Query q = getSession().createQuery(hql);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);

        return q.list();
    }
    
    /**
     * This method returns a list of app usage from between a specified startdate and enddate
     * @param startDate
     * @param endDate
     * @return List<App> List of App that are between the specified startDate and endDate
     */
    public List<App> retrieveAppByMacDate(Date startDate, Date endDate){
        String hql = "from App as app"
                + " where app.appPK.timestamp between :startDateTime and :endDateTime "
                + " order by app.appPK.demographic.macAddress, app.appPK.timestamp";
        
        Query q = getSession().createQuery(hql);
        
        q.setParameter("startDateTime", startDate);
        q.setParameter("endDateTime", endDate);
        
        return q.list();
    }
    
    
    public List<App> retrieveSmartphoneOveruse(Date startDate, Date endDate, String email){
        String hql = "from App as app"
                + " where app.appPK.timestamp between :startDateTime and :endDateTime and app.appPK.demographic.email LIKE :email"
                + " order by app.appPK.timestamp";
        
        Query q = getSession().createQuery(hql);
        
        q.setParameter("startDateTime", startDate);
        q.setParameter("endDateTime", endDate);
        q.setParameter("email", email+"%");
        
        return q.list();
    }
}


