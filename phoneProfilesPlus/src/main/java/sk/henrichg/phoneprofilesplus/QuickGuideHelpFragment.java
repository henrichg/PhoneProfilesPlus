package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

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

        TextView sensors = view.findViewById(R.id.activity_info_quick_guide_sensors_texts);
        String text = "<ul>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_2) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_3) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_4) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_5) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_6) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_7) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_8) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_9) + "</li>";
        text = text + "</ul>";

        sensors.setText(GlobalGUIRoutines.fromHtml(text, true, false));

        if ((scrollTo != 0) && (savedInstanceState == null)) {
            final ScrollView scrollView = view.findViewById(R.id.fragment_important_info_scroll_view);
            final View viewToScroll = view.findViewById(scrollTo);
            if ((scrollView != null) && (viewToScroll != null)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.scrollTo(0, viewToScroll.getTop());
                    }
                }, 200);
            }
        }
    }

}
