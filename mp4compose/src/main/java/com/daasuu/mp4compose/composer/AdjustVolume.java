package com.daasuu.mp4compose.composer;

public class AdjustVolume {

    private static int N_SHORTS = 0xffff;
    private static final short[] VOLUME_NORM_LUT = new short[N_SHORTS];
    private static int MAX_NEGATIVE_AMPLITUDE = 0x8000;

    static {
        precomputeVolumeNormLUT();
    }

    public static byte[] normalizeVolume(byte[] audioSamples, int start, int len) {
        try {
            for (int i = start; i < start+len; i+=2) {
                // convert byte pair to int
                short s1 = audioSamples[i+1];
                short s2 = audioSamples[i];

                s1 = (short) ((s1 & 0xff) << 8);
                s2 = (short) (s2 & 0xff);

                short res = (short) (s1 | s2);

                res = VOLUME_NORM_LUT[res+MAX_NEGATIVE_AMPLITUDE];
                audioSamples[i] = (byte) res;
                audioSamples[i+1] = (byte) (res >> 8);
            }
        } catch (ArrayIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        return audioSamples;
    }

    private static void precomputeVolumeNormLUT() {
        for(int s=0; s<N_SHORTS; s++) {
            double v = s-MAX_NEGATIVE_AMPLITUDE;
            double sign = Math.signum(v);
            // Non-linear volume boost function
            // fitted exponential through (0,0), (10000, 25000), (32767, 32767)
            VOLUME_NORM_LUT[s]=(short)(sign*(1.240769e-22 - (-4.66022/0.0001408133)*
                    (1 - Math.exp(-0.0001408133*v*sign))));
        }
    }
}
