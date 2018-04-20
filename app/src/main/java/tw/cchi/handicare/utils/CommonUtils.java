package tw.cchi.handicare.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import tw.cchi.handicare.R;

public final class CommonUtils {

    private CommonUtils() {
        // This utility class is not publicly instantiable
    }

    public static ProgressDialog showLoadingDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    /**
     * min <= newValue <= max
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int trimValue(int value, int min, int max) {
        if (value < min)
            value = min;
        else if (value > max)
            value = max;
        return value;
    }

    public static String padLeft(String input, char padChar, int length) {
        for (int i = input.length(); i < length; i++)
            input = padChar + input;
        return input;
    }

}
