package com.example.lumatunes;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class playerActivity extends AppCompatActivity {
    Uri audioUri;
    private VideoView videoView;
    private MediaPlayer player;
    private ArrayList<String> list;
    private int position;
    boolean ISPLAYING=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        player = new MediaPlayer();
        list = getIntent().getStringArrayListExtra("list");
        position = getIntent().getIntExtra("position", 0);
        player.reset();
        String path=list.get(position);
        audioUri=Uri.parse(path);
        try {
            player.setDataSource(this, audioUri); // your Uri
            player.prepare(); // prepares the player (synchronous)
        } catch (IOException ignored) {

        }
        ;

        ImageView play = findViewById(R.id.play);
        ImageView next = findViewById(R.id.next);
        ImageView prev = findViewById(R.id.prev);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(!ISPLAYING);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlay(1);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlay(-1);
            }
        });
        play(true);
        updateIcon();
        updaetSeekbar();

    }
    void updaetSeekbar(){
        SeekBar bar=findViewById(R.id.seekBar);

        Handler handler = new Handler();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                bar.setMax(mp.getDuration());
                bar.setProgress(0);
                Runnable updateSeekbar = new Runnable() {
                    @Override
                    public void run() {
                        if(mp.isPlaying()){
                            bar.setProgress(mp.getCurrentPosition());
                            handler.postDelayed(this,1000);
                        }
                    }
                };
                handler.post(updateSeekbar);
            }
        });
    }
    void updateIcon (){
        if(player.isPlaying()){
            ImageView play =findViewById(R.id.play);
            play.setImageResource(android.R.drawable.ic_media_pause);
        }
        else {
            ImageView play =findViewById(R.id.play);
            play.setImageResource(android.R.drawable.ic_media_play);
        }
    }
    void nextPlay(int i){
        position=position+i;
        if(position<0){
            position=list.size()-1;
        }
        if(position>=list.size()){
            position=0;
        }
        player.reset();
        String path=list.get(position);
        audioUri=Uri.parse(path);
        try {
            player.setDataSource(this, audioUri); // your Uri
            player.prepare(); // prepares the player (synchronous)
        } catch (IOException ignored) {

        }

        play(true);
        updateIcon();
    }
    void play(boolean play) {

        if(play){
            player.start();
            ISPLAYING=true;
        }
        else{
            player.pause();
            ISPLAYING=false;
        }
        updateIcon();
        updateTitle();
    }
    void updateTitle(){
        String fileUri=list.get(position);
        String filePath=Uri.parse(fileUri).getPath();
        String title="";
        assert filePath != null;
        for(int i = filePath.lastIndexOf("/")+1; i<filePath.length(); i++){
            title=title+filePath.charAt(i);
        }

        this.getSupportActionBar().setTitle(title);
    }
}