package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ApplicationsMultiSelectPreferenceAdapterX extends RecyclerView.Adapter<ApplicationsMultiSelectDialogPreferenceViewHolderX>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final ApplicationsMultiSelectDialogPreferenceX preference;

    ApplicationsMultiSelectPreferenceAdapterX(ApplicationsMultiSelectDialogPreferenceX preference)
    {
        this.preference = preference;
    }

    @NonNull
    @Override
    public ApplicationsMultiSelectDialogPreferenceViewHolderX onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource;
        resource = R.layout.applications_multiselect_preference_list_item;

        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new ApplicationsMultiSelectDialogPreferenceViewHolderX(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationsMultiSelectDialogPreferenceViewHolderX holder, int position) {
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
