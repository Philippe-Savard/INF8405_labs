package com.example.tp2_inf8405;

public class DeviceInfoConverter {
    public static final int AUDIO_VIDEO_CAMCORDER = 1076;
    public static final int AUDIO_VIDEO_CAR_AUDIO = 1056;
    public static final int AUDIO_VIDEO_HANDSFREE = 1032;
    public static final int AUDIO_VIDEO_HEADPHONES = 1048;
    public static final int AUDIO_VIDEO_HIFI_AUDIO = 1064;
    public static final int AUDIO_VIDEO_LOUDSPEAKER = 1044;
    public static final int AUDIO_VIDEO_MICROPHONE = 1040;
    public static final int AUDIO_VIDEO_PORTABLE_AUDIO = 1052;
    public static final int AUDIO_VIDEO_SET_TOP_BOX = 1060;
    public static final int AUDIO_VIDEO_UNCATEGORIZED = 1024;
    public static final int AUDIO_VIDEO_VCR = 1068;
    public static final int AUDIO_VIDEO_VIDEO_CAMERA = 1072;
    public static final int AUDIO_VIDEO_VIDEO_CONFERENCING = 1088;
    public static final int AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER = 1084;
    public static final int AUDIO_VIDEO_VIDEO_GAMING_TOY = 1096;
    public static final int AUDIO_VIDEO_VIDEO_MONITOR = 1080;
    public static final int AUDIO_VIDEO_WEARABLE_HEADSET = 1028;
    public static final int COMPUTER_DESKTOP = 260;
    public static final int COMPUTER_HANDHELD_PC_PDA = 272;
    public static final int COMPUTER_LAPTOP = 268;
    public static final int COMPUTER_PALM_SIZE_PC_PDA = 276;
    public static final int COMPUTER_SERVER = 264;
    public static final int COMPUTER_UNCATEGORIZED = 256;
    public static final int COMPUTER_WEARABLE = 280;
    public static final int HEALTH_BLOOD_PRESSURE = 2308;
    public static final int HEALTH_DATA_DISPLAY = 2332;
    public static final int HEALTH_GLUCOSE = 2320;
    public static final int HEALTH_PULSE_OXIMETER = 2324;
    public static final int HEALTH_PULSE_RATE = 2328;
    public static final int HEALTH_THERMOMETER = 2312;
    public static final int HEALTH_UNCATEGORIZED = 2304;
    public static final int HEALTH_WEIGHING = 2316;
    public static final int PHONE_CELLULAR = 516;
    public static final int PHONE_CORDLESS = 520;
    public static final int PHONE_ISDN = 532;
    public static final int PHONE_MODEM_OR_GATEWAY = 528;
    public static final int PHONE_SMART = 524;
    public static final int PHONE_UNCATEGORIZED = 512;
    public static final int TOY_CONTROLLER = 2064;
    public static final int TOY_DOLL_ACTION_FIGURE = 2060;
    public static final int TOY_GAME = 2068;
    public static final int TOY_ROBOT = 2052;
    public static final int TOY_UNCATEGORIZED = 2048;
    public static final int TOY_VEHICLE = 2056;
    public static final int WEARABLE_GLASSES = 1812;
    public static final int WEARABLE_HELMET = 1808;
    public static final int WEARABLE_JACKET = 1804;
    public static final int WEARABLE_PAGER = 1800;
    public static final int WEARABLE_UNCATEGORIZED = 1792;
    public static final int WEARABLE_WRIST_WATCH = 1796;

