package com.aaron.musicplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.aaron.musicplay.MusicService.MyBinder;
import com.example.music.R;

public class MainActivity extends Activity {

	private SeekBar seekBar;
	private TextView music_progress;// 当前时间
	private TextView music_duration;// 总时长
	private TextView nameView;// 总时长
	private MusicService musicService;
	private MediaPlayer mediaPlayer;
	private List<MusicInfo> musics = new ArrayList<MusicInfo>();
	private int index;
	private MyServiceConnection conn;
	private SimpleDateFormat format = new SimpleDateFormat("mm:ss");

	private ListView listView;

	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			seekBar.setProgress(mediaPlayer.getCurrentPosition());
			music_progress.setText(format.format(new Date(mediaPlayer.getCurrentPosition())));
			handler.postDelayed(runnable, 100);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		music_duration = (TextView) findViewById(R.id.duration);
		music_progress = (TextView) findViewById(R.id.progress);
		nameView = (TextView) findViewById(R.id.name);
		listView = (ListView) findViewById(R.id.listview);
		// 获取音乐
		initData();
		// 开启服务
		Intent intent = new Intent(this, MusicService.class);
		startService(intent);
		// 绑定服务
		conn = new MyServiceConnection();
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				playMusic(position);
			}
		});
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && mediaPlayer != null) {
					mediaPlayer.seekTo(progress);
				}
			}
		});
	}
	
	/**
	 * 获取音乐
	 */
	public void initData() {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			cursor = this.getContentResolver().query(uri, null, null, null, null);
			while (cursor != null && cursor.moveToNext()) {
				MusicInfo info = new MusicInfo();
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				info.path = path;
				info.name = name;
				info.duration = duration;
				Log.i("info", "name:" + name);
				if (info.path != null && !info.path.equals("") && duration != 0) {
					musics.add(info);
				}
			}
			listView.setAdapter(new MusicAdapter(this, musics));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private class MyServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicService.MyBinder binder = (MyBinder) service;
			musicService = binder.getMusicService();
			mediaPlayer = musicService.mediaPlayer;
			Log.i("INFO", "serviceConnected");
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// 播放完毕
				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

	}

	public void pause(View view) {
		if (mediaPlayer.isPlaying()) {
			musicService.pause();
			handler.removeCallbacks(runnable);
		}
	}

	public void play(View view) {
		playMusic(index);
	}

	public void next(View view) {
		index++;
		if (index == musics.size()) {
			index = 0;
		}
		playMusic(index);
	}

	public void pre(View view) {
		index--;
		if (index < 0) {
			index = musics.size() - 1;
		}
		playMusic(index);
	}

	public void playMusic(int index) {
		if (musics.size() > 0) {
			musicService.play(musics.get(index).path);
			music_duration.setText((format.format(new Date(mediaPlayer.getDuration()))));
			nameView.setText(musics.get(index).name);
			handler.removeCallbacks(runnable);
			handler.post(runnable);
		}
		seekBar.setMax(mediaPlayer.getDuration());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
