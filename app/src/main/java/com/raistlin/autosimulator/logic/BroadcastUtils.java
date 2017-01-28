package com.raistlin.autosimulator.logic;

import android.content.Intent;

public class BroadcastUtils {

    /**
     * Создать уведомление о проехавшей машине
     */
    public static Intent buildAutoDoneIntent() {
        Intent result = new Intent(CoreConst.BROADCAST_INTENT);
        result.putExtra(CoreConst.BROADCAST_TYPE, CoreConst.BROADCAST_AUTO_DONE);
        return result;
    }

    /**
     * Создать уведомление о новой машине
     */
    public static Intent buildAutoCreatedIntent() {
        Intent result = new Intent(CoreConst.BROADCAST_INTENT);
        result.putExtra(CoreConst.BROADCAST_TYPE, CoreConst.BROADCAST_AUTO_CREATED);
        return result;
    }

    /**
     * Создать уведомление о аварии
     */
    public static Intent buildCrashIntent() {
        Intent result = new Intent(CoreConst.BROADCAST_INTENT);
        result.putExtra(CoreConst.BROADCAST_TYPE, CoreConst.BROADCAST_CRASH);
        return result;
    }

    /**
     * Создать уведомление о принудительном торможении
     */
    public static Intent buildAutoForceStopIntent() {
        Intent result = new Intent(CoreConst.BROADCAST_INTENT);
        result.putExtra(CoreConst.BROADCAST_TYPE, CoreConst.BROADCAST_FORCE_STOPS);
        return result;
    }
}
