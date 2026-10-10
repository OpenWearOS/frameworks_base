package com.android.server.policy;

public abstract class WearModeServiceInternal {
    public abstract boolean onButtonDown(int keyCode);

    public abstract boolean onButtonUp(int keyCode);

    public abstract void onKeyguardOccludedChanged(boolean occluded);

    public abstract boolean shouldInterceptButton(int keyCode);

    public abstract boolean shouldInterceptButtonPressBehavior(int keyCode);
}
