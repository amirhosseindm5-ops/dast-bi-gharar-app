package com.dastbigharar.focus;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.view.accessibility.AccessibilityManager;
import java.util.*;

public class MainActivity extends Activity {
    private TextView summary,count,timerState;
    private EditText hours,minutes;
    private final Handler h=new Handler();
    private final Runnable tick=new Runnable(){ public void run(){ refresh(); h.postDelayed(this,1000); }};

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(Color.WHITE); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); build(); }
    @Override protected void onResume(){ super.onResume(); refresh(); h.post(tick); }
    @Override protected void onPause(){ super.onPause(); h.removeCallbacks(tick); }

    private TextView cardTitle(String s){ return Ui.text(this,s,15,Ui.TEXT,true); }
    private TextView sub(String s){ return Ui.text(this,s,12,Ui.MUTED,false); }

    private LinearLayout card(){ LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(Ui.dp(this,16),Ui.dp(this,15),Ui.dp(this,16),Ui.dp(this,15)); c.setBackground(Ui.strokeBg(Color.WHITE,Color.rgb(234,235,241),1,22,this)); return c; }

    private void build(){
        LinearLayout outer=new LinearLayout(this); outer.setOrientation(LinearLayout.VERTICAL); outer.setBackgroundColor(Ui.BG); outer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        ScrollView sv=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),Ui.dp(this,100)); sv.addView(root); outer.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(Ui.dp(this,20),Ui.dp(this,18),Ui.dp(this,20),Ui.dp(this,18)); GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Ui.PURPLE,Color.rgb(139,125,255)}); gd.setCornerRadius(Ui.dp(this,28)); hero.setBackground(gd);
        TextView title=Ui.text(this,"دست بی‌قرار",26,Color.WHITE,true); hero.addView(title); TextView tag=Ui.text(this,"کمتر باز کن، بیشتر تمرکز کن",13,Color.argb(225,255,255,255),false); hero.addView(tag); TextView desc=Ui.text(this,"برنامه‌هایی که بی‌اختیار سراغشان می‌روی را برای مدتی قفل کن و تمرکزت را پس بگیر.",12,Color.argb(235,255,255,255),false); desc.setPadding(0,Ui.dp(this,8),0,0); hero.addView(desc); root.addView(hero,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout apps=card(); apps.addView(cardTitle("برنامه‌های مزاحم")); apps.addView(sub("از بین برنامه‌های نصب‌شده روی گوشی انتخاب کن.")); Button pick=new Button(this); pick.setText("▦   انتخاب برنامه‌ها"); pick.setTextSize(14); pick.setAllCaps(false); pick.setGravity(Gravity.CENTER); pick.setBackground(Ui.strokeBg(Color.rgb(250,249,255),Color.rgb(207,202,252),1,18,this)); pick.setOnClickListener(v->startActivity(new Intent(this,AppSelectionActivity.class))); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,Ui.dp(this,62)); pp.setMargins(0,Ui.dp(this,12),0,0); apps.addView(pick,pp);
        LinearLayout srow=new LinearLayout(this); srow.setGravity(Gravity.CENTER_VERTICAL); srow.setOrientation(LinearLayout.HORIZONTAL); summary=sub("هنوز برنامه‌ای انتخاب نشده"); srow.addView(summary,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1)); count=Ui.text(this,"۰ برنامه",11,Ui.PURPLE,true); count.setGravity(Gravity.CENTER); count.setBackground(Ui.bg(Ui.SOFT,50,this)); srow.addView(count,new LinearLayout.LayoutParams(Ui.dp(this,78),Ui.dp(this,32))); apps.addView(srow); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,Ui.dp(this,14),0,0); root.addView(apps,cp);

        LinearLayout time=card(); time.addView(cardTitle("مدت قفل")); time.addView(sub("زمان دلخواهت را وارد کن.")); LinearLayout fields=new LinearLayout(this); fields.setOrientation(LinearLayout.HORIZONTAL); fields.setWeightSum(2);
        hours=timeField("ساعت","1"); minutes=timeField("دقیقه","0"); fields.addView(wrapField("ساعت",hours),new LinearLayout.LayoutParams(0,Ui.dp(this,86),1)); LinearLayout.LayoutParams mf=new LinearLayout.LayoutParams(0,Ui.dp(this,86),1); mf.setMarginStart(Ui.dp(this,10)); fields.addView(wrapField("دقیقه",minutes),mf); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2); fp.setMargins(0,Ui.dp(this,12),0,0); time.addView(fields,fp);
        LinearLayout presets=new LinearLayout(this); presets.setOrientation(LinearLayout.HORIZONTAL); String[] labs={"۲۵ دقیقه","۴۵ دقیقه","۱ ساعت","۲ ساعت"}; int[][] vals={{0,25},{0,45},{1,0},{2,0}}; for(int i=0;i<labs.length;i++){ Button b=new Button(this); b.setText(labs[i]); b.setTextSize(10); b.setAllCaps(false); b.setBackground(Ui.bg(Color.rgb(243,244,248),50,this)); final int hh=vals[i][0],mm=vals[i][1]; b.setOnClickListener(v->{hours.setText(String.valueOf(hh));minutes.setText(String.valueOf(mm));}); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,Ui.dp(this,42),1); if(i>0) bp.setMarginStart(Ui.dp(this,5)); presets.addView(b,bp); } LinearLayout.LayoutParams prp=new LinearLayout.LayoutParams(-1,-2); prp.setMargins(0,Ui.dp(this,10),0,0); time.addView(presets,prp); LinearLayout.LayoutParams tcp=new LinearLayout.LayoutParams(-1,-2); tcp.setMargins(0,Ui.dp(this,13),0,0); root.addView(time,tcp);

        LinearLayout hard=card(); LinearLayout hr=new LinearLayout(this); hr.setGravity(Gravity.CENTER_VERTICAL); hr.setOrientation(LinearLayout.HORIZONTAL); LinearLayout ht=new LinearLayout(this); ht.setOrientation(LinearLayout.VERTICAL); ht.addView(cardTitle("قفل سخت")); ht.addView(sub("بعد از شروع، تا پایان زمان از داخل برنامه لغو نمی‌شود.")); hr.addView(ht,new LinearLayout.LayoutParams(0,-2,1)); Switch sw=new Switch(this); sw.setChecked(true); sw.setEnabled(false); hr.addView(sw); hard.addView(hr); LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2); hp.setMargins(0,Ui.dp(this,13),0,0); root.addView(hard,hp);

        timerState=Ui.text(this,"",13,Ui.PURPLE,true); timerState.setGravity(Gravity.CENTER); timerState.setPadding(0,Ui.dp(this,14),0,0); root.addView(timerState);

        Button start=new Button(this); start.setText("شروع تمرکز"); start.setTextColor(Color.WHITE); start.setTextSize(16); start.setTypeface(null,1); start.setAllCaps(false); start.setBackground(Ui.bg(Ui.PURPLE,18,this)); start.setOnClickListener(v->startFocus()); LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,Ui.dp(this,58)); stp.setMargins(Ui.dp(this,18),Ui.dp(this,10),Ui.dp(this,18),Ui.dp(this,18)); outer.addView(start,stp);
        setContentView(outer);
    }

    private EditText timeField(String label,String value){ EditText e=new EditText(this); e.setText(value); e.setTextSize(28); e.setTextColor(Ui.TEXT); e.setGravity(Gravity.CENTER); e.setInputType(2); e.setSelectAllOnFocus(true); e.setBackgroundColor(Color.TRANSPARENT); return e; }
    private LinearLayout wrapField(String label,EditText e){ LinearLayout w=new LinearLayout(this); w.setOrientation(LinearLayout.VERTICAL); w.setPadding(Ui.dp(this,10),Ui.dp(this,7),Ui.dp(this,10),Ui.dp(this,5)); w.setBackground(Ui.strokeBg(Color.rgb(250,251,254),Color.rgb(232,234,240),1,18,this)); TextView l=Ui.text(this,label,11,Ui.MUTED,false); l.setGravity(Gravity.CENTER); w.addView(l); w.addView(e,new LinearLayout.LayoutParams(-1,0,1)); return w; }

    private void refresh(){
        Set<String> s=Prefs.getSelected(this); count.setText(s.size()+" برنامه"); summary.setText(s.isEmpty()?"هنوز برنامه‌ای انتخاب نشده":s.size()+" برنامه انتخاب شده");
        if(Prefs.active(this)){ long left=Math.max(0,Prefs.end(this)-System.currentTimeMillis()); long sec=left/1000; long hh=sec/3600,mm=(sec%3600)/60,ss=sec%60; timerState.setText(String.format(Locale.US,"قفل فعال • %02d:%02d:%02d",hh,mm,ss)); } else timerState.setText("");
    }

    private boolean accessibilityEnabled(){
        AccessibilityManager am=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if(am==null) return false;
        for(AccessibilityServiceInfo i:am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            if(i.getResolveInfo()!=null && i.getResolveInfo().serviceInfo!=null && getPackageName().equals(i.getResolveInfo().serviceInfo.packageName)) return true;
        }
        return false;
    }

    private void startFocus(){
        Set<String> s=Prefs.getSelected(this); if(s.isEmpty()){ Toast.makeText(this,"اول حداقل یک برنامه را انتخاب کن.",Toast.LENGTH_SHORT).show(); return; }
        int hh=0,mm=0; try{hh=Integer.parseInt(hours.getText().toString());}catch(Exception ignored){} try{mm=Integer.parseInt(minutes.getText().toString());}catch(Exception ignored){}
        long dur=(hh*60L+mm)*60_000L; if(dur<=0){Toast.makeText(this,"زمان قفل را وارد کن.",Toast.LENGTH_SHORT).show();return;}
        if(!accessibilityEnabled()){
            new AlertDialog.Builder(this).setTitle("فعال‌کردن دسترسی").setMessage("برای قفل‌کردن برنامه‌ها، دسترسی Accessibility «دست بی‌قرار» را یک‌بار فعال کن.").setPositiveButton("باز کردن تنظیمات",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("فعلاً نه",null).show(); return;
        }
        Prefs.start(this,System.currentTimeMillis()+dur); Toast.makeText(this,"حالت تمرکز شروع شد.",Toast.LENGTH_SHORT).show(); refresh();
    }
}
