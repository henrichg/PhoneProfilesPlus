package me.drakeet.support.toast;

import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * @author drakeet
 */
public interface BadTokenListener {

  @SuppressWarnings("unused")
  void onBadTokenCaught(@NonNull Toast toast);

}
