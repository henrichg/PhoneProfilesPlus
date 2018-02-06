package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

class ApplicationEditorDialogAdapter extends RecyclerView.Adapter<ApplicationEditorDialogViewHolder>
                                                implements FastScrollRecyclerView.SectionedAdapter
{
    private final Context context;

    private final ApplicationEditorDialog dialog;

    ApplicationEditorDialogAdapter(ApplicationEditorDialog dialog, Context context)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        this.context = context;

        this.dialog = dialog;
    }

    @Override
    public ApplicationEditorDialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applications_editor_dialog_list_item, parent, false);
        return new ApplicationEditorDialogViewHolder(view, context, dialog);
    }

    @Override
    public void onBindViewHolder(ApplicationEditorDialogViewHolder holder, int position) {
        // Application to display
        Application application = dialog.cachedApplicationList.get(position);
        //System.out.println(String.valueOf(position));

        holder.bindApplication(application, position);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Application application = dialog.cachedApplicationList.get(position);
        /*if (application.checked)
            return "*";
        else*/
            return application.appLabel.substring(0, 1);
    }

    @Override
    public int getItemCount() {
        if (dialog.cachedApplicationList == null)
            return 0;
        else
            return dialog.cachedApplicationList.size();
    }

    /*
    public Object getItem(int position) {
        return dialog.cachedApplicationList.get(position);
    }
    */

    public long getItemId(int position) {
        return position;
    }

}
