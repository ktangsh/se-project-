/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import smua.models.DAO.DemographicDAO;
import smua.models.Entities.Demographic;

/**
 *
 * @author shtang.2014
 */
@Controller
public class SMUAUtility {
    
    @Autowired
    DemographicDAO demographicDAO;
    
    /**
     * Utility method to check if macAddress exists in database
     * @param macAddress A valid SHA-1 MAC address
     * @return true if user exists, else false.
     */
    public boolean validateMacAddress(String macAddress){
        Demographic demo = demographicDAO.getDemographicByMacAddress(macAddress);
        return demo != null;
    }
}
