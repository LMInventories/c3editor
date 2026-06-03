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
import com.caesar3.editor.data.BuildingEntry;

import java.util.ArrayList;
import java.util.List;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.VH> {

    private List<BuildingEntry> items = new ArrayList<>();

    public void submitList(List<BuildingEntry> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_building, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    // -------------------------------------------------------------------------

    static class VH extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final EditText etCost, etDes, etStep, etSize, etRange, etEmp;

        // One watcher reference per field; removed before each rebind to prevent
        // cross-item corruption when the RecyclerView recycles views.
        private TextWatcher wCost, wDes, wStep, wSize, wRange, wEmp;

        VH(@NonNull View v) {
            super(v);
            tvName  = v.findViewById(R.id.tv_building_name);
            etCost  = v.findViewById(R.id.et_cost);
            etDes   = v.findViewById(R.id.et_desirability);
            etStep  = v.findViewById(R.id.et_des_step);
            etSize  = v.findViewById(R.id.et_des_step_size);
            etRange = v.findViewById(R.id.et_des_range);
            etEmp   = v.findViewById(R.id.et_employees);
        }

        void bind(BuildingEntry e) {
            // Step 1: detach old watchers
            removeAll();

            // Step 2: populate
            tvName.setText(e.name);
            if (e.isNothing()) {
                tvName.setTextColor(0xFFB0A090);
                itemView.setAlpha(0.6f);
            } else {
                tvName.setTextColor(0xFF8B3A1D);
                itemView.setAlpha(1.0f);
            }
            etCost .setText(String.valueOf(e.getCost()));
            etDes  .setText(String.valueOf(e.getDesirability()));
            etStep .setText(String.valueOf(e.getDesStep()));
            etSize .setText(String.valueOf(e.getDesStepSize()));
            etRange.setText(String.valueOf(e.getDesRange()));
            etEmp  .setText(String.valueOf(e.getEmployees()));

            // Step 3: attach fresh watchers
            wCost  = w(s -> e.setCost(s));
            wDes   = w(s -> e.setDesirability(s));
            wStep  = w(s -> e.setDesStep(s));
            wSize  = w(s -> e.setDesStepSize(s));
            wRange = w(s -> e.setDesRange(s));
            wEmp   = w(s -> e.setEmployees(s));

            etCost .addTextChangedListener(wCost);
            etDes  .addTextChangedListener(wDes);
            etStep .addTextChangedListener(wStep);
            etSize .addTextChangedListener(wSize);
            etRange.addTextChangedListener(wRange);
            etEmp  .addTextChangedListener(wEmp);
        }

        private void removeAll() {
            if (wCost  != null) etCost .removeTextChangedListener(wCost);
            if (wDes   != null) etDes  .removeTextChangedListener(wDes);
            if (wStep  != null) etStep .removeTextChangedListener(wStep);
            if (wSize  != null) etSize .removeTextChangedListener(wSize);
            if (wRange != null) etRange.removeTextChangedListener(wRange);
            if (wEmp   != null) etEmp  .removeTextChangedListener(wEmp);
            wCost = wDes = wStep = wSize = wRange = wEmp = null;
        }

        interface IntSetter { void set(int v); }

        private static TextWatcher w(IntSetter setter) {
            return new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try { setter.set(Integer.parseInt(s.toString().trim())); }
                    catch (NumberFormatException ignored) {}
                }
            };
        }
    }
}
