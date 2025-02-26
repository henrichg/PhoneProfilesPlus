package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ProfileIconPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private ProfileIconPreference preference;

    private Button colorChooserButton;
    ImageView dialogIcon;

    private ProfileIconPreferenceAdapter adapter;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (ProfileIconPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_profileicon_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        GridView gridView = view.findViewById(R.id.profileicon_pref_dlg_gridview);
        adapter = new ProfileIconPreferenceAdapter(preference, prefContext/*,
                            preference.imageIdentifier,
                            preference.isImageResourceID,
                            preference.useCustomColor,
                            preference.customColor*/);
        //noinspection DataFlowIssue
        gridView.setAdapter(adapter);
        gridView.setSelection(ProfileStatic.getImageResourcePosition(preference.imageIdentifier/*, prefContext*/));

        gridView.setOnItemClickListener((parent, v, position, id) -> {
            preference.setImageIdentifierAndType(ProfileStatic.getImageResourceName(position),true);
            adapter.imageIdentifierAndTypeChanged(/*preference.imageIdentifier, preference.isImageResourceID*/);
            preference.updateIcon(true, this);
            colorChooserButton.setEnabled(preference.isImageResourceID);
        });

        dialogIcon = view.findViewById(R.id.profileicon_pref_dlg_icon);

        colorChooserButton = view.findViewById(R.id.profileicon_pref_dlg_change_color);
        //noinspection DataFlowIssue
        colorChooserButton.setOnClickListener(v -> showCustomColorChooser());
        colorChooserButton.setEnabled(preference.isImageResourceID);

        /*final ImageView helpIcon = layout.findViewById(R.id.profileicon_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindow.showPopup(helpIcon, prefContext, R.string.profileicon_pref_dialog_info_about_status_bar_icon);
            }
        });*/

        final Button customIconButton = view.findViewById(R.id.profileicon_pref_dlg_custom_icon);
        //noinspection DataFlowIssue
        customIconButton.setOnClickListener(v -> {
            if (Permissions.grantCustomProfileIconPermissions(prefContext)) {
                preference.startGallery();
                //mDialog.dismiss();
            }
        });

        final AppCompatImageButton helpButton = view.findViewById(R.id.profileicon_pref_dlg_custom_icon_helpIcon);
        //noinspection DataFlowIssue
        TooltipCompat.setTooltipText(helpButton, getString(R.string.menu_help));
        helpButton.setOnClickListener(v -> {

            PopupMenu popup;
            popup = new PopupMenu(prefContext, helpButton, Gravity.END);
            //noinspection ConstantConditions
            getActivity().getMenuInflater().inflate(R.menu.profile_icon_help_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (getActivity() != null) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_profile_icon_help_icon_color) {
                        String helpString = getString(R.string.profile_icon_preference_iconColor_help_info_1) + StringConstants.TAG_BREAK_HTML +
                                getString(R.string.profile_icon_preference_iconColor_help_info_2);
                        DialogHelpPopupWindow.showPopup(helpButton, R.string.profile_icon_preference_iconColor_help, (Activity)prefContext, helpString, true);
                        return true;
                    }
                    else
                    if (itemId == R.id.menu_profile_icon_help_menu_custom_icon_pack) {
                        String helpString = getString(R.string.profile_icon_preference_custumIconFromIconPack_help_info_1) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.profile_icon_preference_custumIconFromIconPack_help_info_2) +
                                " \"ThemeX: Extract Launcher Theme\". " +
                                getString(R.string.profile_icon_preference_custumIconFromIconPack_help_info_3) + StringConstants.TAG_BREAK_HTML +
                                StringConstants.TAG_URL_LINK_START_HTML + "https://apkpure.com/themex-extract-launcher-theme/com.redphx.themex/download" + StringConstants.TAG_URL_LINK_START_URL_END_HTML+
                                getString(R.string.profile_icon_preference_custumIconFromIconPack_help_info_4)+
                                StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML + StringConstants.TAG_URL_LINK_END_HTML + StringConstants.TAG_DOUBLE_BREAK_HTML
                                ;

                        DialogHelpPopupWindow.showPopup(helpButton, R.string.profile_icon_preference_custumIconFromIconPack_help, (Activity)prefContext, helpString, true);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                return true;
            });

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                popup.show();
        });

        preference.getValuePIDP();
        preference.updateIcon(true, this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistIcon();
        }
        else {
            preference.resetSummary();
        }

        preference.fragment = null;
    }

    private void showCustomColorChooser() {
        if (getActivity() != null) {
            ProfileIconColorChooserDialog colorDialog = new ProfileIconColorChooserDialog((AppCompatActivity) prefContext, preference);
            if (!getActivity().isFinishing())
                colorDialog.showDialog();
        }

        /*
        ColorChooserDialog colorDialog  = new ColorChooserDialog.Builder(prefContext, R.string.colorChooser_pref_dialog_title)
                .titleSub(R.string.colorChooser_pref_dialog_title)  // title of dialog when viewing shades of a color
                .accentMode(false)  // when true, will display accent palette instead of primary palette
                .doneButton(android.R.string.ok)  // changes label of the done button
                .cancelButton(android.R.string.cancel)  // changes label of the cancel button
                .backButton(R.string.empty_string)  // changes label of the back button
                .preselect(customColor)  // optionally preselects a color
                .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                .build();
        colorDialog.show(); // an AppCompatActivity which implements ColorCallback
        */
    }

    void setCustomColor() {
        adapter.setCustomColor();
    }

}
