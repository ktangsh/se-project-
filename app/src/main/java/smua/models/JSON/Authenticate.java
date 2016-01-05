/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

/**
 *
 * @author twjAm
 */
public class Authenticate {
    
    private String status;
    
    private String token; 

    /**
     * Default constructor
     * @param status
     * @param token 
     */
    public Authenticate(String status, String token) {
        this(status);
        this.token = token;
    }
    
    /**
     * Authenticate constructor that takes in status
     * @param status 
     */
    public Authenticate(String status){
        this.status = status;
    }

    /**
     * Return status
     * @return String
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status
     * @param status 
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Return the token
     * @return String
     */
    public String getToken() {
        return token;
    }

    /**
     * Set the token
     * @param token 
     */
    public void setToken(String token) {
        this.token = token;
    }
    
    
}
