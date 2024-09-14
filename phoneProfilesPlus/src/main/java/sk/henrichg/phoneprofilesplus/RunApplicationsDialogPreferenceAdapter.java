package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

class RunApplicationsDialogPreferenceAdapter extends RecyclerView.Adapter<RunApplicationsDialogPreferenceViewHolder>
                                            implements ItemTouchHelperAdapter
{
    private final Context context;

    private final RunApplicationsDialogPreference preference;

    private final OnStartDragItemListener mDragStartListener;

    RunApplicationsDialogPreferenceAdapter(Context context, RunApplicationsDialogPreference preference,
                                           OnStartDragItemListener dragStartListener)
    {
        this.context = context;
        this.preference = preference;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public RunApplicationsDialogPreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_run_applications_preference, parent, false);
        return new RunApplicationsDialogPreferenceViewHolder(view, context, preference);
    }

    @Override
    public void onBindViewHolder(@NonNull final RunApplicationsDialogPreferenceViewHolder holder, int position) {
        Application application = preference.applicationsList.get(position);
        holder.bindApplication(application);

        if (holder.dragHandle != null) {
            holder.dragHandle.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDragStartListener.onStartDrag(holder);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                /*if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }*/
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return preference.applicationsList.size();
    }

    @Override
    public void onItemDismiss(int position) {
        preference.applicationsList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (preference.applicationsList == null)
            return false;

        //Log.d("----- ApplicationsDialogPreferenceAdapter.onItemMove", "fromPosition="+fromPosition);
        //Log.d("----- ApplicationsDialogPreferenceAdapter.onItemMove", "toPosition="+toPosition);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(preference.applicationsList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(preference.applicationsList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void clearView() {

    }

}
