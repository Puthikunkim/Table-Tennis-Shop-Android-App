package com.example.app.Util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;

/**
 * Utility class to display custom styled Toast messages.
 * This class provides a method to show a Toast with a custom layout,
 * allowing for more control over the appearance of the notification compared
 * to the default Android Toast.
 */
public class ToastUtils {
    public static void showCustomToast(Context context, String message) {
        View layout = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}