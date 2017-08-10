package com.test.cw.tvui.util;

import android.support.v4.app.FragmentActivity;

public class OnBackPressedListener implements BaseBackPressedInterface {
    final FragmentActivity activity;

    public OnBackPressedListener(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void doBack() {
        System.out.println("OnBackPressedListener / _doBack");
//        activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        activity.getSupportFragmentManager().popBackStack();
    }
}

interface BaseBackPressedInterface {
    void doBack();
}