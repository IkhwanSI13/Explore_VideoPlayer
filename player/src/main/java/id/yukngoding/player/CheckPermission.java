package id.yukngoding.player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class CheckPermission {
    final public static String CAMERA = "camera";
    final public static String NETWORK = "network";
    final public static String WRITE_EXTERNAL_STORAGE = "Write External Storage";
    final public static String READ_EXTERNAL_STORAGE = "Read External Storage";
    final public static String READ_PHONE_STATE = "Read Phone State";
    final public static String LOCATION = "Location";
    final public static String VIBRATE = "Vibrate";
    final public static String AUDIO = "Audio";
    //    final public static String SMS = "SMS";
    final public static String FOREGROUND = "Foreground";

    final public static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final public static int REQUEST_CODE_ASK_CAMERA = 125;
    final public static int REQUEST_CODE_ASK_NETWORK = 126;
    final public static int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 127;
    final public static int REQUEST_CODE_ASK_READ_EXTERNAL_STORAGE = 128;
    final public static int REQUEST_CODE_ASK_READ_PHONE_STATE = 129;
    final public static int REQUEST_CODE_ASK_LOCATION = 130;
    final public static int REQUEST_CODE_ASK_VIBRATE = 131;
    final public static int REQUEST_CODE_ASK_AUDIO = 132;
    //    final public static int REQUEST_CODE_ASK_SMS = 133;
    final public static int REQUEST_CODE_ASK_FOREGROUND = 134;

    private Context context;
    final public static List<String> permissionsNeeded = new ArrayList<>();

    public CheckPermission(Context context) {
        this.context = context;
    }

    public boolean checkMultiple(List requestList) {
        final List<String> permissionsList = new ArrayList<>();

        if (requestList.contains(NETWORK) && !addPermission(context, permissionsList, Manifest.permission.ACCESS_NETWORK_STATE))
            permissionsNeeded.add("Network");

        if (requestList.contains(LOCATION)) {
            if (!addPermission(context, permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("Location");

            if (!addPermission(context, permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                permissionsNeeded.add("Location");
        }

        if (requestList.contains(CAMERA)) {
            if (!addPermission(context, permissionsList, Manifest.permission.CAMERA))
                permissionsNeeded.add("Camera");
        }

        if (requestList.contains(WRITE_EXTERNAL_STORAGE)) {
            if (!addPermission(context, permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                permissionsNeeded.add("Write External Storage");
        }

        if (requestList.contains(READ_EXTERNAL_STORAGE)) {
            if (!addPermission(context, permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                permissionsNeeded.add("Read External Storage");
        }

        if (requestList.contains(READ_PHONE_STATE)) {
            if (!addPermission(context, permissionsList, Manifest.permission.READ_PHONE_STATE))
                permissionsNeeded.add("Read Phone State");
        }

        if (requestList.contains(VIBRATE)) {
            if (!addPermission(context, permissionsList, Manifest.permission.VIBRATE))
                permissionsNeeded.add("Vibrate");
        }

        if (requestList.contains(AUDIO)) {
            if (!addPermission(context, permissionsList, Manifest.permission.RECORD_AUDIO))
                permissionsNeeded.add("AUDIO");
        }
        if (requestList.contains(FOREGROUND)) {
            if (!addPermission(context, permissionsList, Manifest.permission.FOREGROUND_SERVICE))
                permissionsNeeded.add("FOREGROUND SERVICE");
        }
//        if (requestList.contains(SMS)) {
//            if (!addPermission(context, permissionsList, Manifest.permission.RECEIVE_SMS)) {
//                permissionsNeeded.add("SMS");
//            }
//            if (!addPermission(context, permissionsList, Manifest.permission.SEND_SMS)) {
//                permissionsNeeded.add("SMS");
//            }
//        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                ActivityCompat.requestPermissions((Activity) context, permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            ActivityCompat.requestPermissions((Activity) context, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        } else {
            return true;
        }


    }

    public boolean checkById(int checkPermissionId) {
        final List<String> permissionsList = new ArrayList<>();

        if (checkPermissionId == REQUEST_CODE_ASK_NETWORK && !addPermission(context, permissionsList, Manifest.permission.ACCESS_NETWORK_STATE))
            permissionsNeeded.add("Network");

        if (checkPermissionId == REQUEST_CODE_ASK_LOCATION) {
            if (!addPermission(context, permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("Location");

            if (!addPermission(context, permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                permissionsNeeded.add("Location");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_CAMERA) {
            if (!addPermission(context, permissionsList, Manifest.permission.CAMERA))
                permissionsNeeded.add("Camera");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE) {
            if (!addPermission(context, permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                permissionsNeeded.add("Write External Storage");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_READ_EXTERNAL_STORAGE) {
            if (!addPermission(context, permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                permissionsNeeded.add("Read External Storage");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_READ_PHONE_STATE) {
            if (!addPermission(context, permissionsList, Manifest.permission.READ_PHONE_STATE))
                permissionsNeeded.add("Read Phone State");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_VIBRATE) {
            if (!addPermission(context, permissionsList, Manifest.permission.VIBRATE))
                permissionsNeeded.add("Vibrate");
        }

        if (checkPermissionId == REQUEST_CODE_ASK_AUDIO) {
            if (!addPermission(context, permissionsList, Manifest.permission.RECORD_AUDIO))
                permissionsNeeded.add("AUDIO");
        }

//        if (checkPermissionId == REQUEST_CODE_ASK_SMS) {
//            if (!addPermission(context, permissionsList, Manifest.permission.RECEIVE_SMS)) {
//                permissionsNeeded.add("SMS");
//            }
//        }

        if (checkPermissionId == REQUEST_CODE_ASK_FOREGROUND) {
            if (!addPermission(context, permissionsList, Manifest.permission.FOREGROUND_SERVICE))
                permissionsNeeded.add("FOREGROUND SERVICE");
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                ActivityCompat.requestPermissions((Activity) context, permissionsList.toArray(new String[permissionsList.size()]),
                        checkPermissionId);
            }
            ActivityCompat.requestPermissions((Activity) context, permissionsList.toArray(new String[permissionsList.size()]),
                    checkPermissionId);
            return false;
        } else {
            return true;
        }
    }


    private boolean addPermission(Context context, List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}
