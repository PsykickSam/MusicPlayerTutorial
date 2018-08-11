package tutorial.player.music.dmcx.musicplayer_tutorial.Adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tutorial.player.music.dmcx.musicplayer_tutorial.Activities.MainActivity;
import tutorial.player.music.dmcx.musicplayer_tutorial.Models.Song;
import tutorial.player.music.dmcx.musicplayer_tutorial.R;

public class SongAdapter extends BaseAdapter {

    // Variables
    private ArrayList<Song> songs;
    private Context ctx;
    // Variables


    public SongAdapter(Context ctx, ArrayList<Song> songs) {
        this.songs = songs;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder")
        View songView = LayoutInflater.from(ctx).inflate(R.layout.layout_single_song, viewGroup, false);

        TextView songTitleTV = songView.findViewById(R.id.songTitleTV);
        TextView songArtistTV = songView.findViewById(R.id.songArtistTV);

        Song currentSong = songs.get(i);

        songTitleTV.setText(currentSong.getTitle());
        songArtistTV.setText(currentSong.getArtist());

        songView.setTag(i);
        return songView;
    }
}
