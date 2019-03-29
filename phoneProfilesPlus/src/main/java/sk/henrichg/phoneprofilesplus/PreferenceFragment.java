package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class PreferenceFragment extends android.preference.PreferenceFragment {

    abstract int addPreferencesFromResource();

    private static final HashMap<String, PreferenceScreen> preferenceScreenHashMap = new HashMap<>();

    private PreferenceScreen mPreferenceScreen;

    public static final String EXTRA_NESTED = "nested";

    /**
     * The fragment's current callback objects
     */
    private OnCreateNestedPreferenceFragment onCreateNestedPreferenceFragmentCallback = sDummyOnCreateNestedPreferenceFragmentCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when nested fragment is created
    interface OnCreateNestedPreferenceFragment {
        PreferenceFragment onCreateNestedPreferenceFragment();
    }

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final OnCreateNestedPreferenceFragment sDummyOnCreateNestedPreferenceFragmentCallback = new OnCreateNestedPreferenceFragment() {
        public PreferenceFragment onCreateNestedPreferenceFragment() {
            return null;
        }
    };

    private void savePreferenceScreen(PreferenceScreen preferenceScreen) {
        mPreferenceScreen = preferenceScreen;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnCreateNestedPreferenceFragment)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onCreateNestedPreferenceFragmentCallback = (OnCreateNestedPreferenceFragment) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onCreateNestedPreferenceFragmentCallback = sDummyOnCreateNestedPreferenceFragmentCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PPApplication.logE("PreferenceFragment.onCreate","xxx");

        Bundle bundle = this.getArguments();
        if ((!bundle.getBoolean(EXTRA_NESTED, false)) && (addPreferencesFromResource() != -1)) {
            addPreferencesFromResource(addPreferencesFromResource());
        }
    }

    private ArrayList<Preference> getAllPreferenceScreen(Preference p, ArrayList<Preference> list) {
        if( p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            if(p instanceof PreferenceScreen){
                list.add(p);
            }
            for(int i = 0; i < pCount; i++) {
                getAllPreferenceScreen(pGroup.getPreference(i), list);
            }
        }
        return list;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        PPApplication.logE("PreferenceFragment.onSaveInstanceState", "getSavedInstanceStateKeyName()="+getSavedInstanceStateKeyName());
        PPApplication.logE("PreferenceFragment.onSaveInstanceState", "getPreferenceScreen()="+getPreferenceScreen());
        if (getPreferenceScreen() != null)
            savedInstanceState.putString(getSavedInstanceStateKeyName(), getPreferenceScreen().getKey());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            //If we need to restore the state after a configuration change
            PPApplication.logE("PreferenceFragment.onCreateView","savedInstanceState="+savedInstanceState);
            PPApplication.logE("PreferenceFragment.onCreateView","getPreferenceScreen()="+getPreferenceScreen());

            Bundle bundle = this.getArguments();
            if ((!bundle.getBoolean(EXTRA_NESTED, false)) && (getPreferenceScreen() != null)) { //Main fragment will fill the HashMap
                PPApplication.logE("PreferenceFragment.onCreateView", "put preferenceScreenHashMap");
                ArrayList<Preference> preferences = getAllPreferenceScreen(getPreferenceScreen(), new ArrayList<Preference>());
                for (Preference preference : preferences) {
                    preferenceScreenHashMap.put(getSavedInstanceStateKeyName()+'.'+preference.getKey(),
                            (PreferenceScreen) preference);
                }
            }

            if (savedInstanceState != null) {
                if (getPreferenceScreen() == null) { //Nested fragments will use the HashMap to set their PreferenceScreen
                    PPApplication.logE("PreferenceFragment.onCreateView", "get screen from preferenceScreenHashMap="+getSavedInstanceStateKeyName());
                    PreferenceScreen preferenceScreen = preferenceScreenHashMap
                            .get(getSavedInstanceStateKeyName()+'.'+savedInstanceState.getString(getSavedInstanceStateKeyName()));
                    PPApplication.logE("PreferenceFragment.onCreateView","preferenceScreenHashMap.preferenceScreen="+preferenceScreen);
                    if (preferenceScreen != null) {
                        this.setPreferenceScreen(preferenceScreen);
                    }
                }
            }

            PPApplication.logE("PreferenceFragment.onCreateView","mPreferenceScreen="+mPreferenceScreen);
            PPApplication.logE("PreferenceFragment.onCreateView","getPreferenceScreen()="+getPreferenceScreen());

            if (mPreferenceScreen != null && getPreferenceScreen() == null) {
                // set PreferenceScreen by clicked nested mPreferenceScreen
                super.setPreferenceScreen(mPreferenceScreen);
                PPApplication.logE("PreferenceFragment.onCreateView","setPreferenceScreen");
            }
            ListView lv = v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);

            if (getPreferenceScreen() != null) {
                //Override PreferenceScreen click and preferences style
                for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                    Preference preference = getPreferenceScreen().getPreference(i);

                    if (preference instanceof PreferenceCategory) {
                        for (int j = 0; j < ((PreferenceCategory) preference).getPreferenceCount();
                             j++) {
                            preferenceToMaterialPreference(((PreferenceCategory) preference)
                                    .getPreference(j));
                        }
                    }

                    preferenceToMaterialPreference(preference);
                }
            }
        }
        return v;
    }

    private void preferenceToMaterialPreference(Preference preference){
        if (preference instanceof PreferenceScreen) {
            preference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            onPreferenceScreenClick((PreferenceScreen) preference);
                            return true;
                        }
                    });
        }

        /* Better is using android:layout="@layout/mp_preference_material_widget" in each preference and
           android:layout="@layout/mp_preference_category" in each PreferenceCategory.
           In these layouts is also multiline title. ;-)
        //Apply custom layouts on pre-Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (preference instanceof PreferenceScreen && preference.getLayoutResource()
                    != R.layout.mp_preference_material_widget) {
                preference.setLayoutResource(R.layout.mp_preference_material_widget);
            } else if (preference instanceof PreferenceCategory &&
                    preference.getLayoutResource() != R.layout.mp_preference_category) {
                preference.setLayoutResource(R.layout.mp_preference_category);

                PreferenceCategory category
                        = (PreferenceCategory) preference;
                for (int j = 0; j < category.getPreferenceCount(); j++) {
                    Preference basicPreference = category.getPreference(j);
                    if (!(basicPreference instanceof PreferenceCategory
                            || basicPreference instanceof PreferenceScreen)) {
                        if (basicPreference.getLayoutResource()
                                != R.layout.mp_preference_material_widget) {
                            basicPreference
                                    .setLayoutResource(R.layout.mp_preference_material_widget);
                        }
                    }
                }
            }
        }
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PPApplication.logE("PreferenceFragment.onDestroy","xxx");
        /*if (preferenceScreenHashMap.size() > 0) {
            preferenceScreenHashMap.clear();
        }*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getPreferenceScreen() != null) {
            //Title of each fragment will be specified in the android:title tag of each PreferenceScreen
            //in the preferences xml file
            ((PreferenceActivity) getActivity()).getSupportActionBar()
                    .setTitle(getPreferenceScreen().getTitle());
        }
    }

    private void onPreferenceScreenClick(@NonNull PreferenceScreen preference) {

        final Dialog dialog = preference.getDialog();
        if (dialog != null) { //It might be null if PreferenceScreen contains an intent
            PPApplication.logE("PreferenceFragment.onPreferenceScreenClick","dialog != null");

            //Close the default view without mp_toolbar and create our own Fragment version
            dialog.dismiss();

            PreferenceFragment fragment = onCreateNestedPreferenceFragmentCallback.onCreateNestedPreferenceFragment();
            //NestedPreferenceFragment fragment = new NestedPreferenceFragment();

            //Save the preference screen so it can bet set when the transaction is done
            fragment.savePreferenceScreen(preference);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                transaction.replace(R.id.content, fragment);
            //} else {
            //    transaction.replace(android.R.id.content, fragment);
            //}

            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(preference.getKey());
            transaction.commitAllowingStateLoss();
            //transaction.commit();
        }else if(preference.getIntent() != null) {
            PPApplication.logE("PreferenceFragment.onPreferenceScreenClick","preference.getIntent() != null");

            startActivity(preference.getIntent());
        }
        else
            PPApplication.logE("PreferenceFragment.onPreferenceScreenClick","????");
    }

    String getSavedInstanceStateKeyName() {
        return "PreferenceFragment_PreferenceScreenKey";
    }

}
