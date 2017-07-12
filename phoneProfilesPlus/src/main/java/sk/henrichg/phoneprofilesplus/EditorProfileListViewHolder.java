package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class EditorProfileListViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener, View.OnLongClickListener

{

    ImageView dragHandle;
    //private RelativeLayout listItemRoot;
    private ImageView profileIcon;
    private TextView profileName;
    private ImageView profileIndicator;
    private ImageView profileItemEditMenu;

    private Profile profile;
    private EditorProfileListFragment editorFragment;

    private Context context;

    EditorProfileListViewHolder(View itemView, EditorProfileListFragment editorFragment, Context context, int filterType) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
            dragHandle = (ImageView) itemView.findViewById(R.id.profile_list_drag_handle);
        else
            dragHandle = null;

        //listItemRoot = (RelativeLayout)itemView.findViewById(R.id.profile_list_item_root);
        profileName = (TextView)itemView.findViewById(R.id.profile_list_item_profile_name);
        profileIcon = (ImageView)itemView.findViewById(R.id.profile_list_item_profile_icon);
        profileItemEditMenu = (ImageView)itemView.findViewById(R.id.profile_list_item_edit_menu);
        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
            profileIndicator = (ImageView)itemView.findViewById(R.id.profile_list_profile_pref_indicator);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    void bindProfile(Profile profile) {

        this.profile = profile;

        if (profile._checked && (!ApplicationPreferences.applicationEditorHeader(context)))
        {
            profileName.setTypeface(null, Typeface.BOLD);
            profileName.setTextSize(16);
            profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(editorFragment.getActivity()));
        }
        else
        {
            profileName.setTypeface(null, Typeface.NORMAL);
            profileName.setTextSize(15);
            profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(editorFragment.getActivity()));
        }

        String _profileName = editorFragment.dataWrapper.getProfileNameWithManualIndicator(profile,
                                    profile._checked &&
                        (!ApplicationPreferences.applicationEditorHeader(context)), true, false);
        Log.d("EditorProfileListViewHolder.bindProfile","_profileName="+_profileName);
        if (profile._showInActivator)
            _profileName = "[A] " + _profileName;

        profileName.setText(_profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        context.getPackageName());
                profileIcon.setImageResource(res); // resource na ikonu
            }
        }
        else
        {
            profileIcon.setImageBitmap(profile._iconBitmap);
        }

        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            if (profileIndicator != null)
                profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        profileItemEditMenu.setTag(profile);
        final ImageView _profileItemEditMenu = profileItemEditMenu;
        profileItemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                editorFragment.showEditMenu(_profileItemEditMenu);
            }
        });
    }

    @Override
    public void onClick(View v) {
        editorFragment.startProfilePreferencesActivity(profile, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        editorFragment.activateProfile(profile);
        return true;
    }

}
