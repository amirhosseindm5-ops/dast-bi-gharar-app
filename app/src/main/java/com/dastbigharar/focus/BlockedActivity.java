package com.dastbigharar.focus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class BlockedActivity extends Activity {
    private TextView time;
    private final Handler h=new Handler();
    private final Runnable tick=new Runnable(){ public void run(){ update(); if(Prefs.active(BlockedActivity.this)) h.postDelayed(this,1000); else finish(); }};
    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.WHITE); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); build(); }
    @Override protected void onResume(){ super.onResume(); h.post(tick); }
    @Override protected void onPause(){ super.onPause(); h.removeCallbacks(tick); }
    private void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(Ui.dp(this,30),Ui.dp(this,40),Ui.dp(this,30),Ui.dp(this,40)); root.setBackgroundColor(Color.WHITE); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView icon=Ui.text(this,"✋",52,Color.rgb(255,107,107),true); icon.setGravity(Gravity.CENTER); icon.setBackground(Ui.bg(Color.rgb(255,240,240),28,this)); root.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,96),Ui.dp(this,96)));
        TextView title=Ui.text(this,"دست نگه دار 😄",24,Ui.TEXT,true); title.setGravity(Gravity.CENTER); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2); tp.setMargins(0,Ui.dp(this,20),0,0); root.addView(title,tp);
        TextView desc=Ui.text(this,"این برنامه تا پایان زمان تمرکز قفل شده.\nچند دقیقه دیگه دوباره امتحان کن.",13,Ui.MUTED,false); desc.setGravity(Gravity.CENTER); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2); dp.setMargins(0,Ui.dp(this,8),0,0); root.addView(desc,dp);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(Ui.dp(this,18),Ui.dp(this,14),Ui.dp(this,18),Ui.dp(this,14)); box.setBackground(Ui.strokeBg(Color.rgb(248,247,255),Color.rgb(228,224,255),1,18,this)); TextView lab=Ui.text(this,"زمان باقی‌مانده",12,Ui.MUTED,false); lab.setGravity(Gravity.CENTER); box.addView(lab); time=Ui.text(this,"00:00:00",34,Ui.PURPLE,true); time.setGravity(Gravity.CENTER); box.addView(time); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2); bp.setMargins(0,Ui.dp(this,22),0,0); root.addView(box,bp);
        Button back=new Button(this); back.setText("برگشت به صفحه اصلی"); back.setAllCaps(false); back.setTextSize(14); back.setBackground(Ui.bg(Color.rgb(242,243,247),16,this)); back.setOnClickListener(v->goHome()); LinearLayout.LayoutParams bt=new LinearLayout.LayoutParams(-1,Ui.dp(this,54)); bt.setMargins(0,Ui.dp(this,16),0,0); root.addView(back,bt);
        setContentView(root); update();
    }
    private void update(){ long ms=Math.max(0,Prefs.end(this)-System.currentTimeMillis()); long sec=ms/1000, hh=sec/3600, mm=(sec%3600)/60, ss=sec%60; if(time!=null) time.setText(String.format(Locale.US,"%02d:%02d:%02d",hh,mm,ss)); if(ms<=0) Prefs.stop(this); }
    private void goHome(){ Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); finish(); }
    @Override public void onBackPressed(){ goHome(); }
}
