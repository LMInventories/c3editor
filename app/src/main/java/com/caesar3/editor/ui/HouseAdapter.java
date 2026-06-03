package com.caesar3.editor.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.caesar3.editor.R;
import com.caesar3.editor.data.HouseEntry;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.VH> {

    private List<HouseEntry> items = new ArrayList<>();

    public void submitList(List<HouseEntry> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_house, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) { h.bind(items.get(position)); }

    @Override
    public int getItemCount() { return items.size(); }

    // -------------------------------------------------------------------------

    static class VH extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final EditText etDevolve, etEvolve;
        private TextWatcher wDevolve, wEvolve;

        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tv_house_name);
            etDevolve = v.findViewById(R.id.et_devolve);
            etEvolve  = v.findViewById(R.id.et_evolve);
        }

        void bind(HouseEntry e) {
            // Remove old watchers before setting text
            if (wDevolve != null) etDevolve.removeTextChangedListener(wDevolve);
            if (wEvolve  != null) etEvolve .removeTextChangedListener(wEvolve);
            wDevolve = wEvolve = null;

            tvName.setText(e.name);
            etDevolve.setText(String.valueOf(e.getDevolveLevel()));
            etEvolve .setText(String.valueOf(e.getEvolveLevel()));

            wDevolve = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try { e.setDevolveLevel(Integer.parseInt(s.toString().trim())); }
                    catch (NumberFormatException ignored) {}
                }
            };

            wEvolve = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try { e.setEvolveLevel(Integer.parseInt(s.toString().trim())); }
                    catch (NumberFormatException ignored) {}
                }
            };

            etDevolve.addTextChangedListener(wDevolve);
            etEvolve .addTextChangedListener(wEvolve);
        }
    }
}
