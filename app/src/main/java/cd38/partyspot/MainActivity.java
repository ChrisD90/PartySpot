package cd38.partyspot;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import static android.view.View.X;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "0a831a688957426aaabcc2b43abfd01c";
    private static final String REDIRECT_URI = "http://localhost:8080/callback/";
    private static final int REQUEST_CODE = 1337;
    private SpotifyAppRemote mSpotifyAppRemote;
    private PlayerApi player;
    private String accessToken;

    final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote

                        player = spotifyAppRemote.getPlayerApi();

                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private void connected() {
        // Subscribe to PlayerState

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        player.subscribeToPlayerState()
              .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

        try {
            JSONObject playlistsJSON = getPlaylists();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.play("spotify:playlist:0C2p3WQ1O2kBJgp4QudhH4");
                                             //jmTyMht9Q6GYK3x-lFklkw

    }

    private JSONObject getPlaylists( ) throws IOException {

        //"https://api.spotify.com/v1/me/playlists" -H "Authorization: Bearer {your access token}"
        HttpURLConnection connection = null;
        URL object = new URL("https://api.spotify.com/v1/me/playlists");
        connection = (HttpURLConnection) object.openConnection();
        connection.setRequestProperty("Authorization", "Bearer" + accessToken);
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    accessToken = response.getAccessToken();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    public void pauseMusic(View view) {

        CallResult<PlayerState> playerState = player.getPlayerState();
        player.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}
