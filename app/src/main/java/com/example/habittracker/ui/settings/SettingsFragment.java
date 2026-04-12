package com.example.habittracker.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;
import com.example.habittracker.utils.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotificationSetting;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchNotificationSetting = view.findViewById(R.id.switchNotificationSetting);

        switchNotificationSetting.setChecked(SettingsManager.isNotificationEnabled(requireContext()));

        switchNotificationSetting.setOnCheckedChangeListener((buttonView, isChecked) ->
                SettingsManager.setNotificationEnabled(requireContext(), isChecked)
        );
    }
}