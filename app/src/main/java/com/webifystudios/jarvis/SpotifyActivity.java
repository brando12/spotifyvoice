package com.webifystudios.jarvis;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;

        import com.spotify.sdk.android.player.Spotify;
        import com.spotify.sdk.android.authentication.AuthenticationClient;
        import com.spotify.sdk.android.authentication.AuthenticationRequest;
        import com.spotify.sdk.android.authentication.AuthenticationResponse;
        import com.spotify.sdk.android.player.Config;
        import com.spotify.sdk.android.player.ConnectionStateCallback;
        import com.spotify.sdk.android.player.Player;
        import com.spotify.sdk.android.player.PlayerNotificationCallback;
        import com.spotify.sdk.android.player.PlayerState;

        import java.util.HashMap;
        import java.util.Map;

        import kaaes.spotify.webapi.android.SpotifyApi;
        import kaaes.spotify.webapi.android.SpotifyCallback;
        import kaaes.spotify.webapi.android.SpotifyError;
        import kaaes.spotify.webapi.android.SpotifyService;
        import kaaes.spotify.webapi.android.models.Album;
        import kaaes.spotify.webapi.android.models.Artist;
        import kaaes.spotify.webapi.android.models.ArtistsPager;
        import kaaes.spotify.webapi.android.models.Track;
        import retrofit.Callback;
        import retrofit.RetrofitError;
        import retrofit.client.Response;

public class SpotifyActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "ccb7329ab14445319d8c8830edeab0c3";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "jarvis://callback";

    // Request code that will be passed together with authentication result to the onAuthenticationResult callback
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    private SpotifyBrain spotifyBrain;

    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {

                        mPlayer.addConnectionStateCallback(SpotifyActivity.this);
                        mPlayer.addPlayerNotificationCallback(SpotifyActivity.this);
                        mPlayer.play("spotify:track:5yEPxDjbbzUzyauGtnmVEC");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("SpotifyActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }




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