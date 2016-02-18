package com.webifystudios.jarvis;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.client.Response;

/**
 * Created by brandon on 2/11/16.
 */
public class SpotifyBrain {


        SpotifyApi api = new SpotifyApi();

        // Most (but not all) of the SpotifyBrain Web API endpoints require authorisation.
        // If you know you'll only use the ones that don't require authorisation you can skip this step
        //api.setAccessToken(token);

        SpotifyService spotify = api.getService();

        public String song;

        public JarvisActivity jarvis;


    public SpotifyBrain(JarvisActivity jarvis) {
        this.jarvis = jarvis;
    }

    //Search for an artist and return their ID
    public void getArtistTrack(String artist){

        //search for the artist
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, 0);
        options.put(SpotifyService.LIMIT, 1);


        spotify.searchArtists(artist, options, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {

                if (artistsPager.artists.items.size() != 0) {
                    song = artistsPager.artists.items.get(0).id;
                    Log.d("Artists found:", song);
                    //jarvis.speak(song);
                    getTrack(song);
                }
                else{
                    jarvis.speak("I can't seem to find that artist sir");
                }
            }

            @Override
            public void failure(SpotifyError error) {
                Log.d("error", error.toString());
            }
        });

    }

    public void getTrack(String id){

        spotify.getArtistTopTrack(id, "US", new SpotifyCallback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {

                if (tracks.tracks.size() != 0) {
                    song = tracks.tracks.get(0).name;
                    String artist = tracks.tracks.get(0).artists.get(0).name;
                    Log.d("Artists found:", song);
                    jarvis.speak("I am playing the song " + song + " by " + artist);
                    jarvis.playMusic(tracks.tracks.get(0).uri);
                }else{
                    jarvis.speak("I can't seem to find that song sir");
                }
            }

            @Override
            public void failure(SpotifyError error) {
                Log.d("error", error.toString());
            }
        });


    }


}
