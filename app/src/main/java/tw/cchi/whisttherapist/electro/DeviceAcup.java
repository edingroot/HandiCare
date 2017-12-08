package tw.cchi.whisttherapist.electro;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.nio.ByteBuffer;

import tw.cchi.whisttherapist.GlobalVariable;

public class DeviceAcup {
    private static final String ACTION_USB_PERMISSION = "shengmao.elecacup.USB_PERMISSION";
    private static final int OPT_BATTERY_LOW = 1;
    public static int[] nFreqLevel = new int[]{0, 94, 88, 82, 76, 70, 64, 58, 52, 46, 40, 34, 28, 21, 14, 7};
    private static final int[] nFreqLevel_15 = new int[]{0, 94, 88, 82, 76, 70, 64, 58, 52, 46, 40, 34, 28, 21, 14, 7};
    private static final int[] nFreqLevel_5 = new int[]{0, 100, 80, 60, 40, 20};
    byte[] Readbuf = new byte[32];
    byte[] Sendbuf = new byte[8];
    public AcupStorage alg = new AcupStorage();
    private ByteBuffer buffer;
    private byte byProductCode = (byte) 0;
    private GlobalVariable globalVar;
    public UsbDeviceConnection mConnection;
    public UsbEndpoint mEndpointRead;
    public UsbEndpoint mEndpointWrite;
    public UsbDevice mUsbDevice;
    public UsbInterface mUsbInterface;
    public UsbManager mUsbManager = null;
    private Context main;

    public DeviceAcup(GlobalVariable gv, Context c, UsbManager um) {
        this.globalVar = gv;
        this.main = c;
        this.mUsbManager = um;
        if (getTheTargetDevice() != null) {
            if (this.mUsbDevice.getInterfaceCount() > 0) {
                this.mUsbInterface = this.mUsbDevice.getInterface(0);
                this.mEndpointRead = this.mUsbInterface.getEndpoint(0);
                this.mEndpointWrite = this.mUsbInterface.getEndpoint(1);
            }
            AcupStorage acupStorage;
            if (this.mUsbInterface == null || this.mUsbInterface.getEndpointCount() > 0) {
                commWithUsbDevice();
                acupStorage = this.alg;
            } else {
                commWithUsbDevice();
                acupStorage = this.alg;
            }
            if (AcupStorage.nDeviceType != 0) {
                commWithUsbDevice(11);
                commWithUsbDevice(12);
            }
        }
    }

    public UsbDevice getTheTargetDevice() {
        if (this.mUsbManager == null) {
            return null;
        }
        for (UsbDevice device : this.mUsbManager.getDeviceList().values()) {
            int nVenderID = device.getVendorId();
            int nProductID = device.getProductId();
            int nDevType = 0;
            boolean bFound = false;
            if (device.getProductId() == 57344) {
                bFound = true;
            }
            if (nVenderID == 4163 && nProductID / 256 == 160) {
                nDevType = nProductID % 256;
                bFound = true;
                // TODO: check if this is intentionally ignored ("continue")
//                continue;
            }
            if (bFound) {
                this.alg.setVersion(nDevType);
                setFreqLevel(nDevType);
                this.mUsbDevice = device;
                PendingIntent pi = PendingIntent.getBroadcast(this.main, 0, new Intent(ACTION_USB_PERMISSION), 0);
                if (!this.mUsbManager.hasPermission(device)) {
                    this.mUsbManager.requestPermission(device, pi);
                }
                return this.mUsbDevice;
            }
        }
        return null;
    }

