package com.example.habittracker.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;
import com.example.habittracker.ui.auth.AuthActivity;
import com.example.habittracker.utils.SessionManager;
import com.example.habittracker.utils.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchNotificationSetting;
    private SwitchMaterial switchShakeSetting;
    private View btnLogout;

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
        switchShakeSetting = view.findViewById(R.id.switchShakeSetting);
        btnLogout = view.findViewById(R.id.btnLogout);

        switchNotificationSetting.setChecked(SettingsManager.isNotificationEnabled(requireContext()));
        switchShakeSetting.setChecked(SettingsManager.isShakeEnabled(requireContext()));

        switchNotificationSetting.setOnCheckedChangeListener((buttonView, isChecked) ->
                SettingsManager.setNotificationEnabled(requireContext(), isChecked)
        );

        switchShakeSetting.setOnCheckedChangeListener((buttonView, isChecked) ->
                SettingsManager.setShakeEnabled(requireContext(), isChecked)
        );

        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(requireContext());

            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}