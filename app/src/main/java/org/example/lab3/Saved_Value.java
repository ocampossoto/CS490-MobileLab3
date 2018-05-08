package org.example.lab3;


public class Saved_Value {
    private String Given_Text;
    private String TimeStamp;

    public Saved_Value(){

    }
    protected  Saved_Value(String txt, String time){
        //save given variables
        Given_Text = txt;
        TimeStamp = time;
    }
    //getters
    public String getGiven_Text() {
        return Given_Text;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }
}

