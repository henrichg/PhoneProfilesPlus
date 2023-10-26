package sk.henrichg.phoneprofilesplus;

class BrightnessLookup {

/*
    // https://stackoverflow.com/questions/73220966/how-to-get-the-brightness-percentage-in-android

    static final int GAMMA_SPACE_MIN = 0;
    static final int GAMMA_SPACE_MAX = 65535;

    // Hybrid Log Gamma constant values
    private static final float R = 0.5f;
    private static final float A = 0.17883277f;
    private static final float B = 0.28466892f;
    private static final float C = 0.55991073f;

    static int convertLinearToGammaFloat(float val, float min, float max) {
        // For some reason, HLG normalizes to the range [0, 12] rather than [0, 1]
        final float normalizedVal = PPMathUtils.norm(min, max, val) * 12;
        float ret;
        if (normalizedVal <= 1f) {
            ret = PPMathUtils.sqrt(normalizedVal) * R;
        } else {
            ret = A * PPMathUtils.log(normalizedVal - B) + C;
        }

        ret = PPMathUtils.lerp(GAMMA_SPACE_MIN, GAMMA_SPACE_MAX, ret);
        //ret = ret / GAMMA_SPACE_MAX * max;
        //ret = ret * max;

        int iret = Math.round(ret);

        return iret;
    }

    static double getPercentage(double value, int min, int max) {
        if (value > max) {
            return 1.0;
        }
        if (value < min) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    static int convertGammaToLinear(int val, int min, int max) {
        final float normalizedVal = PPMathUtils.norm(GAMMA_SPACE_MIN, GAMMA_SPACE_MAX, val);
        final float ret;
        if (normalizedVal <= R) {
            ret = PPMathUtils.sq(normalizedVal / R);
        } else {
            ret = PPMathUtils.exp((normalizedVal - C) / A) + B;
        }

        // HLG is normalized to the range [0, 12], so we need to re-normalize to the range [0, 1]
        // in order to derive the correct setting value.
        return Math.round(PPMathUtils.lerp(min, max, ret / 12));
    }

    static float convertGammaToLinearFloat(float val, float min, float max) {
        final float normalizedVal = PPMathUtils.norm(GAMMA_SPACE_MIN, GAMMA_SPACE_MAX, val);
        Log.e("BrightnessLookup.convertLinearToGammaFloat", "normalizedVal="+normalizedVal);
        final float ret;
        if (normalizedVal <= R) {
            ret = PPMathUtils.sq(normalizedVal / R);
        } else {
            ret = PPMathUtils.exp((normalizedVal - C) / A) + B;
        }
        Log.e("BrightnessLookup.convertLinearToGammaFloat", "ret="+ret);

        // HLG is normalized to the range [0, 12], ensure that value is within that range,
        // it shouldn't be out of bounds.
        final float normalizedRet = PPMathUtils.constrain(ret, 0, 12);
        Log.e("BrightnessLookup.convertLinearToGammaFloat", "normalizedRet="+normalizedRet);

        // Re-normalize to the range [0, 1]
        // in order to derive the correct setting value.
        return PPMathUtils.lerp(min, max, normalizedRet / 12);
    }
*/

    //https://tunjid.medium.com/reverse-engineering-android-pies-logarithmic-brightness-curve-ecd41739d7a2

    private static final int FLICKER_THRESHOLD = 24;

