package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ApplicationEditorDialogAdapter extends RecyclerView.Adapter<ApplicationEditorDialogViewHolder>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final ApplicationEditorDialog dialog;

    ApplicationEditorDialogAdapter(ApplicationEditorDialog dialog)
    {
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public ApplicationEditorDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId;
        if (dialog.selectedFilter == 2)
            resId = R.layout.applications_editor_dialog_list_item_intent;
        else
            resId = R.layout.applications_editor_dialog_list_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new ApplicationEditorDialogViewHolder(view, /*context,*/ dialog);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationEditorDialogViewHolder holder, int position) {
        // Application to display
        //PPApplication.logE("ApplicationEditorDialogAdapter.onCreateViewHolder", "dialog.applicationList.size="+dialog.applicationList.size());
        Application application = dialog.applicationList.get(position);
        //PPApplication.logE("ApplicationEditorDialogAdapter.onCreateViewHolder", "position="+position);
        //PPApplication.logE("ApplicationEditorDialogAdapter.onCreateViewHolder", "application="+application);

        holder.bindApplication(application, position);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Application application = dialog.applicationList.get(position);
        /*if (application.checked)
            return "*";
        else*/
            return application.appLabel.substring(0, 1);
    }

    @Override
    public int getItemCount() {
        /*if (dialog.cachedApplicationList == null) {
            //PPApplication.logE("ApplicationEditorDialogAdapter.getItemCount", "getItemCount=0");
            return 0;
        }
        else*/ {
            //PPApplication.logE("ApplicationEditorDialogAdapter.getItemCount", "getItemCount="+dialog.applicationList.size());
            return dialog.applicationList.size();
        }
    }

    /*
    public Object getItem(int position) {
        return dialog.applicationList.get(position);
    }
    */

    public long getItemId(int position) {
        return position;
    }

}