    public static String ConvertDeviceClass(int deviceClass){
        switch (deviceClass) {
            case AUDIO_VIDEO_CAMCORDER:
                return "Audio Video Camcorder";
            case AUDIO_VIDEO_CAR_AUDIO:
                return "Audio Video Car Audio";
            case AUDIO_VIDEO_HANDSFREE:
                return "Audio Video Hands Free";
            case AUDIO_VIDEO_HEADPHONES:
                return "Audio Video Headphones";
            case AUDIO_VIDEO_HIFI_AUDIO:
                return "Audio Video Hifi Audio";
            case AUDIO_VIDEO_LOUDSPEAKER:
                return "Audio Video Loudspeaker";
            case AUDIO_VIDEO_MICROPHONE:
                return "Audio Video Microphone";
            case AUDIO_VIDEO_PORTABLE_AUDIO:
                return "Audio Video Portable Audio";
            case AUDIO_VIDEO_SET_TOP_BOX:
                return "Audio Video Set Top Box";
            case AUDIO_VIDEO_UNCATEGORIZED:
                return "Audio Video Uncategorized";
            case AUDIO_VIDEO_VCR:
                return "Audio Video VCR";
            case AUDIO_VIDEO_VIDEO_CAMERA:
                return "Audio Video Video Camera";
            case AUDIO_VIDEO_VIDEO_CONFERENCING:
                return "Audio Video Video Conferencin";
            case AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                return "Audio Video Video Display and Loudspeaker";
            case AUDIO_VIDEO_VIDEO_GAMING_TOY:
                return "Audio Video Video Gaming Toy";
            case AUDIO_VIDEO_VIDEO_MONITOR:
                return "Audio Video Video Monitor";
            case AUDIO_VIDEO_WEARABLE_HEADSET:
                return "Audio Video Wearable Headset";
            case COMPUTER_DESKTOP:
                return "Computer Desktop";
            case COMPUTER_HANDHELD_PC_PDA:
                return "Computer Handled PC PDA";
            case COMPUTER_LAPTOP:
                return "Computer Laptop";
            case COMPUTER_PALM_SIZE_PC_PDA:
                return "Computer Palm Size PC PDA";
            case COMPUTER_SERVER:
                return "Computer Server";
            case COMPUTER_UNCATEGORIZED:
                return "Computer Uncategorized";
            case COMPUTER_WEARABLE:
                return "Computer Wearable";
            case HEALTH_BLOOD_PRESSURE:
                return "Health Blood Pressure";
            case HEALTH_DATA_DISPLAY:
                return "Health Data Display";
            case HEALTH_GLUCOSE:
                return "Health Glucose";
            case HEALTH_PULSE_OXIMETER:
                return "Health Pulse Oximeter";
            case HEALTH_PULSE_RATE:
                return "Health Pulse Rate";
            case HEALTH_THERMOMETER:
                return "Health Thermometer";
            case HEALTH_UNCATEGORIZED:
                return "Health Uncategorized";
            case HEALTH_WEIGHING:
                return "Health Weighing";
            case PHONE_CELLULAR:
                return "Phone Cellular";
            case PHONE_CORDLESS:
                return "Phone Cordless";
            case PHONE_ISDN:
                return "Phone ISDN";
            case PHONE_MODEM_OR_GATEWAY:
                return "Phone Modem or Gateway";
            case PHONE_SMART:
                return "Phone Smart";
            case PHONE_UNCATEGORIZED:
                return "Phone Uncategorized";
            case TOY_CONTROLLER:
                return "Toy Controller";
            case TOY_DOLL_ACTION_FIGURE:
                return "Toy Doll Action Figure";
            case TOY_GAME:
                return "Toy Game";
            case TOY_ROBOT:
                return "Toy Robot";
            case TOY_UNCATEGORIZED:
                return "Toy Uncategorized";
            case TOY_VEHICLE:
                return "Toy Vehicle";
            case WEARABLE_GLASSES:
                return "Wearable Glasses";
            case WEARABLE_HELMET:
                return "Wearable Helmet";
            case WEARABLE_JACKET:
                return "Wearable Jacket";
            case WEARABLE_PAGER:
                return "Wearable Pager";
            case WEARABLE_UNCATEGORIZED:
                return "Wearable Uncategorized";
            case WEARABLE_WRIST_WATCH:
                return "Wearable Wrist Watch";
            default:
                return "Uncategorized";
        }
    };

    public static String ConvertBondState(int bondState){
        switch (bondState) {
            case 10:
                return "Not Bonded";
            case 11:
                return "Bonding";
            case 12:
                return "Bonded";
            default:
                return "Uncategorized";
        }
    };

    public static String ConvertType(int type){
        switch (type) {
            case 1:
                return "Classic (BR/EDR)";
            case 3:
                return "Dual (BR/EDR/LE)";
            case 2:
                return "LE (Low Energy)";
            default:
                return "Uncategorized";
        }
    };
}
