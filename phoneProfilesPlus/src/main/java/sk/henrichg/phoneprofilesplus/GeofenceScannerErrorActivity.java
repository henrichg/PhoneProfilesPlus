package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.GoogleApiAvailability;

public class GeofenceScannerErrorActivity extends AppCompatActivity {

    private int dialogError;
    //private static GeofenceScannerErrorActivity activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogError = getIntent().getIntExtra(GeofencesScanner.DIALOG_ERROR, 0);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //activity = this;

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, true/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        showErrorDialog(dialogError);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GeofencesScanner.REQUEST_RESOLVE_ERROR) {
            synchronized (PPApplication.geofenceScannerMutex) {
                if (PhoneProfilesService.getInstance() != null) {
                    if (PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                        PhoneProfilesService.getInstance().getGeofencesScanner().mResolvingError = false;
                    if (resultCode == RESULT_OK) {
                        // Make sure the app is not already connected or attempting to connect
                        if (PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                            PhoneProfilesService.getInstance().getGeofencesScanner().connectForResolve();
                    }
                }
            }
        }
    }

    //------------------------------------------

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        if (!isFinishing()) {
            // Create a fragment for the error dialog
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            // Pass the error that should be displayed
            Bundle args = new Bundle();
            args.putInt(GeofencesScanner.DIALOG_ERROR, errorCode);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "errorDialog");
        }
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    private void onDialogDismissed() {
        synchronized (PPApplication.geofenceScannerMutex) {
            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                PhoneProfilesService.getInstance().getGeofencesScanner().mResolvingError = false;
            finish();
            //activity = null;
        }
    }

    /* A fragment to display an error dialog */
    // Must be public:
    //   java.lang.IllegalStateException: Fragment sk.henrichg.phoneprofilesplus.GeofenceScannerErrorActivity.ErrorDialogFragment
    //   must be a public static class to be  properly recreated from instance state.
    @SuppressWarnings("WeakerAccess")
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
            super();
        }

        //////////
        // Fixed (?) java.lang.RuntimeException: Unable to resume activity {sk.henrichg.phoneprofilesplus/sk.henrichg.phoneprofilesplus.GeofenceScannerErrorActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'void android.app.Dialog.setOwnerActivity(android.app.Activity)' on a null object reference
        // at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3106)
        // (https://github.com/spirosoik/RapidAndroidFramework/commit/02faa0a9e3cd73d7159bad3069313b00a9982b37)
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            if (getDialog() == null ) {
                setShowsDialog( false );
            }
            super.onActivityCreated(savedInstanceState);
        }
        //////////

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = -999;
            if (this.getArguments() != null)
                errorCode = this.getArguments().getInt(GeofencesScanner.DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, GeofencesScanner.REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            if (this.getActivity() != null)
                ((GeofenceScannerErrorActivity) getActivity()).onDialogDismissed();
        }
    }

}
