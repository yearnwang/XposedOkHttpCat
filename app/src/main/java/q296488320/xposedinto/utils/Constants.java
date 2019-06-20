package q296488320.xposedinto.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by lyh on 2019/2/14.
 */

public class Constants {

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }


    /**
     * 判断 服务是否 存在
     * @param context
     * @param servicername
     * @return
     */
    public static boolean isRunning(Context context, String servicername){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos= am != null ? am.getRunningServices(100) : null;
        for (ActivityManager.RunningServiceInfo info:infos){
            String name =   info.service.getClassName();
            CLogUtils.e("服务名字   "+name);
            if(servicername.equals(name)){
                return true;
            }
        }
        return false;
    }



    /**
     * 开启悬浮窗设置页面
     * @param activity
     */
    public static void openFloatingDialog(Activity activity){
        if (Build.VERSION.SDK_INT < 23){
            return;
        }
        Intent intent = null;
        try {
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName(activity)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        activity.startActivity(intent);
        activity=null;
    }

    /**
     * 判断悬浮窗权限
     *
     * @param context 上下文
     */
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            return checkOp(context, 24);//24表示悬浮窗权限在AppOpsManager中
        } else {
            //0x8000000表示1000000000000000000000000000如果&第28位所得值为1则该位置被置为1，悬浮窗打开
            return (context.getApplicationInfo().flags & 0x8000000) == 1<<27;
        }
    }

    private static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService("appops");
            try {
                Object object = invokeMethod(manager, "checkOp", op, Binder.getCallingUid(), getPackageName(context));
                return AppOpsManager.MODE_ALLOWED == (Integer) object;
            } catch (Exception e) {
                CLogUtils.e("CheckMIUI", e.toString());
            }
        } else {
            CLogUtils.e("CheckMIUI", "Below API 19 cannot invoke!");
        }
        return false;
    }

    public static String getPackageName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.packageName;
    }

    public static Object invokeMethod(AppOpsManager manager, String method, int op, int uid, String packageName){
        Class c = manager.getClass();
        try {
            Class[] classes = new Class[] {int.class, int.class, String.class};
            Object[] x2 = {op, uid, packageName};
            Method m = c.getDeclaredMethod(method, classes);
            return m.invoke(manager, x2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }




    /**
     * 在 sd卡创建 指定 目录
     * @param fileName
     */
    public static  void createDir(String fileName){
        //Environment.getExternalStorageDirectory().getAbsolutePath():SD卡根目录
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+fileName);
        if (!file.exists()){
            boolean isSuccess = file.mkdirs();
            //Toast.makeText(MainActivity.this,"文件夹创建成功",Toast.LENGTH_LONG).show();
            CLogUtils.e("Sd文件夹 创建成功 " +fileName);
        }
        return ;
    }

    /**
     * 返回 sd卡 路径
     * @return
     */
    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString()+"/";
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }
}
