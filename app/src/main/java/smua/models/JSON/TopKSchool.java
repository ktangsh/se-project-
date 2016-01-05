/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

/**
 *
 * @author shtang.2014
 */
public class TopKSchool implements Comparable<TopKSchool>{

    private int rank;

    private String school;

    private int duration;

    /**
     * TopKSchool constructor that takes in rank, school and duration
     *
     * @param rank
     * @param school
     * @param duration
     */

    public TopKSchool(int rank, String school, int duration) {
        this.rank = rank;
        this.school = school;
        this.duration = duration;
    }

    /**
     * Returns rank
     *
     * @return int
     */
    public int getRank() {
        return rank;
    }

    /**
     * Sets rank
     *
     * @param rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Returns school
     *
     * @return String
     */
    public String getSchool() {
        return school;
    }

    /**
     * Sets school
     *
     * @param school
     */
    public void setSchool(String school) {
        this.school = school;
    }

    /**
     * Returns duration
     *
     * @return int
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets duration
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(TopKSchool topKSchool) {
        if(rank > topKSchool.getRank()) {
            return 1;
        }else if(rank < topKSchool.getRank()) {
            return -1;
        }else {
            return school.compareTo(topKSchool.getSchool());
        }
    }
}
