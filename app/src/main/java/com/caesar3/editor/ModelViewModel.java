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

    private final MutableLiveData<ModelData> modelDataLive = new MutableLiveData<>();
    private final MutableLiveData<String>    errorLive     = new MutableLiveData<>();
    private final MutableLiveData<Boolean>   savedLive     = new MutableLiveData<>();

    private Uri openUri;

    public ModelViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ModelData> getModelData() { return modelDataLive; }
    public LiveData<String>    getError()     { return errorLive; }
    public LiveData<Boolean>   getSaved()     { return savedLive; }
    public Uri                 getOpenUri()   { return openUri; }

    public void loadFile(Uri uri) {
        openUri = uri;
        ContentResolver cr = getApplication().getContentResolver();
        new Thread(() -> {
            try (InputStream is = cr.openInputStream(uri)) {
                if (is == null) {
                    errorLive.postValue("Cannot open file");
                    return;
                }
                ModelData data = ModelParser.parse(is);
                modelDataLive.postValue(data);
            } catch (IOException e) {
                errorLive.postValue("Load error: " + e.getMessage());
            }
        }).start();
    }

    public void saveFile() {
        if (openUri == null) { errorLive.postValue("No file open"); return; }
        saveFileTo(openUri);
    }

    public void saveFileTo(Uri uri) {
        ModelData data = modelDataLive.getValue();
        if (data == null) { errorLive.postValue("No data to save"); return; }

        ContentResolver cr = getApplication().getContentResolver();
        new Thread(() -> {
            // "wt" truncates the file before writing — required for SAF URIs
            try (OutputStream os = cr.openOutputStream(uri, "wt");
                 Writer writer   = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                if (os == null) { errorLive.postValue("Cannot open output stream"); return; }
                writer.write(data.serialize());
                writer.flush();
                openUri = uri;
                savedLive.postValue(true);
            } catch (IOException e) {
                errorLive.postValue("Save error: " + e.getMessage());
            }
        }).start();
    }
}
