package br.com.lpn.radiolpn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();;
    private ImageButton btnPlay;
    private TextView songCurrentDurationLabel;
    private  MediaPlayer mp;
    private PlayPauseButton playPauseButton;
    private Button syncButton;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(br.com.lpn.radiolpn.R.layout.activity_main);

        initParse();
        initComponents();


    }


    public void initComponents(){

        playPauseButton = (PlayPauseButton) findViewById(R.id.main_play_pause_button);
        syncButton = (Button) findViewById(R.id.sync_button);

        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override
            public void onStatusChange(View view, boolean state) {
                if (state) {
                    if (isPaused) {
                        mp.start();
                    } else {
                        mp.prepareAsync();
                    }
                } else {
                    isPaused = true;
                    mp.pause();
                }
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initParse();
            }
        });



        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);



//        Button botao = (Button) findViewById(R.id.btnSync);
 //       botao.setOnClickListener(new View.OnClickListener() {
//           @Override
 //           public void onClick(View arg0) {
//
//                btnPlay.setImageResource(R.drawable.btn_play);
//                prepareData();
//
//            }
//        });


    }

    public void initParse(){
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Properties");
        query.whereEqualTo("key", "radioURL");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject properties, ParseException e) {
                if (e == null) {
                    prepareData(properties.getString("value"));

                } else {
                    showToast("Por favor, verifique sua conexão com a internet e clique no botão Sync para atualizar sua musica");
                }
            }
        });
    }

    public void prepareData(String url){

        try{
            if ( isOnline()  ) {

                mp = new MediaPlayer();

                mp.setDataSource(url);

                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        showToast("Por favor, verifique sua conexão com a internet e clique no botão Sync para atualizar sua musica");
                    }
                });
                mHandler.postDelayed(mUpdateTimeTask, 100);

            }


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        showToast("Por favor, verifique sua conexão com a internet e clique no botão Sync para atualizar sua musica");
        return false;
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long currentDuration = 0;
            currentDuration = mp.getCurrentPosition();
            songCurrentDurationLabel.setText(""+milliSecondsToTimer(currentDuration));

            mHandler.postDelayed(this, 100);
        }
    };
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private void showToast(String message){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }


}


