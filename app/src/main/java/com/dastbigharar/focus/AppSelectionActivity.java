package com.dastbigharar.focus;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class AppSelectionActivity extends Activity {
    private final List<AppInfoItem> all = new ArrayList<>();
    private final Set<String> selected = new HashSet<>();
    private LinearLayout list;
    private TextView count;
    private EditText search;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        selected.addAll(Prefs.getSelected(this));
        loadApps();
        build();
    }

    private void loadApps(){
        PackageManager pm=getPackageManager();
        Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo r:pm.queryIntentActivities(i,0)){
            String pkg=r.activityInfo.packageName;
            if(pkg.equals(getPackageName())) continue;
            String label=r.loadLabel(pm).toString();
            boolean exists=false;
            for(AppInfoItem x:all) if(x.packageName.equals(pkg)){ exists=true; break; }
            if(!exists) all.add(new AppInfoItem(label,pkg));
        }
        Collections.sort(all,(a,b)->a.label.compareToIgnoreCase(b.label));
    }

    private void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),Ui.dp(this,16)); root.setBackgroundColor(Ui.BG); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setOrientation(LinearLayout.HORIZONTAL);
        Button back=new Button(this); back.setText("→"); back.setTextSize(19); back.setBackground(Ui.bg(Color.rgb(242,243,247),13,this)); back.setOnClickListener(v->finishWithSave()); top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,44)));
        TextView title=Ui.text(this,"انتخاب برنامه‌ها",18,Ui.TEXT,true); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,Ui.dp(this,50),1); tp.setMarginStart(Ui.dp(this,10)); top.addView(title,tp);
        count=Ui.text(this,"",12,Ui.PURPLE,true); count.setGravity(Gravity.CENTER); count.setBackground(Ui.bg(Ui.SOFT,50,this)); top.addView(count,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,34)));
        root.addView(top);

        search=new EditText(this); search.setHint("جستجوی برنامه..."); search.setSingleLine(true); search.setTextSize(14); search.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0); search.setBackground(Ui.strokeBg(Color.WHITE,Color.rgb(230,232,239),1,16,this)); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,Ui.dp(this,50)); sp.setMargins(0,Ui.dp(this,12),0,Ui.dp(this,12)); root.addView(search,sp);
        search.addTextChangedListener(new TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ render(); } public void afterTextChanged(Editable e){} });

        ScrollView sv=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sv.addView(list,new ScrollView.LayoutParams(-1,-2)); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        Button done=new Button(this); done.setText("تأیید انتخاب‌ها"); done.setTextColor(Color.WHITE); done.setTextSize(15); done.setTypeface(null,1); done.setAllCaps(false); done.setBackground(Ui.bg(Ui.PURPLE,18,this)); done.setOnClickListener(v->finishWithSave()); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,Ui.dp(this,56)); dp.setMargins(0,Ui.dp(this,12),0,0); root.addView(done,dp);
        setContentView(root); render();
    }

    private void render(){
        list.removeAllViews(); String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.ROOT);
        for(AppInfoItem item:all){
            if(!item.label.toLowerCase(Locale.ROOT).contains(q)) continue;
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(Ui.dp(this,12),Ui.dp(this,10),Ui.dp(this,12),Ui.dp(this,10)); row.setBackground(Ui.strokeBg(Color.WHITE, selected.contains(item.packageName)?Color.rgb(188,181,255):Color.rgb(232,234,240),1,17,this));
            CheckBox cb=new CheckBox(this); cb.setChecked(selected.contains(item.packageName)); cb.setClickable(false); row.addView(cb,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
            LinearLayout texts=new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL); TextView name=Ui.text(this,item.label,14,Ui.TEXT,true); TextView pkg=Ui.text(this,item.packageName,10,Ui.MUTED,false); texts.addView(name); texts.addView(pkg); LinearLayout.LayoutParams tl=new LinearLayout.LayoutParams(0,-2,1); tl.setMarginStart(Ui.dp(this,8)); row.addView(texts,tl);
            row.setOnClickListener(v->{ if(selected.contains(item.packageName)) selected.remove(item.packageName); else selected.add(item.packageName); render(); updateCount(); });
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.setMargins(0,0,0,Ui.dp(this,8)); list.addView(row,rp);
        }
        updateCount();
    }

    private void updateCount(){ if(count!=null) count.setText(String.valueOf(selected.size())); }
    private void finishWithSave(){ Prefs.setSelected(this,selected); setResult(RESULT_OK); finish(); }
    @Override public void onBackPressed(){ finishWithSave(); }
}
