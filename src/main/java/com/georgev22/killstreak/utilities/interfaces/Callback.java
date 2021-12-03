package com.georgev22.killstreak.utilities.interfaces;

public interface Callback {
    void onSuccess();

    void onFailure(Throwable throwable);
}