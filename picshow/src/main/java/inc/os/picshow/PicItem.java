package inc.os.picshow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import inc.os.picshow.util.SoundPoolUtil;

public class PicItem extends RelativeLayout implements View.OnTouchListener, Animation.AnimationListener {

    private Context context;

    private String picPath;

    private View pic_item_view;
    private ImageView pic_show_item_pic;
    private boolean enableClose;
    private View pic_show_item_close;

    private OnPicShowItemPic onPicShowItemPic;

    private OnPicShowItemClose onPicShowItemClose;

    private AlphaAnimation alphaAnimation;

    private SoundPoolUtil soundPoolUtil;
    public PicItem(Context context , @Nullable AttributeSet attributeSet){
        super(context , attributeSet);


        init(context);
    }

    private void init(Context context){
        this.context = context;
        //加载布局
        pic_item_view = LayoutInflater.from(context).inflate(R.layout.layout_pic_item, this);
        pic_show_item_pic = pic_item_view.findViewById(R.id.pic_show_item_pic);
        pic_show_item_pic.setOnClickListener((v) -> {
            if(null != onPicShowItemPic){
                onPicShowItemPic.clickPic(picPath);
            }
        });

        pic_show_item_close = pic_item_view.findViewById(R.id.pic_show_item_close);

    }
    public PicItem setPicClickListener(OnPicShowItemPic onPicShowItemPic){
        this.onPicShowItemPic = onPicShowItemPic;
        return this;
    }
    public PicItem setEnableClose(boolean enableClose){
        this.enableClose = enableClose;
        if(!enableClose){
            this.onPicShowItemClose = null;
        }
        setCloseEvent(enableClose);
        return this;
    }
    public PicItem setEnableClose( boolean enableClose ,  OnPicShowItemClose onPicShowItemClose){
        this.enableClose = enableClose;

        if(enableClose && null != onPicShowItemClose){
            this.onPicShowItemClose = onPicShowItemClose;
        }
        setCloseEvent(enableClose);
        return this;
    }
    private void setCloseEvent(boolean enableClose){

        if(enableClose){
            pic_show_item_close.setVisibility(VISIBLE);
            pic_show_item_close.setOnTouchListener(this);//触摸监听
            //点击监听
            pic_show_item_close.setOnClickListener((v) ->{
                if(null != onPicShowItemClose){
                    onPicShowItemClose.closePic(picPath);
                }
                setHideAnimation(pic_item_view , 800);
            });
        }

    }

    public void show(String picPath , RequestOptions requestOptions) throws Exception{
        this.picPath = picPath;
        if( null == requestOptions){
            throw new RuntimeException("requestOptions is null .");
        }
        int pic_width = requestOptions.getOverrideWidth();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pic_show_item_close.getLayoutParams());
        layoutParams.setMargins(pic_width - layoutParams.width , 0 , 0 , 0);
        pic_show_item_close.setLayoutParams(layoutParams);
        if(picPath.startsWith("http")){
            //加载网络图片
            Glide.with(context).load(picPath).apply(requestOptions).into(pic_show_item_pic);

        }else{
            //本地图片
            File picFile = new File(picPath);
            Glide.with(context).load(picFile).apply(requestOptions).into(pic_show_item_pic);


        }
    }

    //动画效果
    private void setHideAnimation( View view, int duration){
        if (null == view || duration < 0){
            return;
        }

        if (null != alphaAnimation){
            alphaAnimation.cancel();
        }
        // 监听动画结束的操作
        alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setAnimationListener(this);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*soundPoolUtil = SoundPoolUtil.getInstance(context);
                soundPoolUtil.play(1);*/

                if (view.getId() == R.id.pic_show_item_close) {
                    pic_show_item_close.setScaleX((float) 1.2);
                    pic_show_item_close.setScaleY((float) 1.2);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (view.getId() == R.id.pic_show_item_close) {
                    pic_show_item_close.setScaleX(1);
                    pic_show_item_close.setScaleY(1);
                }
                break;
            default:
        }
        return false;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        pic_item_view.setVisibility(GONE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    public interface OnPicShowItemPic{
        void clickPic(String pic_path);
    }
    public interface OnPicShowItemClose{
        void closePic(String pic_path);
    }

}
