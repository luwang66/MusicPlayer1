package com.example.musicplayer1;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

public class MusicService extends Service{
	ServiceReceiver serviceReceiver;
	AssetManager am;//资源管理器
	String[] musics = new String[]{
		"world.mp3",
		"sky.mp3",
	};//定义几首歌曲
	MediaPlayer mPlayer;
	//当前的状态,0x11 代表没有播放 ；0x12代表 正在播放；0x13代表暂停
	int status = 0x11;
	// 记录当前正在播放的音乐
	int current = 0;
	public IBinder onBind(Intent intent){
		return null;
	}
	public void onCreate(){
		am = getAssets();//调用Context里的方法
		// 创建BroadcastReceiver
		serviceReceiver = new ServiceReceiver();
		// 创建IntentFilter
		IntentFilter filter = new IntentFilter(MusicActivity.CONTROL);		
		registerReceiver(serviceReceiver, filter);
		// 创建MediaPlayer
		mPlayer = new MediaPlayer();
		// 为MediaPlayer播放完成事件绑定监听器
		mPlayer.setOnCompletionListener(new OnCompletionListener(){
			public void onCompletion(MediaPlayer mp){
				current++;
				if (current >= 3){
					current = 0;
				}
				/* 发送广播通知Activity更改文本框 */
				Intent sendIntent = new Intent(MusicActivity.UPDATE);
				sendIntent.putExtra("current", current);
				// 发送广播 ，将被Activity组件中的BroadcastReceiver接收到
				sendBroadcast(sendIntent);				
				// 准备、并播放音乐
				prepareAndPlay(musics[current]);				
			}		
		});
		super.onCreate();
	}
	public class ServiceReceiver extends BroadcastReceiver{
		public void onReceive(final Context context, Intent intent){
			int control = intent.getIntExtra("control", -1);
			switch (control){
				// 播放或暂停
				case 1:
					// 原来处于没有播放状态
					if (status == 0x11){
						// 准备、并播放音乐
						prepareAndPlay(musics[current]);
						status = 0x12;
					}
					// 原来处于播放状态
					else if (status == 0x12){						
						mPlayer.pause();// 暂停						
						status = 0x13;// 改变为暂停状态
					}
					// 原来处于暂停状态
					else if (status == 0x13){						
						mPlayer.start();// 播放						
						status = 0x12;// 改变状态
					}
					break;
				// 停止声音
				case 2:
					// 如果原来正在播放或暂停
					if (status == 0x12 || status == 0x13){						
						mPlayer.stop();// 停止播放
						status = 0x11;
					}
			}
			/* 发送广播通知Activity更改图标、文本框 */
			Intent sendIntent = new Intent(MusicActivity.UPDATE);
			sendIntent.putExtra("update", status);
			sendIntent.putExtra("current", current);
			// 发送广播 ，将被Activity组件中的BroadcastReceiver接收到
			sendBroadcast(sendIntent);			
		}
	}
	private void prepareAndPlay(String music){
		try{
			//打开指定音乐文件
			AssetFileDescriptor afd = am.openFd(music);
			mPlayer.reset();
			//使用MediaPlayer加载指定的声音文件。
			mPlayer.setDataSource(afd.getFileDescriptor()
				, afd.getStartOffset()
				, afd.getLength());				
			mPlayer.prepare();// 准备声音			
			mPlayer.start();// 播放
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(serviceReceiver);
	}
}
