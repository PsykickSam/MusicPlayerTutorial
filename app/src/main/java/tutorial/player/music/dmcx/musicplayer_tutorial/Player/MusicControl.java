package tutorial.player.music.dmcx.musicplayer_tutorial.Player;

import android.widget.MediaController;

import tutorial.player.music.dmcx.musicplayer_tutorial.Services.MusicService;

public class MusicControl implements MediaController.MediaPlayerControl {

    private MusicService mMusicService;
    private boolean musicBound;

    public MusicControl(MusicService mMusicService, boolean musicBound) {
        this.mMusicService = mMusicService;
        this.musicBound = musicBound;
    }

    @Override
    public void start() {
        mMusicService.start();
    }

    @Override
    public void pause() {
        mMusicService.pause();
    }

    @Override
    public int getDuration() {
        if (mMusicService != null && musicBound && mMusicService.isPlaying()) {
            return mMusicService.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicService != null && musicBound && mMusicService.isPlaying()) {
            return mMusicService.getSongPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int i) {
        mMusicService.seek(i);
    }

    @Override
    public boolean isPlaying() {
        return mMusicService != null && musicBound && mMusicService.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
