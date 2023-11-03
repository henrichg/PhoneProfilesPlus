package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class ContactsFilterDialog {

    List<ContactFilter> contactsFilterList;

    private final Activity activity;
    //private final DialogPreference preference;
    private final boolean withoutNumbers;

    private final AlertDialog mDialog;
    final ListView contactsFilterListView;
    final RelativeLayout emptyList;

    private final LinearLayout linlaProgress;
    private final LinearLayout rellaDialog;

    private final ContactsFilterDialogAdapter listAdapter;

    private ShowDialogAsyncTask asyncTask = null;

    ContactsFilterDialog(final Activity activity,
                         final boolean withoutNumbers,
                         final DialogPreference preference) {

        this.activity = activity;
        //this.preference = preference;
        this.withoutNumbers = withoutNumbers;

        contactsFilterList = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.contacts_filter_dialog_filter_title);
        dialogBuilder.setCancelable(true);
        //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        dialogBuilder.setPositiveButton(android.R.string.ok, null);

        dialogBuilder.setOnDismissListener(dialog -> {
            if ((asyncTask != null) && asyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
                asyncTask.cancel(true);
            asyncTask = null;
        });

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_contacts_filter, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

//        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        contactsFilterListView = layout.findViewById(R.id.contacts_filter_dlg_listview);
        emptyList = layout.findViewById(R.id.contacts_filter_dlg_empty);

        linlaProgress = layout.findViewById(R.id.contacts_filter_dlg_linla_progress);
        rellaDialog = layout.findViewById(R.id.contacts_filter_dlg_rella_dialog);

        listAdapter = new ContactsFilterDialogAdapter(activity, this);
        contactsFilterListView.setAdapter(listAdapter);

        contactsFilterListView.setOnItemClickListener((parent, v, position, id) -> {
            ((ContactsMultiSelectDialogPreference) preference).setContactsFilter(contactsFilterList.get(position));
            mDialog.dismiss();
        });

    }


    void show() {
        if (!activity.isFinishing()) {
            mDialog.show();

            asyncTask = new ShowDialogAsyncTask(this, activity);
            asyncTask.execute();
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
                ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                if (contactsCache == null) {
                    // cache not created, create it
                    PPApplicationStatic.createContactsCache(activity.getApplicationContext(), false, false/*, true*/);
                    /*contactsCache = PPApplicationStatic.getContactsCache();
                    while (contactsCache.getCaching())
                        GlobalUtils.sleep(100);*/
                } else {
                    if (!contactsCache.getCaching()) {
                        // caching not performed
                        List<Contact> contactList = contactsCache.getList(/*withoutNumbers*/);
                        if (contactList == null) {
                            // not cached, cache it
                            PPApplicationStatic.createContactsCache(activity.getApplicationContext(), false, false/*, true*/);
                            /*contactsCache = PPApplicationStatic.getContactsCache();
                            while (contactsCache.getCaching())
                                GlobalUtils.sleep(100);*/
                        }
                    } else {
                        // wait for cache end
                        while (contactsCache.getCaching())
                            GlobalUtils.sleep(100);
                    }
                }

                if (contactsCache == null)
                    return null;

                synchronized (PPApplication.contactsCacheMutex) {
                    List<Contact> localContactList = contactsCache.getList(/*withoutNumbers*/);
                    if (localContactList != null) {
                        PPApplicationStatic.logE("[CONTACTS_DIALOG] .getValueCMSDP", "localContactList.size()=" + localContactList.size());

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
                    }
                }

                PackageManager packageManager = activity.getPackageManager();

                for (int id = 0; id < _contactsFilterList.size(); id++) {
                    ContactFilter filter = _contactsFilterList.get(id);
                    boolean found = false;
                    String newFilterName = "";
                    try {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(filter.data, PackageManager.MATCH_ALL);
                        if (applicationInfo != null) {
                            newFilterName = packageManager.getApplicationLabel(applicationInfo).toString();
                            found = true;
                        }
                    } catch (Exception ignored) {
                    }
                    if (!found) {
                        if (filter.data != null) {
                            if (filter.data.equals("com.osp.app.signin"))
                                newFilterName = activity.getString(R.string.contact_account_type_samsung_account);
                            if (filter.data.equals("com.google"))
                                newFilterName = activity.getString(R.string.contact_account_type_google_account);
                            if (filter.data.equals("vnd.sec.contact.sim"))
                                newFilterName = activity.getString(R.string.contact_account_type_sim_card);
                            if (filter.data.equals("vnd.sec.contact.sim2"))
                                newFilterName = activity.getString(R.string.contact_account_type_sim_card);
                            if (filter.data.equals("vnd.sec.contact.phone"))
                                newFilterName = activity.getString(R.string.contact_account_type_phone_application);
                            if (filter.data.equals("org.thoughtcrime.securesms"))
                                newFilterName = "Signal";
                            if (filter.data.equals("com.google.android.apps.tachyon"))
                                newFilterName = "Duo";
                            if (filter.data.equals("com.whatsapp"))
                                newFilterName = "WhatsApp";
                        }
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

                    if (dialog.contactsFilterList.size() == 0) {
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
