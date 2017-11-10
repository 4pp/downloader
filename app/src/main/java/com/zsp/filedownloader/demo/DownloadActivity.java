package com.zsp.filedownloader.demo;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zsp.filedownloader.DownLoader;
import com.zsp.filedownloader.R;


public class DownloadActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener{
    private static final String TAG = "DownloadActivity";
    
    ViewPager mViewPager;
    FragmentAdapter fragmentAdapter;
    PagerTabStrip tabbar;

    String[] tabName = {"未完成","已完成"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView(savedInstanceState);
    }

    protected int getLayoutId() {
        return R.layout.activity_download;
    }

    protected void initView(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("下载管理");
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        tabbar = (PagerTabStrip)findViewById(R.id.tabbar);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);

        FragmentManager fm =  getSupportFragmentManager();
        fragmentAdapter =  new FragmentAdapter(fm);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(fragmentAdapter);
//        tabbar.setViewPager(mViewPager);
//        tabbar.setCurrentTab(0);

//        tabbar.setIndicatorColor(getResources().getColor(R.color.colorAccent));
//        tabbar.setTextSelectColor(getResources().getColor(R.color.white));
//        tabbar.setTextUnselectColor(getResources().getColor(R.color.white));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                Log.d(TAG, "onMenuItemClick: 添加任务");
                DownLoader.getInstance().add("http://res9.d.cn/android/yxzx.apk","yxzx");
                break;
        }
        return false;
    }

    class FragmentAdapter extends FragmentPagerAdapter {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabName[position];
        }

        @Override
        public Fragment getItem(int position) {
            return DownloadFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return tabName.length;
        }
    }
}
