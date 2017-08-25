package com.test.cw.tvui.util;

import android.support.v4.app.FragmentActivity;
import com.test.cw.tvui.operation.Import_fileListAct;

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
        Import_fileListAct.isBack_fileView = true;
    }
}

interface BaseBackPressedInterface {
    void doBack();
}