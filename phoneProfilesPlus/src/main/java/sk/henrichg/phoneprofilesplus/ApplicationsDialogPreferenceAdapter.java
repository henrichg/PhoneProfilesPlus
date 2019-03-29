package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ApplicationsDialogPreferenceAdapter extends RecyclerView.Adapter<ApplicationsDialogPreferenceViewHolder>
                                            implements ItemTouchHelperAdapter
{
    private final Context context;

    private final ApplicationsDialogPreference preference;

    private final OnStartDragItemListener mDragStartListener;

    ApplicationsDialogPreferenceAdapter(Context context, ApplicationsDialogPreference preference,
                                        OnStartDragItemListener dragStartListener)
    {
        this.context = context;
        this.preference = preference;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public ApplicationsDialogPreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.applications_preference_list_item, parent, false);
        return new ApplicationsDialogPreferenceViewHolder(view, context, preference);
    }

    @Override
    public void onBindViewHolder(@NonNull final ApplicationsDialogPreferenceViewHolder holder, int position) {
        Application application = preference.applicationsList.get(position);
        holder.bindApplication(application);

        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });
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
}
