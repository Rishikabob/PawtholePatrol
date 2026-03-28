package com.example.pawtholepatrol.utility;

public interface ConfirmationCallback {

    /**
     * Method to see the user response on the inquiry message.
     */
    void onConfirmed(boolean confirm);
}
