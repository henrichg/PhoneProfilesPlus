package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public class PPRecognitionServiceTrampoline extends RecognitionService {
    //private static final String TAG = "RSTrampoline";

    public final ConcurrentHashMap<Object, Object> recognizerMap =
            new ConcurrentHashMap<>();

    public PPRecognitionServiceTrampoline() {
    }

    @Override
    protected void onStartListening(Intent intent, Callback callback) {
        if (!recognizerMap.containsKey(callback) || recognizerMap.get(callback) == null) {
            SpeechRecognizer speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(
                            getApplicationContext(),
                            new ComponentName(getRSPackageName(), getRecognitionServiceName()));
            speechRecognizer.setRecognitionListener(createRecognitionListener(callback));
            recognizerMap.put(callback, speechRecognizer);
        }
        SpeechRecognizer speechRecognizer = (SpeechRecognizer) recognizerMap.get(callback);
        if (speechRecognizer != null)
            speechRecognizer.startListening(intent);
    }

    private String getRSPackageName() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return "com.google.android.tts";
        } else {
            return "com.google.android.googlequicksearchbox";
        }
    }

    private String getRecognitionServiceName() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return "com.google.android.apps.speech.tts.googletts.service.GoogleTTSRecognitionService";
        } else {
            return "com.google.android.voicesearch.serviceapi.GoogleRecognitionService";
        }
    }

    @Override
    protected void onCancel(Callback callback) {
        SpeechRecognizer speechRecognizer = (SpeechRecognizer) recognizerMap.remove(callback);
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
    }

    @Override
    protected void onStopListening(Callback callback) {
        SpeechRecognizer speechRecognizer = (SpeechRecognizer) recognizerMap.get(callback);
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    private RecognitionListener createRecognitionListener(RecognitionService.Callback callback) {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                logIfThrows(() -> callback.readyForSpeech(params));
            }

            @Override
            public void onBeginningOfSpeech() {
                logIfThrows(callback::beginningOfSpeech);
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                logIfThrows(() -> callback.rmsChanged(rmsdB));
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                logIfThrows(() -> callback.bufferReceived(buffer));
            }

            @Override
            public void onEndOfSpeech() {
                logIfThrows(callback::endOfSpeech);
            }

            @Override
            public void onError(int error) {
                logIfThrows(() -> callback.error(error));
            }

            @Override
            public void onResults(Bundle results) {
                logIfThrows(() -> callback.results(results));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                logIfThrows(() -> callback.partialResults(partialResults));
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }

            private void logIfThrows(RemoteExceptionRunnable runnable) {
                try {
                    runnable.run();
                } catch (RemoteException e) {
                    Log.e("PPRecognitionServiceTrampoline.createRecognitionListener", Log.getStackTraceString(e));
                }
            }
        };
    }

    private interface RemoteExceptionRunnable {
        void run() throws RemoteException;
    }
}