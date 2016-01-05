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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import smua.models.Entities.AppLookup;
import smua.models.Entities.Demographic;
//import smua.models.custom.CustomDemoDetails;

/**
 *
 * @author r128
 */

@Repository
@Transactional
public class DemographicDAO /*implements UserDetailsService */{

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
     * Returns all the Demographic data in database via a List
     * @return List<Demographic>
     */
    public List<Demographic> retrieveAll() {
        return getSession().createQuery("from Demographic").list();
    }
    
    /**
     * Commits all data within demographicMap into database
     * @param demographicMap 
     */
    public void batchCommit(HashMap<String, Demographic> demographicMap) {

        StatelessSession session = sessionFactory.openStatelessSession();
        Transaction tx = session.beginTransaction();

        Iterator<Demographic> demographicMapIter = demographicMap.values().iterator();
        int counter = 1;

        while (demographicMapIter.hasNext()) {

            Demographic curDemographic = demographicMapIter.next();
            session.insert(curDemographic);
            
        }
        tx.commit();
        session.close();

    }
    
    /**
     * Get a demographic object from database via an email
     * @param email
     * @return Demographic
     */
    //get Demographic Object method
    public Demographic getDemographicByEmail(String email){
        System.out.println("EMAIL + " + email);
        String hql = "from Demographic where email LIKE :email";
        Query q = getSession().createQuery(hql);
        q.setParameter("email", email+"%");
        Demographic demographic = (Demographic)q.uniqueResult();
        
        return demographic;
    }
    
    /**
     * Retrieve a Demographic object by MAC Address
     * @param macAddress
     * @return A Demographic object identified by a MAC Address
     */
    public Demographic getDemographicByMacAddress(String macAddress){
        String hql = "from Demographic where macAddress = :macAddress";
        Query q = getSession().createQuery(hql);
        q.setParameter("macAddress", macAddress);
        Demographic demographic = (Demographic)q.uniqueResult();
        
        return demographic;
    }

    //get Demographic Object method
    /*public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Email is : " + email);
        String hql = "from Demographic where email = :email";
        Demographic demographic = (Demographic) getSession().createQuery(hql).setParameter("email", email);
        if(demographic == null){
            System.out.println("User does not exists");
            //remember to import Exceptions
        }
        return new CustomDemoDetails(demographic);
    }*/
    
    /**
     * Wipe all data in Demographic table from database
     */
    public void deleteData () {
        getSession().createQuery("delete from Demographic").executeUpdate();
    }
}
