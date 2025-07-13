package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContactsFilterDialog extends DialogFragment {

    List<ContactFilter> contactsFilterList;

    private AppCompatActivity activity;
    private boolean withoutNumbers;

    private AlertDialog mDialog;
    private ListView contactsFilterListView;
    private RelativeLayout emptyList;
    private DialogPreference preference;

    private ContactsFilterDialog fragment;

    private LinearLayout linlaProgress;
    private LinearLayout rellaDialog;

    private ContactsFilterDialogAdapter listAdapter;

    private ShowDialogAsyncTask asyncTask = null;

    public ContactsFilterDialog() {
    }

    public ContactsFilterDialog(final AppCompatActivity activity,
                         final boolean withoutNumbers,
                         final DialogPreference preference) {

        this.activity = activity;
        this.preference = preference;
        this.withoutNumbers = withoutNumbers;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if ((activity != null) && (preference != null)) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            fragment = this;

            contactsFilterList = new ArrayList<>();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.contacts_filter_dialog_filter_title), null);
            //dialogBuilder.setTitle(R.string.contacts_filter_dialog_filter_title);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            dialogBuilder.setPositiveButton(android.R.string.ok, null);

//            dialogBuilder.setOnDismissListener(dialog -> {
//                if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
//                    asyncTask.cancel(true);
//                asyncTask = null;
//            });

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_contacts_filter, null);
            dialogBuilder.setView(layout);

            mDialog = dialogBuilder.create();

            mDialog.setOnShowListener(dialog -> {
                asyncTask = new ShowDialogAsyncTask(fragment, activity);
                asyncTask.execute();
            });

            contactsFilterListView = layout.findViewById(R.id.contacts_filter_dlg_listview);
            emptyList = layout.findViewById(R.id.contacts_filter_dlg_empty);

            linlaProgress = layout.findViewById(R.id.contacts_filter_dlg_linla_progress);
            rellaDialog = layout.findViewById(R.id.contacts_filter_dlg_rella_dialog);

            listAdapter = new ContactsFilterDialogAdapter(activity, this);
            //noinspection DataFlowIssue
            contactsFilterListView.setAdapter(listAdapter);

            contactsFilterListView.setOnItemClickListener((parent, v, position, id) -> {
                ((ContactsMultiSelectDialogPreference) preference).setContactsFilter(contactsFilterList.get(position));
                dismiss();
            });

        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            asyncTask.cancel(true);
        asyncTask = null;

        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "CONTACTS_FILTER_DIALOG");
        }
    }

    private static class ShowDialogAsyncTask extends AsyncTask<Void, Integer, Void> {

        final List<ContactFilter> _contactsFilterList = new ArrayList<>();

        private final WeakReference<ContactsFilterDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakReference;

        public ShowDialogAsyncTask(ContactsFilterDialog dialog,
                                   Activity activity) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ContactsFilterDialog dialog = dialogWeakRef.get();
            if (dialog != null) {
                dialog.rellaDialog.setVisibility(View.GONE);
                dialog.linlaProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContactsFilterDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
//                PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "PPApplicationStatic.getContactsCache()");
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null) {
                    // cache not created, create it
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "PPApplicationStatic.createContactsCache()");
                    PPApplicationStatic.createContactsCache(activity.getApplicationContext(), false, false/*, true*/, false);
                    /*contactsCache = PPApplicationStatic.getContactsCache();
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "contactsCache.getCaching()");
                    if (!contactsCache.getCaching()) {
                        // caching not performed
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "contactsCache.getList()");
                        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
                        if (contactList == null) {
                            // not cached, cache it
//                            PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "PPApplicationStatic.createContactsCache()");
                            PPApplicationStatic.createContactsCache(activity.getApplicationContext(), false, false/*, true*/, false);
                            /*contactsCache = PPApplicationStatic.getContactsCache();
                            while (contactsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        }
                        else
                            contactList.clear();
                    } else {
                        // wait for cache end
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "contactsCache.getCaching()");
                        while (contactsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
                }

                if (contactsCache == null)
                    return null;

//                PPApplicationStatic.logE("[SYNCHRONIZED] ContactsFilterDialog.ShowDialogAsyncTask", "PPApplication.contactsCacheMutex");
                synchronized (PPApplication.contactsCacheMutex) {
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] ContactsFilterDialog.doInBackground", "contactsCache.getList()");
                    List<Contact> localContactList = contactsCache.getList(/*withoutNumbers*/);
                    if (localContactList != null) {
                        for (Contact contact : localContactList) {
                            if (dialog.withoutNumbers || (contact.phoneId != 0)) {
                                boolean found = false;
                                for (ContactFilter filter : _contactsFilterList) {
                                    if (filter.data.equals(contact.accountType)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    ContactFilter filter = new ContactFilter();
                                    filter.data = contact.accountType;
                                    _contactsFilterList.add(filter);
                                }
                            }
                        }
                        localContactList.clear();
                    }
                }

                PackageManager packageManager = activity.getPackageManager();

                for (int id = 0; id < _contactsFilterList.size(); id++) {
                    ContactFilter filter = _contactsFilterList.get(id);
                    boolean found = false;
                    String newFilterName = "";
                    try {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(filter.data, PackageManager.MATCH_ALL);
                        //if (applicationInfo != null) {
                            newFilterName = packageManager.getApplicationLabel(applicationInfo).toString();
                            found = true;
                        //}
                    } catch (Exception ignored) {
                    }
                    if (!found) {
                        if (filter.data != null)
                            newFilterName = ContactsCache.getAccountName(filter.data, activity);
                    }
                    if (newFilterName.isEmpty())
                        newFilterName = filter.data;

                    filter.displayName = newFilterName;
                    _contactsFilterList.set(id, filter);
                }
                _contactsFilterList.sort(new ContactsFilterComparator());

                ContactFilter fillterAll = new ContactFilter();
                fillterAll.data = StringConstants.CONTACTS_FILTER_DATA_ALL;
                fillterAll.displayName = activity.getString(R.string.contacts_filter_dialog_item_show_all);
                _contactsFilterList.add(0, fillterAll);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ContactsFilterDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(activity.getMainLooper());
                handler.post(() -> {
                    dialog.rellaDialog.setVisibility(View.VISIBLE);

                    dialog.contactsFilterList = new ArrayList<>(_contactsFilterList);

                    if (dialog.contactsFilterList.isEmpty()) {
                        dialog.contactsFilterListView.setVisibility(View.GONE);
                        dialog.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        dialog.emptyList.setVisibility(View.GONE);
                        dialog.contactsFilterListView.setVisibility(View.VISIBLE);
                    }

                    dialog.listAdapter.notifyDataSetChanged();
                });
            }
        }

    }

    private static class ContactsFilterComparator implements Comparator<ContactFilter> {

        public int compare(ContactFilter lhs, ContactFilter rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs.displayName, rhs.displayName);
            else
                return 0;
        }
    }

}
