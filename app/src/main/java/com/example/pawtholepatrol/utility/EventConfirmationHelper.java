package com.example.pawtholepatrol.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.pawtholepatrol.AppPreferences;
import com.example.pawtholepatrol.service.OverlayService;

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
    private static final long TIMEOUT_MS = 10000;   // 10 seconds - Possible user study point on length of time

    private static ConfirmationCallback pendingCallback;

    private static Context appContext;

    private static TextToSpeech tts;
    private static SpeechRecognizer speechRecognizer;

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
        appContext = context;
        pendingCallback = callback;
        resultDelivered.set(false);

        boolean visualEnabled = AppPreferences.INSTANCE.isInquiryVisualEnabled(context);
        boolean audioEnabled = AppPreferences.INSTANCE.isInquiryAudioEnabled(context);

        if (visualEnabled) {
            // Launch the WindowManager overlay service instead of a notification
            Intent overlayIntent = new Intent(appContext, OverlayService.class);
            overlayIntent.putExtra("question", inquiry);
            overlayIntent.putExtra("timeout_ms", TIMEOUT_MS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(overlayIntent);
            }
        }

        if (audioEnabled) {
            // Keep audio — it works independently of the visual
            speakAndListen(context, inquiry);

            if (!visualEnabled) {
                // No overlay to handle the timeout, so do it manually
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        deliverResult(appContext, false), TIMEOUT_MS);
            }
        }
    }

    /**
     * Deliver the result for the inquiry notification whether it was called by the button
     * confirmation receiver, the text to speech, or by the timeout.
     */
    public static void deliverResult(Context context, boolean confirmed) {
        if (resultDelivered.getAndSet(true)) {
            Log.d(LOG_TAG, "Already received a response for the user");
            return;
        }

        // Stop the overlay service if it's still showing
        context.stopService(new Intent(context, OverlayService.class));

        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
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
    }

    /**
     * Set up the channel that will handle the inquiry notifications.
     */
    private static void createNotificationChannel(Context context) {
        // Need to check Android OS (notification channels were introduced at v26, current project minimum SDK is v24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel silentChannel = new NotificationChannel(
                    "overlay_service_channel",
                    "Overlay Service",
                    NotificationManager.IMPORTANCE_MIN  // No sound, no peek, no icon in status bar
            );
            silentChannel.setShowBadge(false);


            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(silentChannel);
        } else {
            Log.e(LOG_TAG, "Android SDK version is less than the required minimum: v26");
        }
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
                            deliverResult(appContext, true);
                            return;
                        } else if (word.contains("no")) {
                            deliverResult(appContext, false);
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
