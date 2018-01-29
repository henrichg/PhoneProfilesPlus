package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ApplicationsMultiSelectPreferenceAdapter extends RecyclerView.Adapter<ApplicationsMultiSelectDialogPreferenceViewHolder>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final Context context;

    private final ApplicationsMultiSelectDialogPreference preference;

    private final boolean noShortcuts;

    ApplicationsMultiSelectPreferenceAdapter(Context context, ApplicationsMultiSelectDialogPreference preference, int addShortcuts)
    {
        this.context = context;
        this.preference = preference;

        noShortcuts = addShortcuts == 0;
    }

    @Override
    public ApplicationsMultiSelectDialogPreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resource;
        if (noShortcuts)
            resource = R.layout.applications_multiselect_preference_ns_list_item;
        else
            resource = R.layout.applications_multiselect_preference_list_item;

        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new ApplicationsMultiSelectDialogPreferenceViewHolder(view, context, noShortcuts);
    }

    @Override
    public void onBindViewHolder(ApplicationsMultiSelectDialogPreferenceViewHolder holder, int position) {
        // Application to display
        Application application = preference.applicationList.get(position);
        //System.out.println(String.valueOf(position));

        holder.bindApplication(application);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Application application = preference.applicationList.get(position);
        if (application.checked)
            return "*";
        else
            return application.appLabel.substring(0, 1);
    }

    @Override
    public int getItemCount() {
        if (preference.applicationList == null)
            return 0;
        else
            return preference.applicationList.size();
    }

}
