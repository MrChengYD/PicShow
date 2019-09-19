package inc.os.picshow.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.SoundPool;

import inc.os.picshow.R;

public class SoundPoolUtil {
    private static SoundPoolUtil soundPoolUtil;
    private SoundPool soundPool;

    //单例模式
    public static SoundPoolUtil getInstance(Context context) {
        if (soundPoolUtil == null){
            return new SoundPoolUtil(context);
        }
        return soundPoolUtil;
    }

    private SoundPoolUtil(Context context) {
        soundPool = new SoundPool.Builder().build();
        //加载音频文件
        soundPool.load(context, R.raw.click_sound, 1);

    }

    public void play(int number) {
        //播放音频
        soundPool.play(number, 1, 1, 0, 0, 1);
    }
}