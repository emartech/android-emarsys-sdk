package com.emarsys.mobileengage.iam;


import com.emarsys.mobileengage.MobileEngage;

public class DoNotDisturbProvider {

    public boolean isPaused(){
        return MobileEngage.InApp.isPaused();
    }

}