    public void commWithUsbDevice() {
        this.mConnection = this.mUsbManager.openDevice(this.mUsbDevice);
        if (this.mConnection != null) {
            this.globalVar.bUsb = true;
            if (this.mConnection.claimInterface(this.mUsbInterface, true)) {
                boolean bFail = false;
                int nRetry = 1;
                while (nRetry > 0) {
                    this.Sendbuf[0] = (byte) 67;
                    this.Sendbuf[1] = (byte) 1;
                    this.Sendbuf[2] = (byte) 7;
                    this.Sendbuf[3] = (byte) 0;
                    this.Sendbuf[4] = (byte) 0;
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.controlTransfer(33, 9, 768, 0, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Sendbuf[0] = (byte) 7;
                    this.Sendbuf[1] = (byte) 10;
                    if (this.globalVar.bPower) {
                        this.Sendbuf[2] = (byte) this.globalVar.nX;
                        this.Sendbuf[3] = (byte) nFreqLevel[this.globalVar.nY];
                        this.Sendbuf[4] = (byte) 1;
                    } else {
                        this.Sendbuf[2] = (byte) 0;
                        this.Sendbuf[3] = (byte) 0;
                        this.Sendbuf[4] = (byte) 0;
                    }
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.bulkTransfer(this.mEndpointWrite, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Sendbuf[0] = (byte) 67;
                    this.Sendbuf[1] = (byte) 2;
                    this.Sendbuf[2] = (byte) 7;
                    this.Sendbuf[3] = (byte) 0;
                    this.Sendbuf[4] = (byte) 0;
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.controlTransfer(33, 9, 768, 0, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Readbuf[0] = (byte) 7;
                    if (this.mConnection.bulkTransfer(this.mEndpointRead, this.Readbuf, 32, 3000) != 32) {
                        bFail = true;
                    }
                    nRetry--;
                    if (!bFail) {
                        return;
                    }
                }
                return;
            }
            return;
        }
        this.globalVar.bUsb = false;
    }

    public void commWithUsbDevice(int nCmd) {
        this.mConnection = this.mUsbManager.openDevice(this.mUsbDevice);
        if (this.mConnection != null) {
            this.globalVar.bUsb = true;
            if (this.mConnection.claimInterface(this.mUsbInterface, true)) {
                boolean bFail = false;
                int nRetry = 1;
                while (nRetry > 0) {
                    this.Sendbuf[0] = (byte) 67;
                    this.Sendbuf[1] = (byte) 1;
                    this.Sendbuf[2] = (byte) 7;
                    this.Sendbuf[3] = (byte) 0;
                    this.Sendbuf[4] = (byte) 0;
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.controlTransfer(33, 9, 768, 0, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Sendbuf[0] = (byte) 7;
                    this.Sendbuf[1] = (byte) nCmd;
                    this.Sendbuf[2] = (byte) 0;
                    this.Sendbuf[3] = (byte) 0;
                    this.Sendbuf[4] = (byte) 0;
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.bulkTransfer(this.mEndpointWrite, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Sendbuf[0] = (byte) 67;
                    this.Sendbuf[1] = (byte) 2;
                    this.Sendbuf[2] = (byte) 7;
                    this.Sendbuf[3] = (byte) 0;
                    this.Sendbuf[4] = (byte) 0;
                    this.Sendbuf[5] = (byte) 0;
                    this.Sendbuf[6] = (byte) 0;
                    this.Sendbuf[7] = (byte) 0;
                    if (this.mConnection.controlTransfer(33, 9, 768, 0, this.Sendbuf, 8, 250) != 8) {
                        bFail = true;
                    }
                    this.Readbuf[0] = (byte) 7;
                    if (this.mConnection.bulkTransfer(this.mEndpointRead, this.Readbuf, 32, 3000) != 32) {
                        bFail = true;
                    }
                    String strDisp = "";
                    switch (nCmd) {
                        case 11:
                            if (this.Readbuf[7] < (byte) 21) {
                                this.byProductCode = this.Readbuf[4];
                                this.alg.setDeviceInfo(this.Readbuf);
                            } else {
                                this.byProductCode = this.alg.setDeviceInfo(this.Readbuf);
                            }
                            this.alg.setIni(this.main, "device", String.format("%07d", new Object[]{Integer.valueOf(this.alg.getDeviceSerialInt())}));
                            this.alg.setIni(this.main, "vendor", String.format("%d", new Object[]{Integer.valueOf(this.alg.getDeviceVendor())}));
                            break;
                        case 12:
                            this.alg.setTagInfo(this.Readbuf);
                            break;
                    }
                    nRetry--;
                    if (!bFail) {
                        return;
                    }
                }
                return;
            }
            return;
        }
        this.globalVar.bUsb = false;
    }

    public void setFreqLevel(int nVer) {
        int i;
        switch (nVer) {
            case 0:
                for (i = 0; i < 16; i++) {
                    nFreqLevel[i] = i;
                }
                return;
            case 1:
                for (i = 0; i < 16; i++) {
                    nFreqLevel[i] = nFreqLevel_15[i];
                }
                return;
            default:
                return;
        }
    }
}
