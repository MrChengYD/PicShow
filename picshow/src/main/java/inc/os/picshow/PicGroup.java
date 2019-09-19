package inc.os.picshow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/** 自定义组件
 * 图片显示方式 模仿 facebook 的方式
 * 最多显示4张
 *      |------------------------|
 *      |                        |
 *      |            1           |
 *      |                        |
 *      |------------------------|
 *
 *
 *
 *
 *      |-----------------------|
 *      |           \           |
 *      |      1    \     2     |
 *      |           \           |
 *      |-----------------------|




 *      |-----------------------|
 *      |           \      2    |
 *      |     1     \ ----------|
 *      |           \      3    |
 *      |-----------------------|
 *
 *
 *
 *
 *
 *      |-----------------------|
 *      |           \           |
 *      |     1     \    2      |
 *      |-----------------------|
 *      |     3     \     4     |
 *      |           \           |
 *      |-----------------------|
 *
 *
 *
 *
 *      |-----------------------|
 *      |           \      2    |
 *      |     1     \ ----------|
 *      |           \      3    |
 *      |-----------------------|
 *      |           \           |
 *      |     4     \     5     |
 *      |           \           |
 *      |-----------------------|
 *
 *
 *
 *
 *      |-----------------------|
 *      |           \      2    |
 *      |     1     \ ----------|
 *      |           \      3    |
 *      |-----------------------|
 *      |      \       |       \
 *      |   4  \   5   |   6   \
 *      |-----------------------|
 *


 * **/
public class PicGroup extends LinearLayout implements PicItem.OnPicShowItemPic ,PicItem.OnPicShowItemClose{
    private Context context;
    private Handler handler = new Handler();
    private View pic_group_view;
    private boolean fold;
    private boolean enableClose;
    private int space_px;
    private Drawable background;

    private int LIMIT_MAX = 6;

    private int screen_width;
    private List<String> out_pic_paths;
    private List<String> handle_pic_paths;
    private List<PicItem> list_pic_item = new ArrayList<>();
    private List<RequestOptions> requestOptionList = new ArrayList<>();

    private OnPicClick onPicClick;
    private OnPicClose onPicClose;


