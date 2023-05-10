package me.drakeet.support.toast;

import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * @author drakeet
 */
@SuppressWarnings("unused")
public interface BadTokenListener {

  void onBadTokenCaught(@NonNull Toast toast);

}
