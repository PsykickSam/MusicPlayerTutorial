package tutorial.player.music.dmcx.musicplayer_tutorial.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.util.Log;

import java.util.ArrayList;

import tutorial.player.music.dmcx.musicplayer_tutorial.Activities.MainActivity;
import tutorial.player.music.dmcx.musicplayer_tutorial.Models.Song;
import tutorial.player.music.dmcx.musicplayer_tutorial.R;

public class MusicService extends Service
    implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{

    // Class
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    // Class

    // Variables
    private static final int NOTIFY_ID = 1234;

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private String songTitle;

    private final IBinder binder = new MusicBinder();
    // Variables

    // Methods
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    /*
    * Play song
    * */
    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        long currentSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception ex) {
            Log.e("MUSIC SERVICE", "Error setting data source", ex);
        }

        player.prepareAsync();
    }

    /*
    * Set song
    * */
    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    /*
    * Position
    * return: Song position
    * */
    public int getSongPosition() {
        return player.getCurrentPosition();
    }

    /*
    * Duration
    * return: Song duration
    * */
    public int getDuration() {
        return player.getDuration();
    }

    /*
    * Is playing
    * return: true/false
    * */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /*
    * Pause
    * return: void
    * */
    public void pause() {
        player.pause();
    }

    /*
    * Seek
    * return: void
    * */
    public void seek(int position) {
        player.seekTo(position);
    }

    /*
    * Start
    * return: void
    * */
    public void start() {
        player.start();
    }

    /*
    * Play prev
    * return: void
    * */
    public void playPrev() {
        songPosition--;
        if (songPosition < 0)
            songPosition = songs.size() - 1;
        playSong();
    }

    /*
    * Play next
    * return void
    * */
    public void playNext() {
        songPosition++;
        if (songPosition >= songs.size()) {
            songPosition = 0;
        }
        playSong();
    }

    /*
    * Title
    * return: song title
    * */
    public String getSongTitle() {
        return songTitle;
    }
    // Methods

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        player = new MediaPlayer();

        initMusicPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play_black)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        Notification notification = builder.build();
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
