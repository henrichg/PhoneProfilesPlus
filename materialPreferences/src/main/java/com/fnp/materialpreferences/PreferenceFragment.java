package com.fnp.materialpreferences;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class PreferenceFragment extends android.preference.PreferenceFragment {

    public abstract int addPreferencesFromResource();

    public static HashMap<String, PreferenceScreen> preferenceScreenHashMap = new HashMap<>();

    private PreferenceScreen mPreferenceScreen;

    public void savePreferenceScreen(PreferenceScreen preferenceScreen) {
        mPreferenceScreen = preferenceScreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (addPreferencesFromResource() != -1) {
            addPreferencesFromResource(addPreferencesFromResource());
        }

        //If we need to restore the state after a configuration change
        if (savedInstanceState != null) {
            if (getPreferenceScreen() != null) { //Main fragment will fill the HashMap
                ArrayList<Preference> preferences =  getAllPreferenceScreen(getPreferenceScreen(),
                        new ArrayList<Preference>());
                for(Preference preference: preferences){
                    preferenceScreenHashMap.put(preference.getKey(), (PreferenceScreen)preference);
                }

            } else { //Nested fragments will use the HashMap to set their PreferenceScreen
                PreferenceScreen preferenceScreen = preferenceScreenHashMap
                        .get(savedInstanceState
                                .getString("com.fnp.materialpreferences.nestedFragment"));
                if (preferenceScreen != null) {
                    this.setPreferenceScreen(preferenceScreen);
                }
            }
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
        savedInstanceState.putString("com.fnp.materialpreferences.nestedFragment",
                getPreferenceScreen().getKey());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            if (mPreferenceScreen != null && getPreferenceScreen() == null) {
                super.setPreferenceScreen(mPreferenceScreen);
            }
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);

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

        //Apply custom layouts on pre-Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (preference instanceof PreferenceScreen && preference.getLayoutResource()
                    != R.layout.mp_preference_material) {
                preference.setLayoutResource(R.layout.mp_preference_material);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (preferenceScreenHashMap.size() > 0) {
            preferenceScreenHashMap.clear();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Title of each fragment will be specified in the android:title tag of each PreferenceScreen
        //in the preferences xml file
        ((PreferenceActivity) getActivity()).getSupportActionBar()
                .setTitle(getPreferenceScreen().getTitle());
    }

    public boolean onPreferenceScreenClick(@NonNull PreferenceScreen preference) {

        final Dialog dialog = preference.getDialog();
        if (dialog != null) { //It might be null if PreferenceScreen contains an intent
            //Close the default view without mp_toolbar and create our own Fragment version
            dialog.dismiss();

            NestedPreferenceFragment fragment = new NestedPreferenceFragment();

            //Save the preference screen so it can bet set when the transaction is done
            fragment.savePreferenceScreen(preference);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                transaction.replace(R.id.content, fragment);
            } else {
                transaction.replace(android.R.id.content, fragment);
            }

            //TODO make animation optional (or give methods to animate it)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(preference.getKey());
            transaction.commitAllowingStateLoss();

            return true;
        }else if(preference.getIntent() != null) {
            startActivity(preference.getIntent());
            return true;
        }

        return false;
    }
}
