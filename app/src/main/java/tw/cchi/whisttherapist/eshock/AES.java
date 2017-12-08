package tw.cchi.whisttherapist.eshock;

import android.support.v4.view.MotionEventCompat;

import java.util.Arrays;

import tw.cchi.whisttherapist.eshock.port.TransportMediator;

public class AES {
    private int[] AES_Sbox = new int[]{99, 124, 119, 123, 242, 107, 111, 197, 48, 1, 103, 43, 254, 215, 171, 118, 202, TransportMediator.KEYCODE_MEDIA_RECORD, 201, 125, 250, 89, 71, 240, 173, 212, 162, 175, 156, 164, 114, 192, 183, 253, 147, 38, 54, 63, 247, 204, 52, 165, 229, 241, 113, 216, 49, 21, 4, 199, 35, 195, 24, 150, 5, 154, 7, 18, 128, 226, 235, 39, 178, 117, 9, 131, 44, 26, 27, 110, 90, 160, 82, 59, 214, 179, 41, 227, 47, 132, 83, 209, 0, 237, 32, 252, 177, 91, 106, 203, 190, 57, 74, 76, 88, 207, 208, 239, 170, 251, 67, 77, 51, 133, 69, 249, 2, TransportMediator.KEYCODE_MEDIA_PAUSE, 80, 60, 159, 168, 81, 163, 64, 143, 146, 157, 56, 245, 188, 182, 218, 33, 16, MotionEventCompat.ACTION_MASK, 243, 210, 205, 12, 19, 236, 95, 151, 68, 23, 196, 167, TransportMediator.KEYCODE_MEDIA_PLAY, 61, 100, 93, 25, 115, 96, 129, 79, 220, 34, 42, 144, 136, 70, 238, 184, 20, 222, 94, 11, 219, 224, 50, 58, 10, 73, 6, 36, 92, 194, 211, 172, 98, 145, 149, 228, 121, 231, 200, 55, 109, 141, 213, 78, 169, 108, 86, 244, 234, 101, 122, 174, 8, 186, 120, 37, 46, 28, 166, 180, 198, 232, 221, 116, 31, 75, 189, 139, 138, 112, 62, 181, 102, 72, 3, 246, 14, 97, 53, 87, 185, 134, 193, 29, 158, 225, 248, 152, 17, 105, 217, 142, 148, 155, 30, 135, 233, 206, 85, 40, 223, 140, 161, 137, 13, 191, 230, 66, 104, 65, 153, 45, 15, 176, 84, 187, 22};
    private int[] AES_Sbox_Inv = new int[256];
    private int[] AES_ShiftRowTab = new int[]{0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
    private int[] AES_ShiftRowTab_Inv = new int[16];
    private int[] AES_xtime = new int[256];
    private int[] m_byKey = new int[240];
    private int m_nKeyLen;

    private void AES_SubBytes(int[] state, int[] sbox) {
        for (int i = 0; i < 16; i++) {
            state[i] = sbox[state[i]];
        }
    }

    private void AES_AddRoundKey(int[] state, int[] rkey) {
        for (int i = 0; i < 16; i++) {
            state[i] = state[i] ^ rkey[i];
        }
    }

    private void AES_ShiftRows(int[] state, int[] shifttab) {
        int i;
        int[] h = new int[16];
        for (i = 0; i < 16; i++) {
            h[i] = state[i];
        }
        for (i = 0; i < 16; i++) {
            state[i] = h[shifttab[i]];
        }
    }

    private void AES_MixColumns(int[] state) {
        for (int i = 0; i < 16; i += 4) {
            int s0 = state[i + 0];
            int s1 = state[i + 1];
            int s2 = state[i + 2];
            int s3 = state[i + 3];
            int h = ((s0 ^ s1) ^ s2) ^ s3;
            int i2 = i + 0;
            state[i2] = state[i2] ^ (this.AES_xtime[s0 ^ s1] ^ h);
            i2 = i + 1;
            state[i2] = state[i2] ^ (this.AES_xtime[s1 ^ s2] ^ h);
            i2 = i + 2;
            state[i2] = state[i2] ^ (this.AES_xtime[s2 ^ s3] ^ h);
            i2 = i + 3;
            state[i2] = state[i2] ^ (this.AES_xtime[s3 ^ s0] ^ h);
        }
    }

    private void AES_MixColumns_Inv(int[] state) {
        for (int i = 0; i < 16; i += 4) {
            int s0 = state[i + 0];
            int s1 = state[i + 1];
            int s2 = state[i + 2];
            int s3 = state[i + 3];
            int h = ((s0 ^ s1) ^ s2) ^ s3;
            int xh = this.AES_xtime[h];
            int h1 = this.AES_xtime[this.AES_xtime[(xh ^ s0) ^ s2]] ^ h;
            int h2 = this.AES_xtime[this.AES_xtime[(xh ^ s1) ^ s3]] ^ h;
            int i2 = i + 0;
            state[i2] = state[i2] ^ (this.AES_xtime[s0 ^ s1] ^ h1);
            i2 = i + 1;
            state[i2] = state[i2] ^ (this.AES_xtime[s1 ^ s2] ^ h2);
            i2 = i + 2;
            state[i2] = state[i2] ^ (this.AES_xtime[s2 ^ s3] ^ h1);
            i2 = i + 3;
            state[i2] = state[i2] ^ (this.AES_xtime[s3 ^ s0] ^ h2);
        }
    }

    public void AES_Init() {
        int i;
        for (i = 0; i < 256; i++) {
            this.AES_Sbox_Inv[this.AES_Sbox[i]] = i;
        }
        for (i = 0; i < 16; i++) {
            this.AES_ShiftRowTab_Inv[this.AES_ShiftRowTab[i]] = i;
        }
        for (i = 0; i < 128; i++) {
            this.AES_xtime[i] = i << 1;
            this.AES_xtime[i + 128] = (i << 1) ^ 27;
        }
    }

    int AES_ExpandKey(int[] key, int keyLen) {
        int kl = keyLen;
        int ks = 0;
        int Rcon = 1;
        int[] temp = new int[4];
        int[] temp2 = new int[4];
        switch (kl) {
            case 16:
                ks = 176;
                break;
            case 24:
                ks = 208;
                break;
            case 32:
                ks = 240;
                break;
        }
        int i = kl;
        while (i < ks) {
            temp[0] = key[i - 4];
            temp[1] = key[i - 3];
            temp[2] = key[i - 2];
            temp[3] = key[i - 1];
            if (i % kl == 0) {
                temp2[0] = this.AES_Sbox[temp[1]] ^ Rcon;
                temp2[1] = this.AES_Sbox[temp[2]];
                temp2[2] = this.AES_Sbox[temp[3]];
                temp2[3] = this.AES_Sbox[temp[0]];
                temp[0] = temp2[0];
                temp[1] = temp2[1];
                temp[2] = temp2[2];
                temp[3] = temp2[3];
                Rcon <<= 1;
                if (Rcon >= 256) {
                    Rcon ^= 283;
                }
            } else if (kl > 24 && i % kl == 16) {
                temp2[0] = this.AES_Sbox[temp[0]];
                temp2[1] = this.AES_Sbox[temp[1]];
                temp2[2] = this.AES_Sbox[temp[2]];
                temp2[3] = this.AES_Sbox[temp[3]];
                temp[0] = temp2[0];
                temp[1] = temp2[1];
                temp[2] = temp2[2];
                temp[3] = temp2[3];
            }
            for (int j = 0; j < 4; j++) {
                key[i + j] = key[(i + j) - kl] ^ temp[j];
            }
            i += 4;
        }
        return ks;
    }

    public void Encrypt(int[] block, int[] key, int keyLen) {
        int l = keyLen;
        AES_AddRoundKey(block, key);
        int i = 16;
        while (i < l - 16) {
            AES_SubBytes(block, this.AES_Sbox);
            AES_ShiftRows(block, this.AES_ShiftRowTab);
            AES_MixColumns(block);
            AES_AddRoundKey(block, Arrays.copyOfRange(key, i, 16));
            i += 16;
        }
        AES_SubBytes(block, this.AES_Sbox);
        AES_ShiftRows(block, this.AES_ShiftRowTab);
        AES_AddRoundKey(block, Arrays.copyOfRange(key, i, 16));
    }

    public void AES_Decrypt(int[] block, int[] key, int keyLen) {
        int l = keyLen;
        AES_AddRoundKey(block, Arrays.copyOfRange(key, l - 16, l));
        AES_ShiftRows(block, this.AES_ShiftRowTab_Inv);
        AES_SubBytes(block, this.AES_Sbox_Inv);
        for (int i = l - 32; i >= 16; i -= 16) {
            AES_AddRoundKey(block, Arrays.copyOfRange(key, i, i + 16));
            AES_MixColumns_Inv(block);
            AES_ShiftRows(block, this.AES_ShiftRowTab_Inv);
            AES_SubBytes(block, this.AES_Sbox_Inv);
        }
        AES_AddRoundKey(block, Arrays.copyOfRange(key, 0, 16));
    }

    private int ConvertByteInt(byte byVal) {
        if (byVal < (byte) 0) {
            return byVal + 256;
        }
        return byVal;
    }

    public byte[] Decrypt(byte[] block) {
        int i;
        int[] nIn = new int[16];
        for (i = 0; i < 16; i++) {
            nIn[i] = ConvertByteInt(block[i]);
        }
        AES_Decrypt(nIn, this.m_byKey, this.m_nKeyLen);
        for (i = 0; i < 16; i++) {
            block[i] = (byte) (nIn[i] & MotionEventCompat.ACTION_MASK);
        }
        return block;
    }

    public void Init() {
        AES_Init();
        for (int i = 0; i < 32; i++) {
            this.m_byKey[i] = (byte) i;
        }
        this.m_nKeyLen = AES_ExpandKey(this.m_byKey, 32);
    }
}
