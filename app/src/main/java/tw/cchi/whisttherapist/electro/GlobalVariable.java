package tw.cchi.whisttherapist.electro;

import android.app.Application;
import android.webkit.WebView;
import android.widget.TextView;

public class GlobalVariable extends Application {
    public boolean bPower = false;
    public boolean bSerialMatch = false;
    public boolean bUpdateApk = false;
    public boolean bUsb = false;
    public boolean bVersionDemo = false;
    public boolean bVibrateDemo = false;
    public boolean isCounterRunning = false;
    public WebView myWebView;
    public int nVerBuild = 0;
    public int nVerMajor = 0;
    public int nVerMiner = 0;
    public int nVerNumber = 0;
    public int nX = 1;
    public int nY = 1;
    public int nZ = 1;
    public String sDevEnable = "0";
    public String sDevMall = "0";
    public String sDevMallUrl = "";
    public String sDevPush = "0";
    public String sDevPushStart = "";
    public String sDevPushStop = "";
    public String sDevPushText = "";
    public String sPushText = "";
    public String strAndroidSerial;
    public String strAppVersion = "";
    public String strIMEI;
    public String strPhoneNumber;
    public String strVersion;
    public String strWebApiUrl = "";
    public String strWebVersion = "";
    public TextView textWebTitle;
}
