package fr.feepin.go4lunch.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.fragment.app.DialogFragment;

import fr.feepin.go4lunch.R;

public class PermissionUtils {

    public static boolean isPermissionGranted(String[] permissions, int[] grantResults, String askedPermission) {
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int result = grantResults[i];

            if (permission.equals(askedPermission)) {
                return result == PackageManager.PERMISSION_GRANTED;
            }
        }

        return false;
    }

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
}
