package com.caesar3.editor;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.caesar3.editor.data.ModelData;
import com.caesar3.editor.parser.ModelParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class ModelViewModel extends AndroidViewModel {

    private final MutableLiveData<ModelData> modelDataLive  = new MutableLiveData<>();
    private final MutableLiveData<String>    errorLive      = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   savedLive      = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   backupDoneLive = new MutableLiveData<>();

    private Uri openUri;

    public ModelViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ModelData> getModelData()  { return modelDataLive; }
    public LiveData<String>    getError()      { return errorLive; }
    public LiveData<Boolean>   getSaved()      { return savedLive; }
    public LiveData<Boolean>   getBackupDone() { return backupDoneLive; }
    public Uri                 getOpenUri()    { return openUri; }

    // ── Load ─────────────────────────────────────────────────────────────────

    public void loadFile(Uri uri) {
        openUri = uri;
        ContentResolver cr = getApplication().getContentResolver();
        new Thread(() -> {
            try (InputStream is = cr.openInputStream(uri)) {
                if (is == null) { errorLive.postValue("Cannot open file"); return; }
                modelDataLive.postValue(ModelParser.parse(is));
            } catch (IOException e) {
                errorLive.postValue("Load error: " + e.getMessage());
            }
        }).start();
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    public void saveFile() {
        if (openUri == null) { errorLive.postValue("No file open"); return; }
        saveFileTo(openUri);
    }

    public void saveFileTo(Uri uri) {
        ModelData data = modelDataLive.getValue();
        if (data == null) { errorLive.postValue("No data to save"); return; }

        ContentResolver cr = getApplication().getContentResolver();
        new Thread(() -> {
            try {
                // "wt" truncates before writing — required for SAF URIs
                OutputStream os = cr.openOutputStream(uri, "wt");
                if (os == null) { errorLive.postValue("Cannot open output stream"); return; }
                try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    w.write(data.serialize());
                    w.flush();
                }
                openUri = uri;
                savedLive.postValue(true);
            } catch (IOException e) {
                errorLive.postValue("Save error: " + e.getMessage());
            }
        }).start();
    }

    // ── Backup ───────────────────────────────────────────────────────────────

    /**
     * Byte-copies the currently open source file to destUri.
     * Called before any edits are saved, so the source still holds
     * the original content.
     */
    public void backupFile(Uri destUri) {
        if (openUri == null) { errorLive.postValue("No file open for backup"); return; }
        Uri source = openUri;   // capture on calling thread
        ContentResolver cr = getApplication().getContentResolver();
        new Thread(() -> {
            try (InputStream  in  = cr.openInputStream(source);
                 OutputStream out = cr.openOutputStream(destUri)) {
                if (in == null || out == null) {
                    errorLive.postValue("Backup failed: could not open streams");
                    return;
                }
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                out.flush();
                backupDoneLive.postValue(true);
            } catch (IOException e) {
                errorLive.postValue("Backup error: " + e.getMessage());
            }
        }).start();
    }
}
