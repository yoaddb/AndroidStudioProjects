package com.FinalProject;

public class mShift {
    private String startTime, endTime, key, date;

    public mShift(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof mShift))  {
            return false;
        }
        mShift other = (mShift) obj;
        return key.equals(other.key);
    }
    @Override
    public int hashCode() {
        return key.hashCode() + date.hashCode()
                + startTime.hashCode() + endTime.hashCode();
    }
}
