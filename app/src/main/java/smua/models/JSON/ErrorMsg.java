/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

/**
 *
 * @author r128
 */
public class ErrorMsg {
    
    private String file;
    
    private int line;
    
    private String[] message;

   /**
     * ErrorMsg Constructor that takes in file, line and Array of messages
     * @param file
     * @param line
     * @param message 
     */
    public ErrorMsg(String file, int line, String[] message) {
        this.file = file;
        this.line = line;
        this.message = message;
    }

    /**
     * Returns file
     * @return String
     */
    public String getFile() {
        return file;
    }

    /**
     * Set file
     * @param file 
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Return line
     * @return int
     */
    public int getLine() {
        return line;
    }

    /**
     * Set line
     * @param line 
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Return array of messages
     * @return String[]
     */
    public String[] getMessage() {
        return message;
    }

    /**
     * Set the messages
     * @param message 
     */
    public void setMessage(String[] message) {
        this.message = message;
    }
    
    
}
