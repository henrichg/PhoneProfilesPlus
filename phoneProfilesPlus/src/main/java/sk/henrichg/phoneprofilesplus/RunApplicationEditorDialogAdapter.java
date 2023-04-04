package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class RunApplicationEditorDialogAdapter extends RecyclerView.Adapter<RunApplicationEditorDialogViewHolder>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final RunApplicationEditorDialog dialog;

    RunApplicationEditorDialogAdapter(RunApplicationEditorDialog dialog)
    {
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public RunApplicationEditorDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId;
        if (dialog.selectedFilter == 2)
            resId = R.layout.listitem_run_applications_editor_dialog_intent;
        else
            resId = R.layout.listitem_run_applications_editor_dialog;
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new RunApplicationEditorDialogViewHolder(view, /*context,*/ dialog);
    }

    @Override
    public void onBindViewHolder(@NonNull RunApplicationEditorDialogViewHolder holder, int position) {
        // Application to display
        Application application = dialog.applicationList.get(position);

        holder.bindApplication(application, position);
    }

    @SuppressWarnings("unused")
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
            return 0;
        }
        else*/ {
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
