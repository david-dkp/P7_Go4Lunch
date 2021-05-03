package fr.feepin.go4lunch.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import fr.feepin.go4lunch.R;

public class PermissionUtils {

    public static void showRationalDialog(Context context, int rationalMessageId, DialogInterface.OnClickListener retryButtonListener) {
        new AlertDialog.Builder(context)
                .setPositiveButton(R.string.retry, retryButtonListener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setTitle(R.string.rational_title)
                .setMessage(rationalMessageId)
                .create()
                .show();
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
