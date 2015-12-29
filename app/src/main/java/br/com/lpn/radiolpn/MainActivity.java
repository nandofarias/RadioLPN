package br.com.lpn.radiolpn;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class MainActivity extends Activity {

    private Handler mHandler = new Handler();
    private TextView songCurrentDurationLabel;
    private MediaPlayer mp;
    private MainButton playPauseButton;
    private boolean isPaused = false;

    private final static String MSG_ALERTA = "Por favor, verifique sua conexão com a internet e clique no botão Sincronizar para atualizar sua musica";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initParse();
        initComponents();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_about:
                showAlert("Sobre", "Versão 2.0\n" +
                        "Aplicativo desenvolvido pelo Ministerio de TI Luz Para As Nações\n" +
                        "RadioLPN se encontra disponivel também pelo site www.lpn12.com.br\n" +
                        "Visite também o blog do nosso apostolo www.apostologerson.com", "Fechar");
                return true;
            case R.id.exit:
                finish();
                System.exit(0);
        }


        return super.onOptionsItemSelected(item);
    }


    public void initComponents(){

        playPauseButton = (MainButton) findViewById(R.id.main_play_pause_button);
        Button syncButton = (Button) findViewById(R.id.sync_button);
        playPauseButton.setColor(Color.rgb(0, 184, 245));
        playPauseButton.setActive(false);


        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override
            public void onStatusChange(View view, boolean state) {
                if (state) {
                    if (isPaused) {
                        mp.start();
                    } else {
                        playPauseButton.setActive(false);
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

                if (playPauseButton.isPlayed()) {
                    // Obtain MotionEvent object
                    long downTime = SystemClock.uptimeMillis();
                    long eventTime = SystemClock.uptimeMillis() + 100;
                    float x = 0.0f;
                    float y = 0.0f;
                    // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                    int metaState = 0;
                    MotionEvent motionEvent = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            MotionEvent.ACTION_DOWN,
                            x,
                            y,
                            metaState
                    );

                    // Dispatch touch event to view
                    playPauseButton.dispatchTouchEvent(motionEvent);
                }
                playPauseButton.setActive(false);
                isPaused = false;
                initParse();
            }
        });



        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);

    }

    public void initParse(){
        if (isOnline()) {
            ParseAnalytics.trackAppOpenedInBackground(getIntent());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Properties");
            query.whereEqualTo("key", "radioURL");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject properties, ParseException e) {
                    if (e == null) {
                        prepareData(properties.getString("value"));

                    } else {
                        showToast(MSG_ALERTA);
                    }
                }
            });
        }
    }

    public void prepareData(String url){

        try{
            mp = new MediaPlayer();

            mp.setDataSource(url);

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    playPauseButton.setActive(true);
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    showToast(MSG_ALERTA);
                }
            });

            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    showToast(MSG_ALERTA);
                    return true;
                }
            });

            mHandler.postDelayed(mUpdateTimeTask, 100);

            playPauseButton.setActive(true);

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
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
        showToast(MSG_ALERTA);
        return false;
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long currentDuration;
            currentDuration = mp.getCurrentPosition();
            songCurrentDurationLabel.setText(""+milliSecondsToTimer(currentDuration));

            mHandler.postDelayed(this, 100);
        }
    };
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString;

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

    private void showAlert(String title, String text, String buttonText){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setNeutralButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void createNotification(){

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addCategory(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n  = new Notification.Builder(this)
                .setContentTitle("RadioLPN")
                .setContentText("Aplicativo rodando em background, clique aqui para abrir.")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.smallicon)
                .setColor(Color.rgb(0, 184, 245))
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
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
        createNotification();
        super.onPause();
    }


}


