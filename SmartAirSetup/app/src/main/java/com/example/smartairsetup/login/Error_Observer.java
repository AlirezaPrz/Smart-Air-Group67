package com.example.smartairsetup.login;

public interface Error_Observer {
    public interface ErrorCallback {
        void onError(String errorMessage);
    }
}
