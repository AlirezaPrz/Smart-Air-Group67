package com.example.smartairsetup.notification;

import android.content.Context;
import android.content.Intent;

import com.example.smartairsetup.notification.NotificationReceiver;

public class AlertHelper {

    /**
     * To send alert
     * Just call: NotificationHelper.showAlert(context, type, message)
     */
    public static void showAlert(Context context, String type, String message) {

        String title = selectTitle(type);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TITLE, title);
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(NotificationReceiver.EXTRA_ID, (int) System.currentTimeMillis());

        context.sendBroadcast(intent);
    }
    private static String selectTitle(String type) {
        String title;
        if ("TRIAGE_START".equals(type)) {
            title = "Triage started";
        } else if ("TRIAGE_ESCALATION".equals(type)) {
            title = "Triage escalation";
        } else if ("RED_ZONE".equals(type)) {
            title = "Red-zone day";
        } else if ("RESCUE_REPEATED".equals(type)) {
            title = "Frequent rescue use";
        } else if ("INVENTORY_LOW".equals(type)) {
            title = "Medication inventory low";
        } else if ("WORSE_AFTER_DOSE".equals(type)) {
            title = "Symptoms worse after dose";
        } else {
            title = "SmartAir alert";
        }
        return title;
    }
}
