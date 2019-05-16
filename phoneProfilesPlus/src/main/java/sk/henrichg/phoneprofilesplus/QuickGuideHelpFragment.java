package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class QuickGuideHelpFragment extends Fragment {

    int scrollTo = 0;

    public QuickGuideHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quick_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (scrollTo != 0) {
            final ScrollView scrollView = view.findViewById(R.id.fragment_important_info_scroll_view);
            final View viewToScroll = view.findViewById(scrollTo);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, viewToScroll.getTop());
                }
            }, 200);
        }
    }

}
