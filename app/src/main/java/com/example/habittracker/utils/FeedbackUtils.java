package com.example.habittracker.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public final class FeedbackUtils {

    private FeedbackUtils() {
    }

    public static void performCompleteFeedback(Context context) {
        playTone(880, 90);
        vibrate(context, 35);
    }

    public static void performPlusFeedback(Context context) {
        playTone(660, 60);
        vibrate(context, 20);
    }

    public static void performResetFeedback(Context context) {
        playTone(440, 80);
        vibrate(context, 28);
    }

    private static void playTone(int toneType, int durationMs) {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
            toneGenerator.startTone(toneType, durationMs);

            new Thread(() -> {
                try {
                    Thread.sleep(durationMs + 40L);
                } catch (InterruptedException ignored) {
                }
                toneGenerator.release();
            }).start();
        } catch (Exception ignored) {
        }
    }

    private static void vibrate(Context context, int durationMs) {
        try {
            Vibrator vibrator;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager =
                        (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager == null) return;
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator == null || !vibrator.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                );
            } else {
                vibrator.vibrate(durationMs);
            }
        } catch (Exception ignored) {
        }
    }
}