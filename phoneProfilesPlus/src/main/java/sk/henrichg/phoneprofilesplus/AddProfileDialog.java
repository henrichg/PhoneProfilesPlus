package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddProfileDialog extends DialogFragment
{
    private EditorProfileListFragment profileListFragment;

    private AlertDialog mDialog;
    private EditorActivity activity;

    private LinearLayout linlaProgress;
    private ListView listView;

    private final List<Profile> profileList = new ArrayList<>();

    private GetProfilesAsyncTask getProfilesAsyncTask = null;

    public AddProfileDialog() {
    }

    public AddProfileDialog(EditorActivity activity/*, EditorProfileListFragment profileListFragment*/)
    {
        //this.profileListFragment = profileListFragment;
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (EditorActivity) getActivity();
        if (this.activity != null) {
            this.profileListFragment = (EditorProfileListFragment) activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.new_profile_predefined_profiles_dialog), null);
            //dialogBuilder.setTitle(R.string.new_profile_predefined_profiles_dialog);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_profile_preference, null);
            dialogBuilder.setView(layout);

            mDialog = dialogBuilder.create();

            mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

                doShow();
            });

            linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

            listView = layout.findViewById(R.id.profile_pref_dlg_listview);

            //noinspection DataFlowIssue
            listView.setOnItemClickListener((parent, item, position, id) -> {
                AddProfileViewHolder viewHolder = (AddProfileViewHolder) item.getTag();
                if (viewHolder != null)
                    viewHolder.radioButton.setChecked(true);
                final Handler handler = new Handler(activity.getMainLooper());
                final WeakReference<AddProfileDialog> dialogWeakRef = new WeakReference<>(this);
                handler.postDelayed(() -> {
                    AddProfileDialog dialog1 = dialogWeakRef.get();
                    if (dialog1 != null)
                        dialog1.doOnItemSelected(position);
                }, 200);
            });

        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if ((getProfilesAsyncTask != null) &&
                getProfilesAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            getProfilesAsyncTask.cancel(true);
        }
        getProfilesAsyncTask = null;
        this.profileListFragment = null;
    }

    private void doShow() {
        getProfilesAsyncTask = new AddProfileDialog.GetProfilesAsyncTask(this, activity, profileListFragment.activityDataWrapper);
        getProfilesAsyncTask.execute();
    }

    void doOnItemSelected(int position)
    {
        if (profileListFragment != null)
            profileListFragment.startProfilePreferencesActivity(null, position);

        //noinspection ForLoopReplaceableByForEach
        for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
            Profile profile = it.next();
            profile.releaseIconBitmap();
            profile.releasePreferencesIndicator();
        }
        //}
        profileList.clear();

        dismiss();
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            //mDialog.show();
            show(activity.getSupportFragmentManager(), "ADD_PROFILE_DIALOG");
    }

    private static class GetProfilesAsyncTask extends AsyncTask<Void, Integer, Void> {

        final List<Profile> _profileList = new ArrayList<>();

        private final WeakReference<AddProfileDialog> dialogWeakRef;
        private final WeakReference<EditorActivity> activityWeakRef;
        final DataWrapper dataWrapper;

        public GetProfilesAsyncTask(final AddProfileDialog dialog,
                                  final EditorActivity activity,
                                  final DataWrapper dataWrapper) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakRef = new WeakReference<>(activity);
            this.dataWrapper = dataWrapper.copyDataWrapper();
        }

        /*@Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //listView.setVisibility(View.GONE);
            //linlaProgress.setVisibility(View.VISIBLE);
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
                Profile profile;
                profile = DataWrapperStatic.getNonInitializedProfile(
                        activity.getString(R.string.profile_name_default),
                        StringConstants.PROFILE_ICON_DEFAULT, 0);
                profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                if (applicationEditorPrefIndicator)
                    profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF, DataWrapper.IT_FOR_EDITOR, 0f);
                _profileList.add(profile);
                for (int index = 0; index < 7; index++) {
                    profile = dataWrapper.getPredefinedProfile(index, false, activity);
                    profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                    if (applicationEditorPrefIndicator)
                        profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF, DataWrapper.IT_FOR_EDITOR, 0f);
                    _profileList.add(profile);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            AddProfileDialog dialog = dialogWeakRef.get();
            EditorActivity activity = activityWeakRef.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);
                dialog.listView.setVisibility(View.VISIBLE);

                dialog.profileList.clear();
                dialog.profileList.addAll(_profileList);

                AddProfileAdapter addProfileAdapter = new AddProfileAdapter(dialog, activity, dialog.profileList);
                dialog.listView.setAdapter(addProfileAdapter);
            }
        }

    }

}
