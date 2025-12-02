package com.example.smartairsetup.notification;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.Collections;

public class MyFirebaseService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        //if the parents phone
        String parentUid = FirebaseAuth.getInstance().getUid();
        if (parentUid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(parentUid)
                .collection("device_tokens")
                .document(token)
                .set(Collections.singletonMap("token", token));
    }
}
