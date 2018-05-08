package org.example.lab3;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //database
    SQLiteDatabase myDatabase;

    private static final int REQ_CODE_SPEECH_INPUT = 100;


    //set up speaking
    //set up speaker
    private Speaker speaker;
    private final int CHECK_CODE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkTTS(); //set up the Text-To-Speech

        //set up speak button
        Button speak_it = findViewById(R.id.Speak_Btn);
        //set click listener
        speak_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //have it speak the text in the text box.
                speak();
            }
        });

        //mic button set up
        ImageView mic_button = findViewById(R.id.record_btn);
        //set up click listener
        mic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start voice recognition.
                startVoiceInput();
            }
        });

        //history button sends user to the history page for editing the items.
        Button history = findViewById(R.id.history_button);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //redirect the user
                Intent intent = new Intent(MainActivity.this, Display_Results.class);
                startActivity(intent);
            }
        });

    }

    public void onStart(){
        super.onStart();
        //create the database or set up database values.
        create_db();
    }

    private void startVoiceInput() {
        //set up intent to voice recogniser
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //set location
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //put message into intent
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say something..");

        try {
            //attempt recognition
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this,"Error with Speech Recognition", Toast.LENGTH_LONG);
        }
    }

    protected void speak(){
        //get edit text object
        EditText input = findViewById(R.id.Resulting_Text);
        //get string from object
        String input_text = input.getText().toString();
        //add text to SQL database
        add_text(input_text);
        //speak the text
        speaker.speak(input_text);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //if it's a speech recognition code
            case REQ_CODE_SPEECH_INPUT: {
                //check if result is okay or not empty
                if (resultCode == RESULT_OK && null != data) {
                    //get the data into an array list
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //get the first item since we only need the first item.
                    String Item = result.get(0);
                    //Get the text edit object
                    EditText display = findViewById(R.id.Resulting_Text);
                    //save text to SQL database
                    add_text(Item);
                    //display the item on the screen.
                    display.setText(Item);
                }
                break;
            }
            //check if we have a request for text to speech.
            case CHECK_CODE:{
                //check if the text to speech is working.
                if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                    //if so set up the speaker object and enable it
                    speaker = new Speaker(this);
                    speaker.allow(true);

                }else {
                    //if not set up prompt for installation
                    Intent install = new Intent();
                    install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(install);
                }
                break;
            }


        }
    }

    private void checkTTS(){
        //check if the text to speech is working.
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);

    }

    @SuppressLint("WrongConstant")
    private void create_db(){
        //set up database
        myDatabase = openOrCreateDatabase("Text_list.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        myDatabase.setVersion(1);
        myDatabase.setLockingEnabled(true);
        myDatabase.setLocale(Locale.getDefault());
        //attempt to create the table
        try{
            myDatabase.execSQL("CREATE TABLE Text(id integer primary key, given_text text, time text)");
        }catch (Throwable t){
            //since it's already created it will log an error of already created
            Log.e("Error", t.getMessage());
        }
        //populate the history of items in database
        print_text();
    }

    private void add_text(String txt){
        //set up date reading
        Date now = new Date();
        //format date
        SimpleDateFormat form = new SimpleDateFormat("M/d/y: h:m:s");
        String time = form.format(now.getTime());
        //set up values to be saved to database
        ContentValues values = new ContentValues();
        values.put("given_text", txt);
        values.put("time", time);
        //add values to Text table
        myDatabase.insert("Text", "",values);
        //print the text on history list
        print_text();
    }

    private void print_text(){
        //get the scrollable layout
        LinearLayout linearLayout =  findViewById(R.id.Verticl_Layout);
        //clear layout inorder to keep things clean
        linearLayout.removeAllViews();
        //run query on sql database
        Cursor items = myDatabase.rawQuery("Select * from Text", null);
        //check if we can move through the items.
        if (items.moveToFirst()) {
            //if so go through the items and populate the history section
            do {
                //save values into the object
                final Saved_Value temp = new Saved_Value(items.getString(1), items.getString(2));
                //create new text view
                TextView item = new TextView(this);
                //set up text view properties
                item.setTextSize(20); // text size to 20
                item.setText("Text: " + temp.getGiven_Text()+ "; Date: "+ temp.getTimeStamp()); //set text to the actual given text and date &time
                item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)); //make sure it dynamically grows for larger items
                item.setPadding(0,15,0,15); //set paddings top and bottom at 15
                item.setGravity(Gravity.CENTER); // center text
                //set up click listener
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //if clicked set the edit text to the text given before
                        EditText display = findViewById(R.id.Resulting_Text); // edit text object
                        display.setText(temp.getGiven_Text()); // set text
                    }
                });
                //add text view to scroll view
                linearLayout.addView(item);

            }while (items.moveToNext()); //go to next item
        }

    }



}
