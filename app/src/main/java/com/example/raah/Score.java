package com.example.raah;

public class Score {
    private String gameName;
    private String dateAndTime;
    private int totalAttempts;
    private int correctAttempts;
    public Score(){}
    public Score(String gameName,String dateAndTime, int totalAttempts, int correctAttempts){
        this.correctAttempts=correctAttempts;
        this.dateAndTime=dateAndTime;
        this.gameName=gameName;
        this.totalAttempts=totalAttempts;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }
}
