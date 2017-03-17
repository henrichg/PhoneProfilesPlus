package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public abstract class PreferenceActivity extends AppCompatPreferenceActivity
{

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setPreferenceFragment(PreferenceFragment preferenceFragment) {

        //First check if it's already loaded (configuration change) so we don't overlap fragments
        if(getFragmentManager()
                .findFragmentByTag("sk.henrichg.phoneprofilesplus.MainFragment") != null){
            return;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fragmentTransaction.replace(R.id.content, preferenceFragment,
                    "sk.henrichg.phoneprofilesplus.MainFragment");
        //}else{
        //    fragmentTransaction.replace(android.R.id.content, preferenceFragment,
        //            "sk.henrichg.phoneprofilesplus.MainFragment");
        //}
        fragmentTransaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not add custom layout for lollipop devices or we lose the widgets animation
        // (app compat bug?)
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setContentView(R.layout.mp_activity_settings);
            Toolbar toolbar = (Toolbar) findViewById(R.id.mp_toolbar);
            setSupportActionBar(toolbar);
        //}

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public PreferenceFragment getFragment() {
        int id;
        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            id = R.id.content;
        //} else {
        //    id = android.R.id.content;
        //}

        return (PreferenceFragment) getFragmentManager().findFragmentById(id);
    }

}
