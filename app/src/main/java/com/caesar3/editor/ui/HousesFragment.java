package com.caesar3.editor.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.caesar3.editor.ModelViewModel;
import com.caesar3.editor.R;

public class HousesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_houses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.recycler_houses);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        HouseAdapter adapter = new HouseAdapter();
        recycler.setAdapter(adapter);

        new ViewModelProvider(requireActivity())
                .get(ModelViewModel.class)
                .getModelData()
                .observe(getViewLifecycleOwner(), data -> {
                    if (data != null) adapter.submitList(data.getHouses());
                });
    }
}
