package com.android.server.policy;

public abstract class WearPowerServiceInputInterceptorInternal {
    public abstract boolean interceptInteractiveMotion(int deviceId, int source, long eventTimeNanos);

    public abstract boolean interceptWakeAttempt(int deviceId, int source, long eventTimeNanos,
            boolean sleepingOrGoingToSleep);

    public abstract void onKeyEvent(int keyCode, boolean down);

    public abstract void onWakeFromMotion(int deviceId, int source, long eventTimeNanos,
            int displayStateBeforeWake);
}