    public PicGroup(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(VERTICAL);
        this.context = context;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screen_width = dm.widthPixels;

        final float scale = context.getResources().getDisplayMetrics().density;
        space_px = (int)(2 * scale);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet , R.styleable.PicGroup);
        if( null != typedArray){
            //是否折叠显示
            fold = typedArray.getBoolean(R.styleable.PicGroup_fold , true);
            //折叠 显示 6 张 ， 不折叠 显示 9 张
            LIMIT_MAX = fold ? 6 : 9;

            //是否 点击关闭
            enableClose = typedArray.getBoolean(R.styleable.PicGroup_enableClose , false);

            background = typedArray.getDrawable(R.styleable.PicGroup_background);

            //回收TypedArray，以便后面重用。在调用这个函数后，你就不能再使用这个TypedArray。
            typedArray.recycle();
        }
    }




    // 设置 网络资源路径 或者本地资源路径
    public PicGroup bindData(@NonNull List<String> out_pic_paths){
        this.out_pic_paths = out_pic_paths;

        this.handle_pic_paths = out_pic_paths;

        if(handle_pic_paths.size() > LIMIT_MAX){
            handle_pic_paths = handle_pic_paths.subList(0 , LIMIT_MAX);
        }

        return this;
    }


    public PicGroup setOnPicClickListener(OnPicClick onPicClickListener){
        this.onPicClick = onPicClickListener;
        return this;
    }

    public PicGroup setOnPicCloseListener(OnPicClose onPicCloseListener){
        this.onPicClose = onPicCloseListener;
        return this;
    }
    public void show(){
        if(null == out_pic_paths || out_pic_paths.size() < 1){
            throw new RuntimeException("请先设置图片资源路径!");
        }else{
            //根据传入的数据 加载不同的布局
            new Thread(() -> handler.post(this::initAndShow)).start();
            //initAndShow();
        }
    }

    private void initAndShow(){
        //加载 折叠布局

        if(fold){
            //当选择折叠布局时 不可点击关闭
            enableClose = false;
            initAndShowFoldLayout();
        }else{
            //加载不折叠布局
            initAndShowVerticalLayout();
        }






    }


    private void initAndShowFoldLayout(){
        //加载布局
        pic_group_view =  LayoutInflater.from(context).inflate(R.layout.layout_pic_group_fold, this);
        if(null != background){
            pic_group_view.setBackground(background);
        }
        PicItem pic_item_1 = findViewById(R.id.pic_item_1);

        RelativeLayout right_half = findViewById(R.id.right_half);
        if(handle_pic_paths.size() != 1){
            right_half.setVisibility(VISIBLE);
        }
        PicItem pic_item_2_1 = findViewById(R.id.pic_item_2_1);


        PicItem pic_item_3 = findViewById(R.id.pic_item_3);
        PicItem pic_item_4 = findViewById(R.id.pic_item_4);
        PicItem pic_item_5 = findViewById(R.id.pic_item_5);
        list_pic_item.add(pic_item_1);
        list_pic_item.add(pic_item_2_1);
        if(handle_pic_paths.size() == 3 || handle_pic_paths.size() == 5 || handle_pic_paths.size() == 6){
            PicItem pic_item_2_2 = findViewById(R.id.pic_item_2_2);
            list_pic_item.add(pic_item_2_2);
        }
        list_pic_item.add(pic_item_3);
        list_pic_item.add(pic_item_4);
        list_pic_item.add(pic_item_5);
        //show
        initRequestOptions(handle_pic_paths.size());

        try{
            for(int i = 0 ; i < handle_pic_paths.size(); i ++){
                ((View)list_pic_item.get(i).getParent()).setVisibility(VISIBLE);

                list_pic_item.get(i).setPicClickListener(this).show(handle_pic_paths.get(i) , requestOptionList.get(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initAndShowVerticalLayout(){
        pic_group_view = LayoutInflater.from(context).inflate(R.layout.layout_pic_group_vertical, this);
        if(null != background){
            pic_group_view.setBackground(background);
        }

        //获取屏幕宽度

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override(screen_width , screen_width * 3 / 5).centerCrop().skipMemoryCache(true);

        try{
            for(int i = 0 ; i < handle_pic_paths.size() && i < LIMIT_MAX ; i ++){
                PicItem picItem = new PicItem(context , null);

                picItem.setPicClickListener(this).setEnableClose(enableClose , enableClose ? this : null).show(handle_pic_paths.get(i) , requestOptions);
                addView(picItem);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(picItem.getLayoutParams());
                layoutParams.setMargins(0 , space_px , 0 , 0);
                picItem.setLayoutParams(layoutParams);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    private void initRequestOptions(int picSize){
        switch (picSize){
            case 1 : {
                RequestOptions requestOption_1 = new  RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override(screen_width, screen_width).centerCrop().skipMemoryCache(true);
                requestOptionList.add(requestOption_1);
                break;
            }
            case 2 : {
                RequestOptions requestOption_1 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , screen_width).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_2 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , screen_width).centerCrop().skipMemoryCache(true);
                requestOptionList.add(requestOption_1);
                requestOptionList.add(requestOption_2);
                break;
            }
            case 3 : {
                RequestOptions requestOption_1 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2  , screen_width ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_2 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_3 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                requestOptionList.add(requestOption_1);
                requestOptionList.add(requestOption_2);
                requestOptionList.add(requestOption_3);
                break;
            }

            case 4 : {
                RequestOptions requestOption_1 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_2 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_3 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_4 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                requestOptionList.add(requestOption_1);
                requestOptionList.add(requestOption_2);
                requestOptionList.add(requestOption_3);
                requestOptionList.add(requestOption_4);
                break;
            }
            case 5 : {
                RequestOptions requestOption_1 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2  , screen_width * 3 / 5 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_2 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width * 3 / 5 - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_3 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width * 3 / 5 - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_4 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2  , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_5 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2  , (screen_width - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                requestOptionList.add(requestOption_1);
                requestOptionList.add(requestOption_2);
                requestOptionList.add(requestOption_3);
                requestOptionList.add(requestOption_4);
                requestOptionList.add(requestOption_5);
                break;
            }
            case 6 :{
                RequestOptions requestOption_1 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2  , screen_width * 3 / 5 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_2 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width * 3 / 5 - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_3 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px) / 2 , (screen_width * 3 / 5 - space_px) / 2 ).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_4 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px * 2) / 3 , (screen_width - space_px * 2) / 3).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_5 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px * 2) / 3 , (screen_width - space_px * 2) / 3).centerCrop().skipMemoryCache(true);
                RequestOptions requestOption_6 = new RequestOptions().placeholder(R.drawable.icon_loading).error(R.drawable.icon_load_error).override((screen_width - space_px * 2) / 3 , (screen_width - space_px * 2) / 3).centerCrop().skipMemoryCache(true);

                requestOptionList.add(requestOption_1);
                requestOptionList.add(requestOption_2);
                requestOptionList.add(requestOption_3);
                requestOptionList.add(requestOption_4);
                requestOptionList.add(requestOption_5);
                requestOptionList.add(requestOption_6);
                break;
            }
        }

    }



    public interface OnPicClick{
        void clickPicPath(int count , int current , String pic_path);
    }
    public interface OnPicClose{
        void closePicPath(String pic_path);
    }

    @Override
    public void clickPic(String pic_path) {
        if(null != onPicClick){
            onPicClick.clickPicPath(out_pic_paths.size() , out_pic_paths.indexOf(pic_path) , pic_path);
        }
    }
    @Override
    public void closePic(String pic_path) {
        if(null != onPicClose){
            onPicClose.closePicPath(pic_path);
        }
    }

}
