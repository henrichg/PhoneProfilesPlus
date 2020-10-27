package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

class ImportantInfoActivityFragmentStateAdapterX extends FragmentStateAdapter {

    private final ArrayList<Fragment> arrayList = new ArrayList<>();

    ImportantInfoActivityFragmentStateAdapterX(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return arrayList.get(position);
    }

    /*
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return arrayList.get(position);
    }
    */

    void addFragment(Fragment fragment) {
        arrayList.add(fragment);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

}
