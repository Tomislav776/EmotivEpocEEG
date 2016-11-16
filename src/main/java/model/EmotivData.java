package model;


import Iedk.Edk;
import Iedk.EdkErrorCode;
import Iedk.EmoState;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import controller.Main;
import controller.MainScreen;

import java.util.Random;

public class EmotivData{

    public double totalAlpha, totalLowBeta, totalHighBeta, totalGamma, totalTheta;
    double totalAlphaSum, totalLowBetaSum, totalHighBetaSum, totalGammaSum, totalThetaSum;
    public double singleAlpha, singleLowBeta, singleHighBeta, singleGamma, singleTheta;
    public static int signalAF3, signalAF4, signalF7, signalF3, signalF4, signalF8, signalFC5, signalFC6, signalT7, signalT8, signalP7, signalP8, signalO1, signalO2, signalCMS, signalDRL;
    public static int batteryStrength, wirelessStrength;

    public static int stateSend;
    public static boolean sendingData=true;

    private boolean zeroSet = true;

    public static DoubleByReference alpha     = new DoubleByReference(0);
    public static DoubleByReference low_beta  = new DoubleByReference(0);
    public static DoubleByReference high_beta = new DoubleByReference(0);
    public static DoubleByReference gamma     = new DoubleByReference(0);
    public static DoubleByReference theta     = new DoubleByReference(0);


    public void epocHeadsetInfo() {
        Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
        Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();
        IntByReference userID = null;
        int state2 = 0;
        boolean onStateChanged = false;
        boolean readytocollect = false;
        boolean ready = false;
        IntByReference batteryLevel = new IntByReference(0);
        IntByReference maxBatteryLevel = new IntByReference(0);

        userID = new IntByReference(0);

        if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") !=
                EdkErrorCode.EDK_OK.ToInt()) {
            System.out.println("Emotiv Engine start up failed.");
            //return;
        }

        while (true) {

            try {
                Thread.sleep(100);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            state2 = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);
            //System.out.println("State " + state2);

            if (state2 == EdkErrorCode.EDK_OK.ToInt()) {
                zeroSet = true;

                int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
                Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);


                switch(eventType)
                {
                    case 0x0010:
                        System.out.println("User added");
                        readytocollect = true;
                        ready = true;
                        break;
                    case 0x0020:
                        System.out.println("User removed");
                        readytocollect = false; 		//just single connection
                        break;
                    case 0x0040:
                        onStateChanged = true;
                        Edk.INSTANCE.IEE_EmoEngineEventGetEmoState(eEvent, eState);
                        break;
                    default:
                        break;
                }


                if (ready) {

                    int i = 0;
                    String choice = MainScreen.choiceEmotivData;

                    // 01 = Pz
                    switch (choice) {
                        case "AF3" :  i = 3; break;
                        case "F7" :  i = 4; break;
                        case "F3" :  i = 5; break;
                        case "FC5" :  i = 6; break;
                        case "T7" :  i = 7; break;
                        case "P7" :  i = 8; break;
                        case "Pz" :  i = 9; break;
                        case "O1" :  i = 9; break;
                        case "O2" :  i = 10; break;
                        case "P8" :  i = 11; break;
                        case "T8" :  i = 12; break;
                        case "FC6" :  i = 13; break;
                        case "F4" :  i = 14; break;
                        case "F8" :  i = 15; break;
                        case "AF4" :  i = 16; break;
                        case "Total" :  i = 0; break;
                        default: break;
                    }

                    if (i == 0){
                        totalAlphaSum = totalLowBetaSum = totalHighBetaSum = totalGammaSum = totalThetaSum = 0;
                        for(int j = 3 ; j < 17 ; j++)
                        {
                            int result = Edk.INSTANCE.IEE_GetAverageBandPowers(userID.getValue(), j, theta, alpha, low_beta, high_beta, gamma);
                            if(result == EdkErrorCode.EDK_OK.ToInt()){
                                totalAlphaSum += alpha.getValue();
                                totalLowBetaSum += low_beta.getValue();
                                totalHighBetaSum += high_beta.getValue();
                                totalGammaSum += gamma.getValue();
                                totalThetaSum += theta.getValue();

                            }
                        }

                        totalAlpha = totalAlphaSum / 14;
                        totalLowBeta = totalLowBetaSum / 14;
                        totalHighBeta = totalHighBeta / 14;
                        totalGamma = totalGamma / 14;
                        totalTheta = totalTheta / 14;
                    }
                    else {
                        int result = Edk.INSTANCE.IEE_GetAverageBandPowers(userID.getValue(), i, theta, alpha, low_beta, high_beta, gamma);
                        if(result == EdkErrorCode.EDK_OK.ToInt()){
                            singleAlpha = alpha.getValue();
                            singleGamma = gamma.getValue();
                            singleHighBeta = high_beta.getValue();
                            singleLowBeta = low_beta.getValue();
                            singleTheta = theta.getValue();
                        }
                    }


                }


                if (readytocollect && onStateChanged)
                {
                    wirelessStrength = EmoState.INSTANCE.IS_GetWirelessSignalStatus(eState);

                    EmoState.INSTANCE.IS_GetBatteryChargeLevel(eState, batteryLevel, maxBatteryLevel);
                    batteryStrength = batteryLevel.getValue();

                    signalAF3 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_AF3.getType());
                    signalAF4 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_AF4.getType());
                    signalF7 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_F7.getType());
                    signalF3 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_F3.getType());
                    signalF4 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_F4.getType());
                    signalF8 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_F8.getType());
                    signalFC5 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_FC5.getType());
                    signalFC6 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_FC6.getType());
                    signalT7 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_T7.getType());
                    signalT8 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_T8.getType());
                    signalP7 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_P7.getType());
                    signalP8 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_P8.getType());
                    signalO1 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_O1.getType());
                    signalO2 = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_O2.getType());
                    signalCMS = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_CMS.getType());
                    signalDRL = EmoState.INSTANCE.IS_GetContactQuality(eState, EmoState.IEE_InputChannels_t.IEE_CHAN_DRL.getType());

                }
            } else if (state2 != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
                System.out.println("Internal error in Emotiv Engine!");
                break;
            } else if (state2 == EdkErrorCode.EDK_NO_EVENT.ToInt() && zeroSet){
               setToZero();
            }
        }

        Edk.INSTANCE.IEE_EngineDisconnect();
        System.out.println("Disconnected!");
        /*
        Edk.INSTANCE.IEE_EngineDisconnect();
        Edk.INSTANCE.IEE_EmoStateFree(eState);
        Edk.INSTANCE.IEE_EmoEngineEventFree(eEvent);
        System.out.println("Disconnected!");*/
    }

    public void setToZero (){
        zeroSet = false;

        //batteryStrength = 0;
        //wirelessStrength = 0;

        singleAlpha = 0;
        singleGamma = 0;
        singleHighBeta = 0;
        singleLowBeta = 0;
        singleTheta = 0;

        totalAlpha = 0;
        totalLowBeta = 0;
        totalHighBeta = 0;
        totalGamma = 0;
        totalTheta = 0;

        signalAF3 = 0;
        signalAF4 = 0;
        signalF7 = 0;
        signalF3 = 0;
        signalF4 = 0;
        signalF8 = 0;
        signalFC5 =0;
        signalFC6 = 0;
        signalT7 = 0;
        signalT8 = 0;
        signalP7 = 0;
        signalP8 = 0;
        signalO1 = 0;
        signalO2 = 0;
        signalCMS = 0;
        signalDRL = 0;
    }

}
