package com.caesar3.editor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.caesar3.editor.ui.PagerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ModelViewModel viewModel;
    private ViewPager2     viewPager;
    private View           svEmpty;    // ScrollView welcome state — only setVisibility() called

    /**
     * Tracks whether the backup dialog has already been shown for the current file load.
     * Reset in openLauncher (a genuine new file) NOT in the modelDataLive observer,
     * so config-changes (rotation) do not re-show the dialog.
     */
    private boolean backupDialogShown = false;

    // ── SAF launchers ────────────────────────────────────────────────────────
    // All three must be registered before onStart (field initializers run before onCreate).

    private final ActivityResultLauncher<String[]> openLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) return;
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException ignored) {}
                        backupDialogShown = false;  // new file → allow dialog once
                        viewModel.loadFile(uri);
                    });

    private final ActivityResultLauncher<String> saveAsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"),
                    uri -> { if (uri != null) viewModel.saveFileTo(uri); });

    private final ActivityResultLauncher<String> backupLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"),
                    uri -> {
                        if (uri != null) {
                            viewModel.backupFile(uri);
                            // editing view revealed in backupDoneLive observer after copy finishes
                        } else {
                            // user cancelled the file picker → treat as Skip
                            showEditingView();
                        }
                    });

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        svEmpty   = findViewById(R.id.tv_empty);   // ScrollView, id kept as tv_empty

        viewPager.setAdapter(new PagerAdapter(this));

        TabLayout tabs = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabs, viewPager, (tab, pos) ->
                tab.setText(pos == 0 ? R.string.tab_buildings : R.string.tab_houses)
        ).attach();

        viewModel = new ViewModelProvider(this).get(ModelViewModel.class);

        // If the ViewModel already holds data (rotation / config change), suppress the backup
        // dialog before registering the observer — LiveData re-delivers on onStart, so the flag
        // must be set synchronously here, before the Activity reaches the STARTED state.
        if (viewModel.getModelData().getValue() != null) {
            backupDialogShown = true;
        }

        // File loaded → show backup dialog (once per file; flag guards against re-show on rotation)
        viewModel.getModelData().observe(this, data -> {
            if (data == null) return;
            Toast.makeText(this, R.string.msg_file_loaded, Toast.LENGTH_SHORT).show();
            maybeShowBackupDialog();
        });

        viewModel.getError().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });

        viewModel.getSaved().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok))
                Toast.makeText(this, R.string.msg_save_ok, Toast.LENGTH_SHORT).show();
        });

        // Backup completed → toast + reveal editing view
        viewModel.getBackupDone().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(this, R.string.msg_backup_ok, Toast.LENGTH_SHORT).show();
                showEditingView();
            }
        });

        // Restore editing view after a config change (ViewModel survived; dialog already handled)
        if (viewModel.getModelData().getValue() != null) {
            showEditingView();
        }
    }

    // ── Dialog & view helpers ─────────────────────────────────────────────────

    private void maybeShowBackupDialog() {
        if (backupDialogShown) return;
        backupDialogShown = true;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_backup_title)
                .setMessage(R.string.dialog_backup_message)
                .setPositiveButton(R.string.dialog_backup_create, (d, w) ->
                        backupLauncher.launch("c3_model_backup.txt"))
                .setNegativeButton(R.string.dialog_backup_skip, (d, w) ->
                        showEditingView())
                // Back-press / outside tap treated the same as Skip
                .setOnCancelListener(d -> showEditingView())
                .show();
    }

    /** Hides the welcome ScrollView and reveals the editing ViewPager. */
    private void showEditingView() {
        svEmpty.setVisibility(View.GONE);
        viewPager.setVisibility(View.VISIBLE);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

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