    static int lookup(int query, boolean isByte) {
        int[][] _table = (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ? tableXiaomi : table;

        int low = 0;
        int mid = 0;
        int high = _table.length - 1;
        boolean increasing = true;

        // No direct mapping for bytes, just crawl for the lower values
        if (!isByte && query < FLICKER_THRESHOLD) return crawl(query, 0, false, true);

        while (low <= high) {
            mid = (low + high) / 2;
            int key = _table[mid][isByte ? 0 : 1];
            int diff = query - key;
            increasing = diff > 0;

            if (Math.abs(diff) < 2) return crawl(query, mid, isByte, increasing);
            else if (increasing) low = mid + 1;
            else high = mid - 1;
        }

        return crawl(query, mid, isByte, increasing);
    }

    private static int crawl(int query, int index, boolean isByte, boolean increasing) {
        int[][] _table = (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ? tableXiaomi : table;

        int i = index;
        int num = _table.length;
        int key = isByte ? 0 : 1;
        int value = isByte ? 1 : 0;

        if (increasing) { for (; i < num; i++) if (_table[i][key] >= query) return _table[i][value]; }
        else for (; i >= 0; i--) if (_table[i][key] <= query) return _table[i][value];

        return 0;
    }

    private static final int[][] table = {
            {0, 0},
            {1, 6},
            {2, 11},
            {3, 15},
            {4, 19},
            {5, 22},
            {6, 24},
            {7, 27},
            {8, 29},
            {9, 31},
            {10, 33},
            {11, 34},
            {12, 36},
            {13, 38},
            {14, 39},
            {15, 41},
            {16, 42},
            {17, 43},
            {18, 45},
            {19, 46},
            {20, 47},
            {21, 49},
            {22, 50},
            {23, 51},
            {24, 52},
            {25, 53},
            {26, 54},
            {27, 55},
            {28, 56},
            {29, 57},
            {30, 57},
            {31, 57},
            {32, 57},
            {33, 60},
            {34, 60},
            {35, 61},
            {36, 62},
            {37, 62},
            {38, 63},
            {39, 63},
            {40, 64},
            {41, 64},
            {42, 65},
            {43, 65},
            {44, 66},
            {45, 66},
            {46, 67},
            {47, 67},
            {48, 68},
            {49, 68},
            {50, 69},
            {51, 69},
            {52, 70},
            {53, 70},
            {54, 70},
            {55, 71},
            {56, 71},
            {57, 71},
            {58, 72},
            {59, 72},
            {60, 72},
            {61, 73},
            {62, 73},
            {63, 73},
            {64, 74},
            {65, 74},
            {66, 74},
            {67, 75},
            {68, 75},
            {71, 76},
            {73, 76},
            {76, 77},
            {79, 78},
            {80, 78},
            {81, 78},
            {84, 79},
            {86, 80},
            {89, 80},
            {91, 81},
            {94, 81},
            {96, 82},
            {97, 82},
            {98, 82},
            {99, 82},
            {102, 83},
            {104, 83},
            {107, 84},
            {109, 84},
            {112, 85},
            {114, 85},
            {117, 85},
            {119, 86},
            {122, 87},
            {124, 87},
            {127, 87},
            {130, 88},
            {132, 88},
            {133, 88},
            {134, 88},
            {135, 88},
            {136, 88},
            {137, 88},
            {138, 89},
            {139, 89},
            {140, 89},
            {141, 89},
            {142, 89},
            {143, 89},
            {144, 89},
            {145, 90},
            {146, 90},
            {147, 90},
            {148, 90},
            {149, 90},
            {150, 90},
            {151, 90},
            {152, 91},
            {153, 91},
            {154, 91},
            {155, 91},
            {156, 91},
            {157, 91},
            {158, 91},
            {159, 91},
            {160, 91},
            {161, 91},
            {162, 92},
            {163, 92},
            {164, 92},
            {165, 92},
            {166, 92},
            {167, 92},
            {168, 92},
            {169, 92},
            {170, 92},
            {171, 93},
            {172, 93},
            {173, 93},
            {174, 93},
            {175, 93},
            {176, 93},
            {177, 93},
            {178, 93},
            {179, 93},
            {180, 94},
            {181, 94},
            {182, 94},
            {183, 94},
            {184, 94},
            {185, 94},
            {186, 94},
            {187, 94},
            {188, 94},
            {189, 94},
            {190, 95},
            {191, 95},
            {192, 95},
            {193, 95},
            {194, 95},
            {195, 95},
            {196, 95},
            {197, 95},
            {198, 95},
            {199, 95},
            {200, 96},
            {201, 96},
            {202, 96},
            {203, 96},
            {204, 96},
            {205, 96},
            {206, 96},
            {207, 96},
            {208, 96},
            {209, 96},
            {210, 96},
            {211, 96},
            {212, 97},
            {213, 97},
            {214, 97},
            {215, 97},
            {216, 97},
            {217, 97},
            {218, 97},
            {219, 97},
            {220, 97},
            {221, 97},
            {222, 97},
            {223, 98},
            {224, 98},
            {225, 98},
            {226, 98},
            {227, 98},
            {228, 98},
            {229, 98},
            {230, 98},
            {231, 98},
            {232, 98},
            {233, 98},
            {234, 98},
            {235, 99},
            {236, 99},
            {237, 99},
            {238, 99},
            {239, 99},
            {240, 99},
            {241, 99},
            {242, 99},
            {243, 99},
            {244, 99},
            {245, 99},
            {246, 99},
            {247, 99},
            {248, 100},
            {249, 100},
            {250, 100},
            {251, 100},
            {252, 100},
            {253, 100},
            {254, 100},
            {255, 100}
    };

    private static final int[][] tableXiaomi = {
            {0, 0},
            {1, 1},
            {2, 2},
            {3, 3},
            {4, 4},
            {5, 5},
            {6, 6},
            {7, 7},
            {8, 8},
            {9, 9},
            {10, 10},
            {11, 11},
            {12, 12},
            {13, 13},
            {14, 14},
            {15, 15},
            {16, 16},
            {17, 17},
            {18, 18},
            {19, 19},
            {20, 20},
            {21, 21},
            {22, 22},
            {23, 23},
            {24, 24},
            {25, 25},
            {26, 26},
            {27, 27},
            {28, 28},
            {29, 29},
            {30, 30},
            {31, 31},
            {32, 32},
            {33, 33},
            {34, 34},
            {35, 35},
            {36, 36},
            {37, 37},
            {38, 38},
            {39, 39},
            {40, 40},
            {41, 41},
            {42, 42},
            {43, 43},
            {44, 44},
            {45, 44},
            {46, 45},
            {47, 45},
            {48, 46},
            {49, 46},
            {50, 47},
            {51, 47},
            {52, 48},
            {53, 48},
            {54, 49},
            {55, 50},
            {57, 51},
            {58, 52},
            {60, 53},
            {61, 54},
            {63, 55},
            {64, 56},
            {65, 57},
            {66, 58},
            {67, 59},
            {70, 60},
            {71, 61},
            {73, 62},
            {76, 63},
            {79, 64},
            {80, 65},
            {81, 66},
            {84, 67},
            {86, 68},
            {89, 69},
            {90, 70},
            {92, 71},
            {94, 72},
            {96, 73},
            {98, 74},
            {100, 75},
            {104, 75},
            {107, 76},
            {109, 76},
            {112, 76},
            {114, 77},
            {150, 77},
            {119, 77},
            {122, 77},
            {124, 78},
            {127, 78},
            {130, 78},
            {132, 78},
            {133, 78},
            {134, 79},
            {135, 79},
            {136, 79},
            {137, 79},
            {138, 80},
            {139, 80},
            {140, 80},
            {141, 80},
            {142, 80},
            {143, 81},
            {144, 81},
            {145, 81},
            {146, 81},
            {147, 82},
            {148, 82},
            {149, 82},
            {150, 82},
            {151, 82},
            {152, 83},
            {153, 83},
            {154, 83},
            {155, 83},
            {156, 83},
            {157, 84},
            {158, 84},
            {159, 84},
            {160, 84},
            {161, 84},
            {162, 84},
            {163, 85},
            {164, 85},
            {165, 85},
            {166, 85},
            {167, 85},
            {168, 85},
            {169, 86},
            {170, 86},
            {171, 86},
            {172, 86},
            {173, 87},
            {174, 87},
            {175, 87},
            {176, 87},
            {177, 88},
            {178, 88},
            {179, 88},
            {180, 88},
            {181, 88},
            {182, 89},
            {183, 89},
            {184, 89},
            {185, 89},
            {186, 90},
            {187, 90},
            {188, 90},
            {189, 90},
            {190, 90},
            {191, 91},
            {192, 91},
            {193, 91},
            {194, 91},
            {195, 91},
            {196, 92},
            {197, 92},
            {198, 92},
            {199, 92},
            {200, 92},
            {201, 93},
            {202, 93},
            {203, 93},
            {204, 93},
            {205, 94},
            {206, 94},
            {207, 94},
            {208, 94},
            {209, 94},
            {210, 95},
            {211, 95},
            {212, 95},
            {213, 95},
            {214, 95},
            {215, 95},
            {216, 96},
            {217, 96},
            {218, 96},
            {219, 96},
            {220, 96},
            {221, 96},
            {222, 96},
            {223, 97},
            {224, 97},
            {225, 97},
            {226, 97},
            {227, 97},
            {228, 97},
            {229, 97},
            {230, 97},
            {231, 98},
            {232, 98},
            {233, 98},
            {234, 98},
            {235, 98},
            {236, 98},
            {237, 98},
            {238, 98},
            {239, 98},
            {240, 98},
            {241, 98},
            {242, 99},
            {243, 99},
            {244, 99},
            {245, 99},
            {246, 99},
            {247, 99},
            {248, 99},
            {249, 99},
            {250, 99},
            {251, 99},
            {252, 99},
            {253, 99},
            {254, 100},
            {255, 100}
    };

}