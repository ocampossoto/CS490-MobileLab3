package org.example.lab3;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class Display_Results extends AppCompatActivity {
    String message;

    SQLiteDatabase myDatabase;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display__results);

        //set up database
        myDatabase = openOrCreateDatabase("Text_list.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        myDatabase.setVersion(1);
        myDatabase.setLockingEnabled(true);
        myDatabase.setLocale(Locale.getDefault());

        //populate history list
        print_text();
    }

    private void print_text(){
        //get layout
        LinearLayout linearLayout =  findViewById(R.id.Verticl_Layout);
        //clear layout
        linearLayout.removeAllViews();
        //run query to get all items
        final Cursor items = myDatabase.rawQuery("Select * from Text", null);
        //check if we can go to first item
        if (items.moveToFirst()) {
            do {
                //get the ID and save it.
                final int ID = items.getInt(0);
                //save data to object
                Saved_Value temp = new Saved_Value(items.getString(1), items.getString(2));
                //create a text view object
                TextView item = new TextView(this);
                //set features for text view
                item.setTextSize(20); //text size
                item.setText("Text: " + temp.getGiven_Text()+ "; Date: "+ temp.getTimeStamp()); // display item information
                item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)); // make sure it's taking the right space on page
                item.setPadding(0,20,0,20); // set up padding
                item.setGravity(Gravity.CENTER);//make sure things are centered
                //set up click listener
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if clicked delete it from database
                        myDatabase.delete("Text"  , "id=?",new String[] {Integer.toString(ID)});
                        //refresh history list
                        print_text();
                    }
                });
                //display items
                linearLayout.addView(item);

            }while (items.moveToNext());
        }
        //if list is empty finish this activity.
        else{
            finish();
        }
    }


}
