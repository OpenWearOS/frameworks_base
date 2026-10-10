package com.android.server.policy;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.KeyEvent;

import com.android.server.LocalServices;

public class WearWindowManagerPolicy {
    public static final String ACTION_SHOW_COOLDOWN_DIALOG = "org.openwearos.action.SHOW_COOLDOWN_DIALOG";
    private static final String SETTING_COOLDOWN = "cooldown_mode_on";
    private static final String PROP_TTW = "persist.sys.wear.ttw_optimization";

    private static final int POWER = KeyEvent.KEYCODE_POWER;
    private static final int STEM_PRIMARY = KeyEvent.KEYCODE_STEM_PRIMARY;

    private final Context mContext;

    public WearWindowManagerPolicy(Context context) {
        mContext = context;
    }

    private static WearModeServiceInternal modes() {
        return LocalServices.getService(WearModeServiceInternal.class);
    }

    private static WearPowerServiceInputInterceptorInternal power() {
        return LocalServices.getService(WearPowerServiceInputInterceptorInternal.class);
    }

    private static boolean modesWant(int key) {
        WearModeServiceInternal m = modes();
        return m != null && m.shouldInterceptButton(key);
    }

    private static boolean modesWantBehavior(int key) {
        WearModeServiceInternal m = modes();
        return m != null && m.shouldInterceptButtonPressBehavior(key);
    }

    private boolean cooldown() {
        return Settings.Global.getInt(mContext.getContentResolver(), SETTING_COOLDOWN, 0) == 1;
    }

    private void showCooldownHelp() {
        mContext.sendBroadcastAsUser(new Intent(ACTION_SHOW_COOLDOWN_DIALOG), UserHandle.CURRENT_OR_SELF);
    }

    private static void tellPower(int key, boolean down) {
        WearPowerServiceInputInterceptorInternal p = power();
        if (p != null) p.onKeyEvent(key, down);
    }

    public boolean hasPowerLongPressBehaviour() {
        return modesWant(POWER) || cooldown();
    }

    public boolean interceptPowerKeyDown() {
        tellPower(POWER, true);
        if (modesWant(POWER)) return modes().onButtonDown(POWER);
        return cooldown();
    }

    public void onPowerKeyUp() {
        tellPower(POWER, false);
        if (modesWant(POWER)) modes().onButtonUp(POWER);
        else if (cooldown()) showCooldownHelp();
    }

    public boolean interceptPowerSinglePress() {
        return modesWant(POWER) || modesWantBehavior(POWER) || cooldown();
    }

    public boolean interceptPowerLongPress() {
        return interceptPowerSinglePress();
    }

    public boolean interceptPowerVeryLongPress() {
        return modesWantBehavior(POWER);
    }

    public boolean interceptStemPrimaryPressAction() {
        boolean modes = modesWant(STEM_PRIMARY);
        if (!modes && !modesWantBehavior(STEM_PRIMARY) && !cooldown()) return false;
        if (modes) modes().onButtonDown(STEM_PRIMARY);
        if (cooldown()) showCooldownHelp();
        return true;
    }

    public void onStemPrimaryKeyEvent(boolean down) {
        tellPower(STEM_PRIMARY, down);
    }

    public boolean interceptKeyEventWhenNonInteractive(int keyCode) {
        if (keyCode == STEM_PRIMARY) {
            if (cooldown()) {
                showCooldownHelp();
                return true;
            }
            if (modesWant(keyCode)) return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case POWER:
            case KeyEvent.KEYCODE_STEM_1:
            case KeyEvent.KEYCODE_STEM_2:
            case KeyEvent.KEYCODE_STEM_3:
                return true;
            default:
                return false;
        }
    }

    public boolean interceptInteractiveMotion(int deviceId, int source, long timeNanos) {
        WearPowerServiceInputInterceptorInternal p = power();
        return p != null && p.interceptInteractiveMotion(deviceId, source, timeNanos);
    }

    public boolean interceptWakeAttempt(int deviceId, int source, long timeNanos, boolean sleeping) {
        WearPowerServiceInputInterceptorInternal p = power();
        return p != null && p.interceptWakeAttempt(deviceId, source, timeNanos, sleeping);
    }

    public void onWakeFromMotion(int deviceId, int source, long timeNanos, int displayStateBefore) {
        WearPowerServiceInputInterceptorInternal p = power();
        if (p != null) p.onWakeFromMotion(deviceId, source, timeNanos, displayStateBefore);
    }

    public boolean shouldDispatchWakeUpMotionEvent() {
        return SystemProperties.getBoolean(PROP_TTW, false);
    }

    public void onKeyguardOccludedChanged(boolean occluded) {
        WearModeServiceInternal m = modes();
        if (m != null) m.onKeyguardOccludedChanged(occluded);
    }
}
