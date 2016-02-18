package com.webifystudios.jarvis;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

public class JarvisActivity extends Activity implements ConnectionStateCallback, PlayerNotificationCallback {

    private TextView txtSpeechInput;
    private Button btnSpeak;
    private TextToSpeech t1;
    private SpotifyBrain spotifyBrain;
    private Player mPlayer;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String CLIENT_ID = "ccb7329ab14445319d8c8830edeab0c3";
    private static final String REDIRECT_URI = "jarvis://callback";

    private static final int REQ_CODE_PLAYER = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (Button) findViewById(R.id.btnSpeak);


        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });


        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQ_CODE_PLAYER, request);

    }



    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                    //Jarvis takes the input and decides what to do with it
                    decide(result.get(0));



                    //Repeat the user
                    //speak("My name is Jarvis and" + result.get(0));


                }
                break;
            }
            case REQ_CODE_PLAYER: {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
                if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                        @Override
                        public void onInitialized(Player player) {

                            mPlayer.addConnectionStateCallback(JarvisActivity.this);
                            mPlayer.addPlayerNotificationCallback(JarvisActivity.this);

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("SpotifyActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                }
            }

        }
    }


    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    //JARVIS DECISION/TEXT PROCESSING ENGINE
    public void decide(String text){

        String[] words = text.split(" ");
        if (words.length > 0){

            if (words[0].equals("play")){
                //generate the key word
                String keyword = "";
                for(int i=1;i<words.length;i++){
                    keyword += words[i] + " ";
                }
                spotifyBrain = new SpotifyBrain(this);
                spotifyBrain.getArtistTrack(keyword);
            }

            else speak(words[0]);

        }
    }


    //-------------------------------------------------------------------------------

    //Method makes Jarvis Speak to the user
    public void speak(String toSpeak){

        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }


    //quickly play the music
    public void playMusic(String uri){
        mPlayer.play(uri);
    }

    //quickly pause the music
    public void pauseMusic(){
        mPlayer.pause();
    }

    //--------------------------------------------------------------------------------


    @Override
    public void onLoggedIn() {
        Log.d("SpotifyActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("SpotifyActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SpotifyActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("SpotifyActivity", "Playback event received: " + eventType.name());
        switch (eventType) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("SpotifyActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}