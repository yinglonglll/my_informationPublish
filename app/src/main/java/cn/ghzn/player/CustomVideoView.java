package cn.ghzn.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.VideoView;

public class    CustomVideoView extends VideoView {
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();//获取的是默认显示器的宽高，即实现全屏而不是父控件的宽高
//        int height = wm.getDefaultDisplay().getHeight();

        ViewGroup viewGroup = (ViewGroup)getParent();
        if (null != viewGroup) {//父控件不为空，则获取父控件的宽高，并设置宽高；
            int parentWidth = viewGroup.getWidth();
            int parentHeight = viewGroup.getHeight();

            setMeasuredDimension(parentWidth, parentHeight);
        }

//        setMeasuredDimension(width, height);
    }
}
