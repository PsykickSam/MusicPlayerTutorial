package tutorial.player.music.dmcx.musicplayer_tutorial.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tutorial.player.music.dmcx.musicplayer_tutorial.Adapter.SongAdapter;
import tutorial.player.music.dmcx.musicplayer_tutorial.Models.Song;
import tutorial.player.music.dmcx.musicplayer_tutorial.Player.MusicControl;
import tutorial.player.music.dmcx.musicplayer_tutorial.Player.MusicController;
import tutorial.player.music.dmcx.musicplayer_tutorial.R;
import tutorial.player.music.dmcx.musicplayer_tutorial.Services.MusicService;
import tutorial.player.music.dmcx.musicplayer_tutorial.Utility.AppAlertDialog;
import tutorial.player.music.dmcx.musicplayer_tutorial.Variables.Vars;

public class MainActivity extends AppCompatActivity {

    // Variables
    public static MainActivity instance;

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1234;

    private ArrayList<Song> songs;

    private ListView songsLV;
    private TextView songTitleTV;
    private Button playPauseBTN;

    private SongAdapter songAdapter;
    private MusicControl mMusicControl;
    private MusicController mMusicController;
    private MusicService mMusicService;
    private Intent playIntent;
    private boolean musicBound = false;
    // Variables

    // Methods
    private void init() {
        songsLV = findViewById(R.id.songsLV);
        songTitleTV = findViewById(R.id.songTitleTV);
        playPauseBTN = findViewById(R.id.playPauseBTN);

        songs = new ArrayList<>();

        mMusicControl = new MusicControl(mMusicService, musicBound);
        mMusicController = new MusicController(instance);
        mMusicController.setMediaPlayer(mMusicControl);
        mMusicController.setAnchorView(findViewById(R.id.songsLV));
        mMusicController.setEnabled(true);
    }

    private void props() {
        loadSongs();

        mMusicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play next
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play prev
            }
        });

        playPauseBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMusicService.isPlaying()) {
                    mMusicService.pause();
                    playPauseBTN.setBackground(getResources().getDrawable(R.drawable.play_black, null));
                } else {
                    mMusicService.start();
                    playPauseBTN.setBackground(getResources().getDrawable(R.drawable.pause_black, null));
                }
            }
        });
    }

    /*
    * Check Permissions
    * */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(instance, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    //
    private void getSongList() {
        // Retrive song info

        if (checkPermission()) {
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            @SuppressLint("Recycle")
            Cursor cursor = musicResolver.query(musicUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

                do {
                    long thisId = cursor.getLong(idCol);
                    String thisTitle = cursor.getString(titleCol);
                    String thisArtist = cursor.getString(artistCol);
                    songs.add(new Song(thisId, thisTitle, thisArtist));
                } while (cursor.moveToNext());

                Collections.sort(songs, new Comparator<Song>() {
                    @Override
                    public int compare(Song song, Song t1) {
                        return song.getTitle().compareTo(t1.getTitle());
                    }
                });
            }
        }

    }

    /*
    * Load songs
    * */
    private void loadSongs() {
        getSongList();
        songAdapter = new SongAdapter(instance, songs);
        songsLV.setAdapter(songAdapter);
    }

    /*
    * Song picked
    * */
    public void songPicked(View view){
        mMusicService.setSong(Integer.parseInt(view.getTag().toString()));
        mMusicService.playSong();

        songTitleTV.setText(mMusicService.getSongTitle());
        playPauseBTN.setBackground(getResources().getDrawable(R.drawable.pause_black, null));
    }

    /*
    * Play next
    * */
    private void playNext() {
        mMusicService.playNext();
        mMusicController.show(0);
    }

    /*
     * Play prev
     * */
    private void playPrev() {
        mMusicService.playPrev();
        mMusicController.show(0);
    }
    // Methods

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        Vars.appAlertDialog = new AppAlertDialog();

        init();
        props();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            mMusicService = binder.getService();
            //pass list
            mMusicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionEndMI:
                stopService(playIntent);
                mMusicService=null;
                finish();
                break;
            case R.id.actionSuffleMI:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isPermissionGrantedForExternalStorage = false;

        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isPermissionGrantedForExternalStorage = false;
                        Vars.appAlertDialog.createDialog(
                                instance, false, "Permission",
                                "The app need storage permission to get the music files", "Yes", "No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Positive
                                        checkPermission();
                                        Vars.appAlertDialog.dismiss();
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Negative
                                        instance.finish();
                                        Vars.appAlertDialog.dismiss();
                                    }
                                }).show();
                        break;
                    } else {
                        isPermissionGrantedForExternalStorage = true;
                    }
                }
            }
        }

        if (isPermissionGrantedForExternalStorage) {
            loadSongs();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(instance, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMusicService = null;
        stopService(playIntent);
    }
}
