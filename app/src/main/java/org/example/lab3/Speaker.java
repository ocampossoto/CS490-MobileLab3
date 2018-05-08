package org.example.lab3;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;


public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    private boolean ready = false;

    private boolean allowed = false;

    public Speaker(Context context){
        //set up text to speech
        tts = new TextToSpeech(context, this);
    }

    public boolean isAllowed(){
        //return value
        return allowed;
    }

    public void allow(boolean allowed){
        //set allowed and have
        this.allowed = allowed;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
           //set local
            tts.setLanguage(Locale.US);
            //set variable
            ready = true;
        }else{
            ready = false;
        }
    }


    public void speak(String text){
        // Speak only if the TTS is ready
        // and the user has allowed speech
        if(ready && allowed) {
            //create map
            HashMap<String, String> hash = new HashMap<String,String>();
            //set up the items to speak
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            //give to the text to speech to execute
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    // Free up resources
    public void destroy(){
        tts.shutdown();
    }
}
