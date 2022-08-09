package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class RunApplicationEditorDialogAdapterX extends RecyclerView.Adapter<RunApplicationEditorDialogViewHolderX>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final RunApplicationEditorDialogX dialog;

    RunApplicationEditorDialogAdapterX(RunApplicationEditorDialogX dialog)
    {
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public RunApplicationEditorDialogViewHolderX onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId;
        if (dialog.selectedFilter == 2)
            resId = R.layout.run_applications_editor_dialog_list_item_intent;
        else
            resId = R.layout.run_applications_editor_dialog_list_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new RunApplicationEditorDialogViewHolderX(view, /*context,*/ dialog);
    }

    @Override
    public void onBindViewHolder(@NonNull RunApplicationEditorDialogViewHolderX holder, int position) {
        // Application to display
        Application application = dialog.applicationList.get(position);

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
