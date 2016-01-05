/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.DemographicDAO;
import smua.models.Entities.Demographic;

/**
 *
 * @author TAN WEI JIE AMOS
 */
@Controller
public class LoginController {


    
    @Autowired
    DemographicDAO demoDAO;
    
    /**
     * Logs a user in if email and password provided are correct. Returns a Demographic entity
     * @param email
     * @param password
     * @return Demographic
     */
    public Demographic executeLogin(String email, String password) {
       
        if (!email.matches("[0-9a-zA-Z.]+")) {
            return null;
        }
        
        //check if admin
        if (email.equals("admin") && password.equals("remyamoshingheijennifercq")) {
            System.out.println("code here is executed");
            return new Demographic(email, password);
        }
        //retrieve demo object
        Demographic demo = demoDAO.getDemographicByEmail(email);
        if (demo != null && password != null && password.equals(demo.getPassword())) {
            //forward to home page
            return demo;
        }
        return null;
    }
    
    



}
