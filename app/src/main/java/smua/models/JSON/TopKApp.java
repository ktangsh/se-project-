/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smua.models.JSON;

/**
 *
 * @author jennifer
 */
public class TopKApp implements Comparable<TopKApp>{

    private int rank;

    private String name;

    private int duration;

    /**
     * TopKApp constructor that takes in rank, name and duration
     *
     * @param rank
     * @param name
     * @param duration
     */
    public TopKApp(int rank, String name, int duration) {
        this.rank = rank;
        this.name = name;
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
     * Returns name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
    public int compareTo(TopKApp topKApp) {
        if(rank > topKApp.getRank()) {
            return 1;
        }else if(rank < topKApp.getRank()) {
            return -1;
        }else {
            return name.compareTo(topKApp.getName());
        }
    }

}
