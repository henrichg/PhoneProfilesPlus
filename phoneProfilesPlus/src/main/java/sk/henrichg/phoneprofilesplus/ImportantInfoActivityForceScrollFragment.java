package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public class ImportantInfoActivityForceScrollFragment extends Fragment {

    boolean showQuickGuide = false;
    int showFragment = 0;
    int scrollTo = 0;

    public ImportantInfoActivityForceScrollFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            showQuickGuide = arguments.getBoolean(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE);
            showFragment = arguments.getInt(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT);
            scrollTo = arguments.getInt(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (showQuickGuide)
            return inflater.inflate(R.layout.fragment_important_info_quick_guide, container, false);
        else {
            switch (showFragment) {
                case 1:
                    return inflater.inflate(R.layout.fragment_important_info_profiles_force_scroll, container, false);
                case 2:
                    return inflater.inflate(R.layout.fragment_important_info_events_force_scroll, container, false);
                default:
                    return inflater.inflate(R.layout.fragment_important_info_system_force_scroll, container, false);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
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
            ScrollView _scrollView;
            switch (showFragment) {
                case 1:
                    _scrollView = view.findViewById(R.id.fragment_important_info_force_scroll_profiles_scroll_view);
                    break;
                case 2:
                    _scrollView = view.findViewById(R.id.fragment_important_info_force_scroll_events_scroll_view);
                    break;
                default:
                    _scrollView = view.findViewById(R.id.fragment_important_info_force_scroll_system_scroll_view);
                    break;
            }
            View _viewToScroll = view.findViewById(scrollTo);
            if ((_scrollView != null) && (_viewToScroll != null)) {
                final Handler handler = new Handler(activity.getMainLooper());
                final WeakReference<ScrollView> scrollViewWeakRef = new WeakReference<>(_scrollView);
                final WeakReference<View> viewToScrollWeakRef = new WeakReference<>(_viewToScroll);
                final WeakReference<Activity> activityWeakRef = new WeakReference<>(activity);
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ImportantInfoHelpFragment.onViewCreated (2)");
                    ScrollView scrollView = scrollViewWeakRef.get();
                    View viewToScroll = viewToScrollWeakRef.get();
                    Activity _activity = activityWeakRef.get();

                    //noinspection ConstantValue
                    if ((_activity == null) || _activity.isFinishing() || _activity.isDestroyed())
                        return;

                    if ((scrollView != null) && (viewToScroll != null))
                        scrollView.scrollTo(0, viewToScroll.getTop());
                }, 200);
            }
        }

    }

}
