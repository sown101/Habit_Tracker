package com.example.habittracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Constructor rỗng bắt buộc phải có đối với Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Lệnh này sẽ nạp file giao diện fragment_home.xml vào lớp Java này
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}