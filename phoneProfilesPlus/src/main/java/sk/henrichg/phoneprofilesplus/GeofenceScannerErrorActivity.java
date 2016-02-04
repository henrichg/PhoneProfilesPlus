package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.GoogleApiAvailability;

public class GeofenceScannerErrorActivity extends AppCompatActivity {

    private int dialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        dialogError = getIntent().getIntExtra(GeofencesScanner.DIALOG_ERROR, 0);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        showErrorDialog(dialogError);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GeofencesScanner.REQUEST_RESOLVE_ERROR) {
            GlobalData.geofencesScanner.mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                GlobalData.geofencesScanner.connectForResolve();
            }
        }
    }

    //------------------------------------------

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(GeofencesScanner.DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        GlobalData.geofencesScanner.mResolvingError = false;
        finish();
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(GeofencesScanner.DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, GeofencesScanner.REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((GeofenceScannerErrorActivity) getActivity()).onDialogDismissed();
        }
    }

}
