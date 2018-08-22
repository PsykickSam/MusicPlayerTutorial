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
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import tutorial.player.music.dmcx.musicplayer_tutorial.Adapter.SongAdapter;
import tutorial.player.music.dmcx.musicplayer_tutorial.Models.Song;
import tutorial.player.music.dmcx.musicplayer_tutorial.R;
import tutorial.player.music.dmcx.musicplayer_tutorial.Services.MusicService;
import tutorial.player.music.dmcx.musicplayer_tutorial.Utility.AppAlertDialog;
import tutorial.player.music.dmcx.musicplayer_tutorial.Utility.Utils;
import tutorial.player.music.dmcx.musicplayer_tutorial.Variables.Vars;

public class MainActivity extends AppCompatActivity {

    // Variables
    public static MainActivity instance;

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1234;
    private static final int minSizeOfSong = 1000000;

    private ArrayList<Song> songs;

    private ListView songsLV;
    private TextView songTitleTV, songArtistTV, updateTimeTV, endingTimeTV;
    private Button playPauseBTN, prevBTN, nextBTN;
    private SeekBar musicSeekbar;

    private SongAdapter songAdapter;
    private MusicService mMusicService;
    private Intent playIntent;
    private Handler seekHandler;
    private Runnable seekRunnable;
    private String currentSongTag;

    private int setSeekBarCurrentPosition;
    private boolean isUserSeeking = false;
    // Variables

    // Static methods
    public static void staticPlayNext() {
        instance.playNext();
    }
    // Static methods

    // Methods
    private void init() {
        songsLV = findViewById(R.id.songsLV);
        songTitleTV = findViewById(R.id.songTitleTV);
        playPauseBTN = findViewById(R.id.playPauseBTN);
        songArtistTV = findViewById(R.id.songArtistTV);
        prevBTN = findViewById(R.id.prevBTN);
        nextBTN = findViewById(R.id.nextBTN);
        musicSeekbar = findViewById(R.id.musicSeekbar);
        updateTimeTV = findViewById(R.id.updateTimeTV);
        endingTimeTV = findViewById(R.id.endingTimeTV);

        songs = new ArrayList<>();
        seekHandler = new Handler();
        currentSongTag = "";
    }

    private void props() {
        loadSongs();

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

        prevBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });

        nextBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });

        musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setSeekBarCurrentPosition = i;
                updateTimeTV.setText(Utils.getTimeString((long) setSeekBarCurrentPosition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                mMusicService.seek(setSeekBarCurrentPosition);
                updateTimeTV.setText(Utils.getTimeString((long) setSeekBarCurrentPosition));
                updateSongPosition();
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
            if (songs != null) {
                songs.clear();
            }

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
                    long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    if (size > minSizeOfSong) {
                        songs.add(new Song(thisId, thisTitle, thisArtist, ""));
                    }
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
        if (!currentSongTag.equals(view.getTag().toString())) {
            currentSongTag = view.getTag().toString();
            mMusicService.setSong(Integer.parseInt(currentSongTag));
            mMusicService.playSong();
            destroySeekbarRunnable();

            setupSongInfo();
        }
    }

    /*
    * Set up song info
    * */
    private void setupSongInfo() {
        songTitleTV.setText(mMusicService.getSongTitle());
        songArtistTV.setText(mMusicService.getSongArtist());
        playPauseBTN.setBackground(getResources().getDrawable(R.drawable.pause_black, null));

        prepareSongPlayback();
    }

    /*
    * Get durations
    * */
    private void prepareSongPlayback() {
        musicSeekbar.setProgress(0);
        updateTimeTV.setText(R.string.zoro_secs);
        endingTimeTV.setText(R.string.buffuring);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endingTimeTV.setText(Utils.getTimeString((long) mMusicService.getDuration()));
                musicSeekbar.setMax(mMusicService.getDuration());
                updateSongPosition();
            }
        }, 300);
    }

    /*
    * Play next
    * */
    private void playNext() {
        mMusicService.playNext();
        songTitleTV.setText(mMusicService.getSongTitle());
        setupSongInfo();
    }

    /*
     * Play prev
     * */
    private void playPrev() {
        mMusicService.playPrev();
        songTitleTV.setText(mMusicService.getSongTitle());
        setupSongInfo();
    }

    /*
    * Update song position
    * */
    private void updateSongPosition() {
        seekHandler.postDelayed(setSeekbarRunnable(), 1000);
    }

    /*
    * RUNNABLE: SEEKBAR
    * */
    private Runnable setSeekbarRunnable() {
        seekRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isUserSeeking) {
                    final int getCurrentPostision = mMusicService.getSongCurrentPosition();
                    musicSeekbar.setProgress(getCurrentPostision);
                    updateTimeTV.setText(Utils.getTimeString(getCurrentPostision));
                    seekHandler.postDelayed(this, 1000);
                }
            }
        };

        return seekRunnable;
    }
    private void destroySeekbarRunnable() {
        try {
            seekHandler.removeCallbacks(seekRunnable);
        } catch (Exception ex) {
            Log.d(Vars.APPTAG, "destroySeekbarRunnable: :SONG HAS NO RUNNABLE: " + ex.getMessage());
        }
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
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
