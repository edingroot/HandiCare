package tw.cchi.handicare.eshock;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;

import java.util.Arrays;

import tw.cchi.handicare.eshock.port.TransportMediator;

public class AcupStorage {
//    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static int nDeviceSerial = 0;
    public static int nDeviceType = 0;
    private static int nTagSerial = 0;
    private static int nVendorID = 1;
//    private static String sAccount = null;
//    private static String sCheckDate = null;
    private static String strDeviceSerial = "";
    public static String strFirmwareVersion = "1.0";
    public static String strVersion = "Demo";
    private AES aesobj = new AES();

    AcupStorage() {
        this.aesobj.Init();
    }

    public void setVersion(int nVer) {
        switch (nVer) {
            case 0:
                strVersion = "V1.0";
                break;
            case 1:
                strVersion = "V1.1";
                break;
        }
        nDeviceType = nVer;
    }

    public String getVersion() {
        String strval = "";
        if (nDeviceType == 0) {
            return "Vendor: " + String.format("%d", new Object[]{Integer.valueOf(nVendorID)}) + "\r\nDevice: " + strVersion;
        }
        strval = "Vendor: " + String.format("%d", new Object[]{Integer.valueOf(nVendorID)}) + "\r\nDevice: " + strDeviceSerial + "\r\nFirmware: " + strFirmwareVersion;
        if (nTagSerial == 9999999) {
            return strval;
        }
        return strval + "\r\nTAG: " + String.format("%07d", new Object[]{Integer.valueOf(nTagSerial)});
    }

    public String getVersion(String strVer) {
        String strval = "";
        if (nDeviceType == 0) {
            return "Vendor: " + String.format("%d", new Object[]{Integer.valueOf(nVendorID)}) + "\r\nDevice: " + strVer;
        }
        strval = "Vendor: " + String.format("%d", new Object[]{Integer.valueOf(nVendorID)}) + "\r\nDevice: " + strDeviceSerial + "\r\nFirmware: " + strFirmwareVersion;
        if (nTagSerial == 9999999) {
            return strval;
        }
        return strval + "\r\nTAG: " + String.format("%07d", new Object[]{Integer.valueOf(nTagSerial)});
    }

    public String getDeviceSerial() {
        return strDeviceSerial;
    }

    public int getDeviceSerialInt() {
        return nDeviceSerial;
    }

    public int getDeviceVendor() {
        return nVendorID;
    }

    public byte setDeviceInfo(byte[] buf) {
        strFirmwareVersion = "V" + Integer.toString(buf[7] / 16) + "." + Integer.toString(buf[7] % 16);
        if (buf[7] < (byte) 21) {
            nDeviceSerial = ((buf[1] & MotionEventCompat.ACTION_MASK) + ((buf[2] & MotionEventCompat.ACTION_MASK) * 256)) + ((buf[3] & MotionEventCompat.ACTION_MASK) * 65536);
            if (nDeviceSerial > 9999999) {
                nDeviceSerial = 9999999;
            }
            strDeviceSerial = String.format("%07d", new Object[]{Integer.valueOf(nDeviceSerial)});
            nVendorID = 1;
            return (byte) 0;
        }
        byte nSum = (byte) 0;
        for (int i = 0; i < 16; i++) {
            nSum = (byte) ((nSum + ConvertByteInt(buf[i + 14])) % 256);
        }
        if (nSum > TransportMediator.KEYCODE_MEDIA_PAUSE) {
            nSum -= 256;
        }
        if (nSum != buf[30]) {
            return (byte) 0;
        }
        byte[] byRet = this.aesobj.Decrypt(Arrays.copyOfRange(buf, 14, 30));
        nDeviceSerial = ((byRet[1] & MotionEventCompat.ACTION_MASK) + ((byRet[2] & MotionEventCompat.ACTION_MASK) * 256)) + ((byRet[3] & MotionEventCompat.ACTION_MASK) * 65536);
        if (nDeviceSerial > 9999999) {
            nDeviceSerial = 9999999;
        }
        strDeviceSerial = String.format("%07d", new Object[]{Integer.valueOf(nDeviceSerial)});
        if (byRet[0] < (byte) 0) {
            nVendorID = byRet[0] + MotionEventCompat.ACTION_MASK;
        } else {
            nVendorID = byRet[0];
        }
        return byRet[0];
    }

    public void setTagInfo(byte[] buf) {
        nTagSerial = ((buf[4] & MotionEventCompat.ACTION_MASK) + ((buf[3] & MotionEventCompat.ACTION_MASK) * 256)) + ((buf[2] & MotionEventCompat.ACTION_MASK) * 65536);
        if (nTagSerial > 9999999) {
            nTagSerial = 9999999;
        }
    }

//    public static void setAccount(Context c, String s) {
//        PreferenceManager.getDefaultSharedPreferences(c).edit().putString("account_number", s).commit();
//        sAccount = s;
//    }

    public String getIni(Context c, String sTag) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(sTag, "");
    }

    public void setIni(Context c, String sTag, String sValue) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(sTag, sValue).commit();
    }

//    public void setDeviceInfo(Context c, String sEnable, String sMall, String sPush, String sMallWeb) {
//        setIni(c, "enable", sEnable);
//        setIni(c, "mall", sMall);
//        setIni(c, "mall_web", sMallWeb);
//        setIni(c, "push", sPush);
//    }

//    public static void setCheckDate(Context c, String s) {
//        PreferenceManager.getDefaultSharedPreferences(c).edit().putString("check_date", s).commit();
//        sCheckDate = s;
//    }

//    public static String getCheckDate(Context c) {
//        sCheckDate = PreferenceManager.getDefaultSharedPreferences(c).getString("check_date", "");
//        return sCheckDate;
//    }

//    public String asHex(byte[] buf, int nLen, int nStart) {
//        char[] chars = new char[(nLen * 2)];
//        for (int i = 0; i < nLen; i++) {
//            int n = (nStart + i) + 1;
//            chars[i * 2] = HEX_CHARS[(buf[n] & 240) >>> 4];
//            chars[(i * 2) + 1] = HEX_CHARS[buf[n] & 15];
//        }
//        return new String(chars);
//    }

    public int ConvertByteInt(byte byVal) {
        if (byVal < (byte) 0) {
            return byVal + 256;
        }
        return byVal;
    }
}
