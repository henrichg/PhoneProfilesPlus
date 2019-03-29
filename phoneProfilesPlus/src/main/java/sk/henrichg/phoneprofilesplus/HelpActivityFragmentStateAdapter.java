package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

class HelpActivityFragmentStateAdapter extends FragmentStatePagerAdapter {

    private final int mNumOfTabs;

    HelpActivityFragmentStateAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new QuickGuideHelpFragment();
            default:
                return new ImportantInfoHelpFragment();
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
