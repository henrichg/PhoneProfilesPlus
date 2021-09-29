package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

@SuppressWarnings("WeakerAccess")
public class ImportantInfoActivityForceScrollFragment extends Fragment {

    int scrollTo = 0;

    public ImportantInfoActivityForceScrollFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.important_info_fragment_important_info, container, false);
        return inflater.inflate(R.layout.important_info_fragment_expandable_system_force_scroll, container, false);
    }

    @SuppressLint({"SetTextI18n", "BatteryLife"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        if (activity == null)
            return;

        ImportantInfoHelpFragment.doOnViewCreated(view, this);

        if ((scrollTo != 0) && (savedInstanceState == null)) {
            final ScrollView scrollView = view.findViewById(R.id.fragment_important_info_force_scroll_system_scroll_view);
            final View viewToScroll = view.findViewById(scrollTo);
            if ((scrollView != null) && (viewToScroll != null)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ImportantInfoHelpFragment.onViewCreated (2)");
                    scrollView.scrollTo(0, viewToScroll.getTop());
                }, 2000);
            }
        }

    }

}
