package br.com.lpn.radiolpn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

/**
 * Created by Fernando on 27/12/2015.
 */
public class MainButton extends PlayPauseButton{

    private boolean active = true;

    public MainButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(active)
            super.onTouchEvent(event);
        return !active;
    }


    public void setActive(boolean active) {
        this.active = active;
    }


}
