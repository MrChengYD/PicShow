package inc.os;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import inc.os.picshow.PicGroup;


public class MainActivity extends Activity implements PicGroup.OnPicClick, PicGroup.OnPicClose {

    private Context context;
    private PicGroup picGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);


        List<String> pic_paths = getPicSource();
        picGroup = findViewById(R.id.pic_group);
        picGroup.bindData(pic_paths).setOnPicClickListener(this).setOnPicCloseListener(this).show();

    }
    private List<String> getPicSource(){
        return Arrays.asList(
                "https://ddim.oss-cn-hangzhou.aliyuncs.com/pics/cdc016735b3945e9a2d53272aa688069",
                "https://ddim.oss-cn-hangzhou.aliyuncs.com/pics/cdc016735b3945e9a2d53272aa688069",
                "https://ddim.oss-cn-hangzhou.aliyuncs.com/pics/cdc016735b3945e9a2d53272aa688069",
                "https://ddim.oss-cn-hangzhou.aliyuncs.com/pics/cdc016735b3945e9a2d53272aa688069",
                "https://ddim.oss-cn-hangzhou.aliyuncs.com/pics/cdc016735b3945e9a2d53272aa688069"
                );
    }

    @Override
    public void clickPicPath(int count, int current, String pic_path) {

    }

    @Override
    public void closePicPath(String pic_path) {

    }
}
