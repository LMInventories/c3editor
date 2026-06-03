package com.caesar3.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.caesar3.editor.ui.PagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ModelViewModel viewModel;
    private ViewPager2 viewPager;
    private TextView   tvEmpty;

    private final ActivityResultLauncher<String[]> openLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) return;
                        // Keep persistent read+write permission so save works after reboot
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException ignored) {}
                        viewModel.loadFile(uri);
                    });

    private final ActivityResultLauncher<String> saveAsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"),
                    uri -> { if (uri != null) viewModel.saveFileTo(uri); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        tvEmpty   = findViewById(R.id.tv_empty);

        viewPager.setAdapter(new PagerAdapter(this));

        TabLayout tabs = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabs, viewPager, (tab, pos) ->
                tab.setText(pos == 0 ? R.string.tab_buildings : R.string.tab_houses)
        ).attach();

        viewModel = new ViewModelProvider(this).get(ModelViewModel.class);

        viewModel.getModelData().observe(this, data -> {
            if (data == null) return;
            tvEmpty.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.msg_file_loaded, Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });

        viewModel.getSaved().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok))
                Toast.makeText(this, R.string.msg_save_ok, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open) {
            openLauncher.launch(new String[]{"text/*", "*/*"});
            return true;
        } else if (id == R.id.action_save) {
            if (viewModel.getOpenUri() == null) {
                Toast.makeText(this, R.string.hint_no_file, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.saveFile();
            }
            return true;
        } else if (id == R.id.action_save_as) {
            saveAsLauncher.launch("c3_model.txt");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
