package com.dastbigharar.focus;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import java.util.Set;

public class AppBlockAccessibilityService extends AccessibilityService {
    private long lastLaunch=0;
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(event==null || event.getPackageName()==null) return;
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        if(!Prefs.active(this)){ if(Prefs.end(this)>0 && Prefs.end(this)<=System.currentTimeMillis()) Prefs.stop(this); return; }
        String pkg=event.getPackageName().toString();
        if(pkg.equals(getPackageName())) return;
        Set<String> selected=Prefs.getSelected(this);
        if(selected.contains(pkg)){
            long now=System.currentTimeMillis(); if(now-lastLaunch<700) return; lastLaunch=now;
            performGlobalAction(GLOBAL_ACTION_HOME);
            Intent i=new Intent(this,BlockedActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(i);
        }
    }
    @Override public void onInterrupt(){}
}
