package tw.cchi.whisttherapist;

public class Util {

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

}
