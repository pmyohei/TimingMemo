package com.example.timingmemo;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.timingmemo.db.async.AsyncTmpCreateDataMemo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.timingmemo.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //---------------------------
        // BottomNavigation設定
        //---------------------------
        /*BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_memo, R.id.navigation_record, R.id.navigation_history)
                .build();*/
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 下部ナビアイコン再選択時の動作
        binding.navView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // 実装なし（再選択されてもフラグメントの生成はしないようにするため）
            }
        });

        // アクションバーを非表示
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //---------------
        // 疑似データ生成
        //---------------
        AsyncTmpCreateDataMemo db = new AsyncTmpCreateDataMemo(this, new AsyncTmpCreateDataMemo.OnFinishListener() {
            @Override
            public void onFinish(int pid) {
            }
        });
        //非同期処理開始
        db.execute();
    }

}
