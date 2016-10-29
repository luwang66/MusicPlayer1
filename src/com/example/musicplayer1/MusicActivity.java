package com.example.musicplayer1;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MusicActivity extends Activity implements OnClickListener {
	//获取界面中显示歌曲标题、作者文本框
	TextView title, author;
	// 播放/暂停、停止按钮
	ImageButton play, stop;
	ActivityReceiver activityReceiver;
	public static final String CONTROL = "com.example.control";//控制播放、暂停
	public static final String UPDATE = "com.example.update";//更新界面显示
	// 定义音乐的播放状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
	int status = 0x11;
	String[] titleStrs = new String[] { "在这个世界相遇", "天籁之音"};//歌曲名
	String[] authorStrs = new String[] { "陈奕迅", "纯音乐"};//演唱者

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);
		// 获取程序界面中的两个按钮以及两个文本显示框
		play = (ImageButton) this.findViewById(R.id.play);
		stop = (ImageButton) this.findViewById(R.id.stop);
		title = (TextView) findViewById(R.id.title);
		author = (TextView) findViewById(R.id.author);
		// 为两个按钮的单击事件添加监听器
		play.setOnClickListener(this);
		stop.setOnClickListener(this);
		activityReceiver = new ActivityReceiver();
		// 创建IntentFilter
		IntentFilter filter = new IntentFilter(UPDATE);
		// 指定BroadcastReceiver监听的Action
		// filter.addAction(UPDATE_ACTION);
		// 注册BroadcastReceiver
		registerReceiver(activityReceiver, filter);
		Intent intent = new Intent(this, MusicService.class);
		startService(intent);// 启动后台Service
	}
	// 自定义的BroadcastReceiver，负责监听从Service传回来的广播
	public class ActivityReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// 获取Intent中的update消息，update代表播放状态，默认为-1
			int update = intent.getIntExtra("update", -1);
			// 获取Intent中的current消息，current代表当前正在播放的歌曲，默认为-1
			int current = intent.getIntExtra("current", -1);
			if (current >= 0) {
				title.setText(titleStrs[current]);
				author.setText(authorStrs[current]);
			}
			switch (update) {
			case 0x11:
				play.setImageResource(R.drawable.play);
				status = 0x11;
				break;
			// 控制系统进入播放状态
			case 0x12:
				// 播放状态下设置使用暂停图标
				play.setImageResource(R.drawable.pause);
				// 设置当前状态
				status = 0x12;
				break;
			// 控制系统进入暂停状态
			case 0x13:
				// 暂停状态下设置使用播放图标
				play.setImageResource(R.drawable.play);
				// 设置当前状态
				status = 0x13;
				break;
			}
		}
	}
 /*
	mPlayer.setOnCompletionListener(new OnCompletionListent(){
		public void onCompletion(MediaPlayer mp){
			current++;
			if(current>=2){
				current=0;
			}
			Intent sendIntent=new Intent (MainActivity.UPDATE);
			sendIntent.putExtra("current",current);
			sendBroadcast(sendIntent);
			prepareAndPlay(musics[current]);
		}
	}
	);
	
	private void prepareAndPlay(String music){
	 try{
		 AssetFileDescriptor afd=am.openFd(music);
		 mPlayer.reset();
		 mPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		 mPlayer.prepare();
		 mPlayer.start();
	     }
	 catch(IOException e){
		 e.printStackTrace();
	      }
      }
 
 */
	public void onClick(View source) {
		// 创建Intent
		Intent intent = new Intent(CONTROL);
		System.out.println(source.getId());
		System.out.println(source.getId() == R.id.play);
		switch (source.getId()) {
		// 按下播放/暂停按钮
		case R.id.play:
			intent.putExtra("control", 1);
			break;
		// 按下停止按钮
		case R.id.stop:
			intent.putExtra("control", 2);
			break;
		}
		// 发送广播 ，将被Service组件中的BroadcastReceiver接收到
		sendBroadcast(intent);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(activityReceiver);
	}
}
