package com.example.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

/**
 * 动态权限帮助类
 */
public class PermissionHelper {

    private Activity mActivity;
    private PermissionInterface mPermissionInterface;

    public PermissionHelper(@NonNull Activity activity , @NonNull PermissionInterface permissionInterface){
        mActivity = activity ;
        mPermissionInterface = permissionInterface;
    }


    public void requestPermissions(){
        String[] denidPermissions = PermissionUtil.getDeniedPermissions(mActivity,mPermissionInterface.getPermissions());
        if (denidPermissions != null && denidPermissions.length>0){
            PermissionUtil.requestPermissions(mActivity,denidPermissions,mPermissionInterface.getPermissionsRequestCode());
        }
        else {
            mPermissionInterface.requestPermissionsSuccess();
        }
    }

    public boolean requestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        if (requestCode == mPermissionInterface.getPermissionsRequestCode()){
            boolean isAllGranted = true; // 是否全部权限已授权
            for (int result :grantResults){
                if (result == PackageManager.PERMISSION_GRANTED){
                    isAllGranted = false;
                    break;
                }
            }
            if ( isAllGranted){
                //已全部授权
                mPermissionInterface.requestPermissionsSuccess();
            }
            else {
                //授权有缺少
                mPermissionInterface.requestPermissionsFail();
            }
            return true;
        }
        return  false;
    }

}
