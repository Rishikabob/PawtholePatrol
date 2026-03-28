package com.example.pawtholepatrol.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventConfirmationHelper {

    // ID of the notification used for the inquiry
    public static final int INQUIRY_ID = 117;

    // ID of the channel that handles the inquiry notifications
    private static final String CHANNEL_ID = "confirmation_channel";

    // Timeout of the inquiry notification before it automatically accepts "No". Similar to the
    // Waze notifications that timeout when asking about a road condition.
    private static final long TIMEOUT_MS = 10000;   // 10 seconds

    private static ConfirmationCallback pendingCallback;
    private static TextToSpeech tts;
    private static SpeechRecognizer speechRecognizer;
    private static Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static AtomicBoolean resultDelivered = new AtomicBoolean(false);

    private static final String LOG_TAG = "EventConfirmationHelper";

    /**
     * Initialization of inquiry utility, the text to speech set to only support American English.
     * Initialization is called upon in the MainActivity file.
     */
    public static void init(Context context) {
        createNotificationChannel(context);

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);     // Possible future work: multiple languages
            }
        });
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * CALL THIS METHOD TO ASK THE USER AN INQUIRY
     *
     * Method sends the notification and receives the user's answer, whether the user actually answers
     * or the notification times out.
     *
     * Example call in another class:
     *
     * EventConfirmationHelper.askForConfirmation(
     *      context,
     *      "Was there a pothole?",
     *      confirmed -> {
     *          if (confirmed) {
     *              // Code if the user said yes
     *          } else {
     *              // Code if the user said no or timed out
     *          }
     *      }
     * );
     * ---------------------------------------------------------------------------------------------
     */
    public static void askForConfirmation(Context context,
                                          String inquiry,
                                          ConfirmationCallback callback) {
        pendingCallback = callback;
        resultDelivered.set(false);

        showConfirmationNotification(context, inquiry);
        speakAndListen(context, inquiry);
        startTimeout();
    }

    /**
     * Deliver the result for the inquiry notification whether it was called by the button
     * confirmation receiver, the text to speech, or by the timeout.
     */
    public static void deliverResult(boolean confirmed) {
        // Guard to only register the first response from the three methods
        if (resultDelivered.getAndSet(true)) {
            Log.d(LOG_TAG, "Already received a response for the user");
            return;
        }

        // Kill the timeout handler
        timeoutHandler.removeCallbacksAndMessages(null);

        // Check the speech to text response
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();   // Possible talking point - app only listens during the notification uptime
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (pendingCallback != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                pendingCallback.onConfirmed(confirmed);
                pendingCallback = null;
            });
        }
    }

    /**
     * Cleans up all supporting services on application shutdown.
     * Is called upon in the MainActivity file.
     */
    public static void shutdown() {
        // Clean up text-to-speech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        // Clean up the timeout handler for the inquiry notifications
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Set up the channel that will handle the inquiry notifications.
     */
    private static void createNotificationChannel(Context context) {
        // Need to check Android OS (notification channels were introduced at v26, current project minimum SDK is v24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Inquiry Confirmations", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Asks user to confirm if pothole exists");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        } else {
            Log.e(LOG_TAG, "Android SDK version is less than the required minimum: v26");
        }
    }

    /**
     * Setting up the visual inquiry notification.
     */
    private static void showConfirmationNotification(Context context, String inquiry) {
        // Set up the yes response
        Intent yesIntent = new Intent(context, ConfirmationReceiver.class);
        yesIntent.setAction(ConfirmationReceiver.PRESS_YES);
        PendingIntent yesPending = PendingIntent.getBroadcast(
                context,
                0,
                yesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set up the no response
        Intent noIntent = new Intent(context, ConfirmationReceiver.class);
        noIntent.setAction(ConfirmationReceiver.PRESS_NO);
        PendingIntent noPending = PendingIntent.getBroadcast(
                context,
                1,
                noIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set up the layout and functions of the notification itself
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Confirm Pothole")
                .setContentText(inquiry)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Setting it to high makes it a heads-up notification
                .setCategory(NotificationCompat.CATEGORY_CALL)  // Call makes it an overlay
                .addAction(android.R.drawable.ic_menu_send, "Yes ✓", yesPending)
                .addAction(android.R.drawable.ic_delete, "No x", noPending)
                .setAutoCancel(false)
                .setOngoing(true);  // Prevents swiping the notification away

        // Display the inquiry notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(INQUIRY_ID, builder.build());
    }

    /**
     * Starts the handler to control the timeout of the notification, and if it times out, taking
     * the user response as a no.
     */
    private static void startTimeout() {
        timeoutHandler.postDelayed(() -> deliverResult(false), TIMEOUT_MS);
    }

    /**
     * Handles the audio portion of the inquiry notification. It will speak out the inquiry message
     * and then listen for the user to respond with "yes" or "no".
     */
    private static void speakAndListen(Context context, String inquiry) {
        // Speak the inquiry prompt
        tts.speak(inquiry, TextToSpeech.QUEUE_FLUSH, null, "TTS_DONE");

        // Wait for the text to speech to finish then listen for the answer
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override
            public void onStart(String id) {}

            @Override
            public void onDone(String id) {
                if ("TTS_DONE".equals(id)) {
                    new Handler(Looper.getMainLooper()).post(() -> startListening(context));
                }
            }

            @Override
            public void onError(String id) {}
        });
    }

    /**
     * Handles the speech to text, looking for the matching "yes" or "no" phrase from the user.
     */
    private static void startListening(Context context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onBeginningOfSpeech() {}
            @Override public void onBufferReceived(byte[] bytes) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int i) {}
            @Override public void onEvent(int i, Bundle bundle) {}
            @Override public void onPartialResults(Bundle bundle) {}
            @Override public void onReadyForSpeech(Bundle bundle) {}

            /**
             * Actually going through the returned speech to look for a match to "yes" or "no".
             *
             * Note: The way this method behaves, the first instance of a matching answer will be
             *       used. A possible enhancement could be a more advanced speech to text
             *       algorithm/method.
             */
            @Override
            public void onResults(Bundle bundle) {
                List<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String match : matches) {
                        String word = match.toLowerCase().trim();
                        if (word.contains("yes")) {
                            deliverResult(true);
                            return;
                        } else if (word.contains("no")) {
                            deliverResult(false);
                            return;
                        }
                    }
                }

                // Nothing was recognized so it will fall to the timeout handler
                Log.w(LOG_TAG, "No recognizable answer was observed, defaulting to timeout");
            }

            @Override public void onRmsChanged(float v) {}
        });
    }

}
