package sk.henrichg.phoneprofilesplus;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

class HelpActivityFragmentStateAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public HelpActivityFragmentStateAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ImportantInfoHelpFragment tab1 = new ImportantInfoHelpFragment();
                return tab1;
            case 1:
                QuickGuideHelpFragment tab2 = new QuickGuideHelpFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
