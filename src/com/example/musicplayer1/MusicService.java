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
	AssetManager am;//��Դ������
	String[] musics = new String[]{
		"world.mp3",
		"sky.mp3",
	};//���弸�׸���
	MediaPlayer mPlayer;
	//��ǰ��״̬,0x11 ����û�в��� ��0x12���� ���ڲ��ţ�0x13������ͣ
	int status = 0x11;
	// ��¼��ǰ���ڲ��ŵ�����
	int current = 0;
	public IBinder onBind(Intent intent){
		return null;
	}
	public void onCreate(){
		am = getAssets();//����Context��ķ���
		// ����BroadcastReceiver
		serviceReceiver = new ServiceReceiver();
		// ����IntentFilter
		IntentFilter filter = new IntentFilter(MusicActivity.CONTROL);		
		registerReceiver(serviceReceiver, filter);
		// ����MediaPlayer
		mPlayer = new MediaPlayer();
		// ΪMediaPlayer��������¼��󶨼�����
		mPlayer.setOnCompletionListener(new OnCompletionListener(){
			public void onCompletion(MediaPlayer mp){
				current++;
				if (current >= 3){
					current = 0;
				}
				/* ���͹㲥֪ͨActivity�����ı��� */
				Intent sendIntent = new Intent(MusicActivity.UPDATE);
				sendIntent.putExtra("current", current);
				// ���͹㲥 ������Activity����е�BroadcastReceiver���յ�
				sendBroadcast(sendIntent);				
				// ׼��������������
				prepareAndPlay(musics[current]);				
			}		
		});
		super.onCreate();
	}
	public class ServiceReceiver extends BroadcastReceiver{
		public void onReceive(final Context context, Intent intent){
			int control = intent.getIntExtra("control", -1);
			switch (control){
				// ���Ż���ͣ
				case 1:
					// ԭ������û�в���״̬
					if (status == 0x11){
						// ׼��������������
						prepareAndPlay(musics[current]);
						status = 0x12;
					}
					// ԭ�����ڲ���״̬
					else if (status == 0x12){						
						mPlayer.pause();// ��ͣ						
						status = 0x13;// �ı�Ϊ��ͣ״̬
					}
					// ԭ��������ͣ״̬
					else if (status == 0x13){						
						mPlayer.start();// ����						
						status = 0x12;// �ı�״̬
					}
					break;
				// ֹͣ����
				case 2:
					// ���ԭ�����ڲ��Ż���ͣ
					if (status == 0x12 || status == 0x13){						
						mPlayer.stop();// ֹͣ����
						status = 0x11;
					}
			}
			/* ���͹㲥֪ͨActivity����ͼ�ꡢ�ı��� */
			Intent sendIntent = new Intent(MusicActivity.UPDATE);
			sendIntent.putExtra("update", status);
			sendIntent.putExtra("current", current);
			// ���͹㲥 ������Activity����е�BroadcastReceiver���յ�
			sendBroadcast(sendIntent);			
		}
	}
	private void prepareAndPlay(String music){
		try{
			//��ָ�������ļ�
			AssetFileDescriptor afd = am.openFd(music);
			mPlayer.reset();
			//ʹ��MediaPlayer����ָ���������ļ���
			mPlayer.setDataSource(afd.getFileDescriptor()
				, afd.getStartOffset()
				, afd.getLength());				
			mPlayer.prepare();// ׼������			
			mPlayer.start();// ����
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
