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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

@SuppressWarnings("WeakerAccess")
public class ImportantInfoActivityForceScrollFragment extends Fragment {

    boolean showQuickGuide = false;
    int scrollTo = 0;

    public ImportantInfoActivityForceScrollFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (showQuickGuide)
            return inflater.inflate(R.layout.important_info_fragment_quick_guide, container, false);
        else
            return inflater.inflate(R.layout.important_info_fragment_system_force_scroll, container, false);
    }

    @SuppressLint({"SetTextI18n", "BatteryLife"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        if (activity == null)
            return;

        ImportantInfoHelpFragment.doOnViewCreated(view, this);

        TextView txt = view.findViewById(R.id.fragment_important_info_expandable_system_txt_empty);
        if (txt != null)
            txt.setVisibility(View.GONE);
        txt = view.findViewById(R.id.fragment_important_info_expandable_profiles_txt_empty);
        if (txt != null)
            txt.setVisibility(View.GONE);
        txt = view.findViewById(R.id.fragment_important_info_expandable_events_txt_empty);
        if (txt != null)
            txt.setVisibility(View.GONE);


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
