package com.ediattah.yakko.Service;

/**
 * Created by ujs on 17/10/2017.
 */

public interface OnBotResult {
    void onTaskCompleted();
    void onTaskStarted();
    void onTaskUpdated(String... response);
    void onTaskError(String result);
}
