/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.DAO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import smua.models.Entities.AppLookup;

/**
 *
 * @author r128
 */
@Repository
@Transactional
public class AppLookupDAO {

    @Autowired
    private SessionFactory sessionFactory;
    
    /**
     * Get current Hibernate session
     * @return Session
     */
    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void create(AppLookup appLookup) {
        getSession().save(appLookup);
    }
    
    /**
     * Returns all the AppLookup data in database via a List 
     * @return List<AppLookup>
     */
    public List<AppLookup> retrieveAll() {
        return getSession().createQuery("from AppLookup").list();
    }
    
    /**
     * Commits all data within appLookupMap into database
     * @param appLookupMap
     */
    public void batchCommit(HashMap<Integer, AppLookup> appLookupMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<AppLookup> appLookupMapIter = appLookupMap.values().iterator();
        int counter = 0;

        while (appLookupMapIter.hasNext()) {
            
            AppLookup curAppLookup = appLookupMapIter.next();
            session.insert(curAppLookup);
            
        }
        tx.commit();
        session.close();

    }
    
    /**
     * Wipe all data in AppLookup table from database
     */
    public void deleteData () {
        getSession().createQuery("delete from AppLookup").executeUpdate();
    }
}
