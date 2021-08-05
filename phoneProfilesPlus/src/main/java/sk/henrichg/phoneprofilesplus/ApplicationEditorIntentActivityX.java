package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationEditorIntentActivityX extends AppCompatActivity {

    private Application application = null;
    private PPIntent ppIntent = null;

    private int startApplicationDelay;

    private Button okButton;

    //private ScrollView intentScrollView;
    private EditText intentNameEditText;
    private AppCompatSpinner intentIntentTypeSpinner;
    private EditText intentPackageName;
    private EditText intentClassName;
    private AppCompatSpinner intentActionSpinner;
    private EditText intentActionEdit;
    private EditText intentData;
    private EditText intentMimeType;
    private TextView categoryTextView;
    private TextView flagsTextView;

    private EditText intentExtraKeyName1;
    private EditText intentExtraKeyValue1;
    private AppCompatSpinner intentExtraSpinner1;
    private EditText intentExtraKeyName2;
    private EditText intentExtraKeyValue2;
    private AppCompatSpinner intentExtraSpinner2;
    private EditText intentExtraKeyName3;
    private EditText intentExtraKeyValue3;
    private AppCompatSpinner intentExtraSpinner3;
    private EditText intentExtraKeyName4;
    private EditText intentExtraKeyValue4;
    private AppCompatSpinner intentExtraSpinner4;
    private EditText intentExtraKeyName5;
    private EditText intentExtraKeyValue5;
    private AppCompatSpinner intentExtraSpinner5;

    private String[] actionsArray;
    private String[] categoryArray;
    private boolean[] categoryIndices;
    private String[] flagArray;
    private boolean[] flagIndices;

    public static final String EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY = "dialogPreferenceStartApplicationDelay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_application_editor_intent);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.intent_editor_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            application = intent.getParcelableExtra(ApplicationEditorDialogX.EXTRA_APPLICATION);
            ppIntent = intent.getParcelableExtra(ApplicationEditorDialogX.EXTRA_PP_INTENT);
            startApplicationDelay = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0);
        }
        else {
            application = savedInstanceState.getParcelable(ApplicationEditorDialogX.EXTRA_APPLICATION);
            ppIntent = savedInstanceState.getParcelable(ApplicationEditorDialogX.EXTRA_PP_INTENT);
            startApplicationDelay = savedInstanceState.getInt(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0);
        }
        /*if (ppIntent == null)
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent=null");
        else
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent._id="+ppIntent._id);*/

        okButton = findViewById(R.id.application_editor_intent_ok);
        //intentScrollView = findViewById(R.id.application_editor_intent_scroll_view);

        intentNameEditText = findViewById(R.id.application_editor_intent_intent_name);
        intentNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableOKButton();
            }
        });

        intentIntentTypeSpinner = findViewById(R.id.application_editor_intent_intent_type_spinner);
        GlobalGUIRoutines.HighlightedSpinnerAdapter spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentIntentTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentIntentTypeSpinner.setAdapter(spinnerAdapter);
        intentIntentTypeSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentIntentTypeSpinner.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentIntentTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentIntentTypeSpinner.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentPackageName = findViewById(R.id.application_editor_intent_package_name);

        intentClassName = findViewById(R.id.application_editor_intent_class_name);
        intentData = findViewById(R.id.application_editor_intent_data);
        intentMimeType = findViewById(R.id.application_editor_intent_mime_type);

        intentActionSpinner = findViewById(R.id.application_editor_intent_action_spinner);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentActionArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentActionSpinner.setAdapter(spinnerAdapter);
        intentActionSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentActionSpinner.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentActionSpinner.getAdapter()).setSelection(position);

                if (position == 0) {
                    intentActionEdit.setText(R.string.empty_string);
                    intentActionEdit.setEnabled(false);
                }
                else
                if (position == 1) {
                    intentActionEdit.setEnabled(true);
                }
                else {
                    intentActionEdit.setText(R.string.empty_string);
                    intentActionEdit.setEnabled(false);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentActionEdit = findViewById(R.id.application_editor_intent_action_edit);

        final Activity activity = this;

        categoryTextView = findViewById(R.id.application_editor_intent_category_value);
        categoryArray = getResources().getStringArray(R.array.applicationEditorIntentCategoryArray);
        categoryIndices = new boolean[categoryArray.length];
        AppCompatImageButton intentCategoryButton = findViewById(R.id.application_editor_intent_category_btn);
        TooltipCompat.setTooltipText(intentCategoryButton, getString(R.string.application_editor_intent_edit_category_button_tooltip));
        intentCategoryButton.setOnClickListener(v -> {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.application_editor_intent_categories_dlg_title)
                            //.setIcon(getDialogIcon())
                            .setMultiChoiceItems(R.array.applicationEditorIntentCategoryArray, categoryIndices,
                                    (dialog, which, isChecked) -> categoryIndices[which] = isChecked);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                intentNameEditText.clearFocus();
                intentPackageName.clearFocus();
                intentClassName.clearFocus();
                intentActionEdit.clearFocus();
                intentData.clearFocus();
                intentMimeType.clearFocus();

                String categoryValue = "";
                int i = 0;
                for (boolean selected : categoryIndices) {
                    if (selected) {
                        if (!categoryValue.isEmpty())
                            //noinspection StringConcatenationInLoop
                            categoryValue = categoryValue + "\n";
                        //noinspection StringConcatenationInLoop
                        categoryValue = categoryValue + categoryArray[i];
                    }
                    ++i;
                }
                categoryTextView.setText(categoryValue);
                /*intentScrollView.post(new Runnable() {
                    public void run() {
                        intentScrollView.scrollTo(0, categoryTextView.getBottom());
                    }
                });*/
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

            //AlertDialog mDialog = builder.create();
            if (!isFinishing())
                dialog.show();
        });

        flagsTextView = findViewById(R.id.application_editor_intent_flags_value);
        flagArray = getResources().getStringArray(R.array.applicationEditorIntentFlagArray);
        flagIndices = new boolean[flagArray.length];
        AppCompatImageButton intentFlagsButton = findViewById(R.id.application_editor_intent_flags_btn);
        TooltipCompat.setTooltipText(intentFlagsButton, getString(R.string.application_editor_intent_edit_flags_button_tooltip));
        intentFlagsButton.setOnClickListener(v -> {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.application_editor_intent_flags_dlg_title)
                            //.setIcon(getDialogIcon())
                            .setMultiChoiceItems(R.array.applicationEditorIntentFlagArray, flagIndices, (dialog, which, isChecked) -> flagIndices[which] = isChecked);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                intentNameEditText.clearFocus();
                intentPackageName.clearFocus();
                intentClassName.clearFocus();
                intentActionEdit.clearFocus();
                intentData.clearFocus();
                intentMimeType.clearFocus();

                String flagsValue = "";
                int i = 0;
                for (boolean selected : flagIndices) {
                    if (selected) {
                        if (!flagsValue.isEmpty())
                            //noinspection StringConcatenationInLoop
                            flagsValue = flagsValue + "\n";
                        //noinspection StringConcatenationInLoop
                        flagsValue = flagsValue + flagArray[i];
                    }
                    ++i;
                }
                flagsTextView.setText(flagsValue);
                /*intentScrollView.post(new Runnable() {
                    public void run() {
                        intentScrollView.scrollTo(0, flagsTextView.getBottom());
                    }
                });*/
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            });

            AlertDialog mDialog = builder.create();

//                mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

            if (!isFinishing())
                mDialog.show();
        });

        intentExtraKeyName1 = findViewById(R.id.application_editor_intent_extra_key_1);
        intentExtraKeyValue1 = findViewById(R.id.application_editor_intent_extra_value_1);

        intentExtraSpinner1  = findViewById(R.id.application_editor_intent_extra_type_spinner_1);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentExtraTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentExtraSpinner1.setAdapter(spinnerAdapter);
        intentExtraSpinner1.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentExtraSpinner1.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentExtraSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentExtraSpinner1.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentExtraKeyName2 = findViewById(R.id.application_editor_intent_extra_key_2);
        intentExtraKeyValue2 = findViewById(R.id.application_editor_intent_extra_value_2);

        intentExtraSpinner2  = findViewById(R.id.application_editor_intent_extra_type_spinner_2);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentExtraTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentExtraSpinner2.setAdapter(spinnerAdapter);
        intentExtraSpinner2.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentExtraSpinner2.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentExtraSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentExtraSpinner2.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentExtraKeyName3 = findViewById(R.id.application_editor_intent_extra_key_3);
        intentExtraKeyValue3 = findViewById(R.id.application_editor_intent_extra_value_3);

        intentExtraSpinner3  = findViewById(R.id.application_editor_intent_extra_type_spinner_3);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentExtraTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentExtraSpinner3.setAdapter(spinnerAdapter);
        intentExtraSpinner3.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentExtraSpinner3.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentExtraSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentExtraSpinner3.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentExtraKeyName4 = findViewById(R.id.application_editor_intent_extra_key_4);
        intentExtraKeyValue4 = findViewById(R.id.application_editor_intent_extra_value_4);

        intentExtraSpinner4  = findViewById(R.id.application_editor_intent_extra_type_spinner_4);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentExtraTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentExtraSpinner4.setAdapter(spinnerAdapter);
        intentExtraSpinner4.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentExtraSpinner4.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentExtraSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentExtraSpinner4.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        intentExtraKeyName5 = findViewById(R.id.application_editor_intent_extra_key_5);
        intentExtraKeyValue5 = findViewById(R.id.application_editor_intent_extra_value_5);

        intentExtraSpinner5  = findViewById(R.id.application_editor_intent_extra_type_spinner_5);
        spinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_spinner,
                getResources().getStringArray(R.array.applicationEditorIntentExtraTypeArray));
        spinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        intentExtraSpinner5.setAdapter(spinnerAdapter);
        intentExtraSpinner5.setPopupBackgroundResource(R.drawable.popupmenu_background);
        intentExtraSpinner5.setBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner));
        intentExtraSpinner5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)intentExtraSpinner5.getAdapter()).setSelection(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        actionsArray = getResources().getStringArray(R.array.applicationEditorIntentActionArray);
        if (ppIntent != null) {
            intentNameEditText.setText(ppIntent._name);
            intentIntentTypeSpinner.setSelection(ppIntent._intentType);
            intentPackageName.setText(ppIntent._packageName);
            intentClassName.setText(ppIntent._className);
            intentData.setText(ppIntent._data);
            intentMimeType.setText(ppIntent._mimeType);

            if ((ppIntent._action == null) || ppIntent._action.isEmpty()) {
                intentActionSpinner.setSelection(0);
                intentActionEdit.setText(R.string.empty_string);
            } else {
                boolean custom = true;
                for (String action : actionsArray) {
                    if (action.equals(ppIntent._action)) {
                        custom = false;
                        break;
                    }
                }
                if (custom) {
                    intentActionSpinner.setSelection(1);
                    intentActionEdit.setText(ppIntent._action);
                } else {
                    intentActionSpinner.setSelection(Arrays.asList(actionsArray).indexOf(ppIntent._action));
                    intentActionEdit.setText(R.string.empty_string);
                }
            }
            intentActionEdit.setEnabled(false);

            if (ppIntent._categories != null) {
                String categoryValue = ppIntent._categories.replaceAll("\\|", "\n");
                categoryTextView.setText(categoryValue);
                List<String> stringList = new ArrayList<>(Arrays.asList(categoryArray));
                String[] splits = ppIntent._categories.split("\\|");
                for (String category : splits) {
                    int i = stringList.indexOf(category);
                    if (i != -1) {
                        categoryIndices[i] = true;
                    }
                }
            }

            if (ppIntent._flags != null) {
                String flagsValue = ppIntent._flags.replaceAll("\\|", "\n");
                flagsTextView.setText(flagsValue);
                List<String> stringList = new ArrayList<>(Arrays.asList(flagArray));
                String[] splits = ppIntent._flags.split("\\|");
                for (String flag : splits) {
                    int i = stringList.indexOf(flag);
                    if (i != -1)
                        flagIndices[i] = true;
                }
            }

            if (ppIntent._extraKey1 != null) {
                intentExtraKeyName1.setText(ppIntent._extraKey1);
                intentExtraKeyValue1.setText(ppIntent._extraValue1);
                intentExtraSpinner1.setSelection(ppIntent._extraType1);
            }
            if (ppIntent._extraKey2 != null) {
                intentExtraKeyName2.setText(ppIntent._extraKey2);
                intentExtraKeyValue2.setText(ppIntent._extraValue2);
                intentExtraSpinner2.setSelection(ppIntent._extraType2);
            }
            if (ppIntent._extraKey3 != null) {
                intentExtraKeyName3.setText(ppIntent._extraKey3);
                intentExtraKeyValue3.setText(ppIntent._extraValue3);
                intentExtraSpinner3.setSelection(ppIntent._extraType3);
            }
            if (ppIntent._extraKey4 != null) {
                intentExtraKeyName4.setText(ppIntent._extraKey4);
                intentExtraKeyValue4.setText(ppIntent._extraValue4);
                intentExtraSpinner4.setSelection(ppIntent._extraType4);
            }
            if (ppIntent._extraKey5 != null) {
                intentExtraKeyName5.setText(ppIntent._extraKey5);
                intentExtraKeyValue5.setText(ppIntent._extraValue5);
                intentExtraSpinner5.setSelection(ppIntent._extraType5);
            }

        }
        else {
            intentActionEdit.setEnabled(false);
        }

        enableOKButton();
        okButton.setOnClickListener(v -> {
            saveIntent();
            Intent returnIntent = new Intent();
            returnIntent.putExtra(ApplicationEditorDialogX.EXTRA_PP_INTENT, ppIntent);
            returnIntent.putExtra(ApplicationEditorDialogX.EXTRA_APPLICATION, application);
            returnIntent.putExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });

        Button cancelButton = findViewById(R.id.application_editor_intent_cancel);
        cancelButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        });

        Button testButton = findViewById(R.id.application_editor_intent_test);
        testButton.setOnClickListener(v -> {
            //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "ppIntent="+ppIntent);
            if (ppIntent == null) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.application_editor_intent_test_title)
                                //.setIcon(getDialogIcon())
                                .setMessage(R.string.application_editor_intent_test_activity_bad_data);
                builder.setPositiveButton(android.R.string.ok, null);

                AlertDialog mDialog = builder.create();

//                    mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                if (!isFinishing())
                    mDialog.show();
            }
            else {
                saveIntent();
                Intent testIntent = createIntent(ppIntent);
                //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "testIntent="+testIntent);
                boolean ok = false;
                if (testIntent != null) {
                    //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "ppIntent._intentType="+ppIntent._intentType);
                    if (ppIntent._intentType == 0) {
                        try {
                            testIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(testIntent);
                            //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "activity started");
                            ok = true;
                        } catch (Exception e) {
                            //Log.e("ApplicationEditorIntentActivityX.onCreate.testButtonClick", Log.getStackTraceString(e));
                            //PPApplication.recordException(e);
                        }
                    } else {
                        try {
                            sendBroadcast(testIntent);
                            //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "broadcast sent");
                            ok = true;
                        } catch (Exception e) {
                            //Log.e("ApplicationEditorIntentActivityX.onCreate.testButtonClick", Log.getStackTraceString(e));
                        }
                    }
                }
                //PPApplication.logE("ApplicationEditorIntentActivityX.onCreate.testButtonClick", "ok="+ok);
                if (!ok) {
                    AlertDialog.Builder builder;
                    if (ppIntent._intentType == 0) {
                        builder =
                                new AlertDialog.Builder(activity)
                                        .setTitle(R.string.application_editor_intent_test_title)
                                        //.setIcon(getDialogIcon())
                                        .setMessage(R.string.application_editor_intent_test_activity_bad_data);
                    } else {
                        builder =
                                new AlertDialog.Builder(activity)
                                        .setTitle(R.string.application_editor_intent_test_title)
                                        //.setIcon(getDialogIcon())
                                        .setMessage(R.string.application_editor_intent_test_broadcast_bad_data);
                    }
                    builder.setPositiveButton(android.R.string.ok, null);

                    AlertDialog mDialog = builder.create();

//                        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                            @Override
//                            public void onShow(DialogInterface dialog) {
//                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                if (positive != null) positive.setAllCaps(false);
//                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                if (negative != null) negative.setAllCaps(false);
//                            }
//                        });

                    if (!isFinishing())
                        mDialog.show();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(ApplicationEditorDialogX.EXTRA_APPLICATION, application);
        savedInstanceState.putParcelable(ApplicationEditorDialogX.EXTRA_PP_INTENT, ppIntent);
        savedInstanceState.putInt(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);
    }

    private void saveIntent() {
        if (ppIntent == null)
            return;

        if (intentNameEditText.getText() != null)
            ppIntent._name = intentNameEditText.getText().toString();
        else
            ppIntent._name = "";
        if (application != null)
            application.appLabel = ppIntent._name;
        ppIntent._intentType = intentIntentTypeSpinner.getSelectedItemPosition();
        if (intentPackageName.getText() != null)
            ppIntent._packageName = intentPackageName.getText().toString().replaceAll(" ","");
        else
            ppIntent._packageName = "";
        if (intentClassName.getText() != null)
            ppIntent._className = intentClassName.getText().toString().replaceAll(" ","");
        else
            ppIntent._className = "";
        if (intentData.getText() != null)
            ppIntent._data = intentData.getText().toString();
        else
            ppIntent._data = "";
        if (intentMimeType.getText() != null)
            ppIntent._mimeType = intentMimeType.getText().toString().replaceAll(" ","");
        else
            ppIntent._mimeType = "";

        int actionSpinnerId = intentActionSpinner.getSelectedItemPosition();
        if (actionSpinnerId == 0)
            ppIntent._action = "";
        else
        if (actionSpinnerId == 1)
            if (intentActionEdit.getText() != null)
                ppIntent._action = intentActionEdit.getText().toString();
            else
                ppIntent._action = "";
        else {
            String action = "";
            switch (actionsArray[actionSpinnerId]) {
                case "ACTION_ALL_APPS":
                    action = Intent.ACTION_ALL_APPS;
                    break;
                case "ACTION_ANSWER":
                    action = Intent.ACTION_ANSWER;
                    break;
                case "ACTION_APPLICATION_PREFERENCES":
                    action = Intent.ACTION_APPLICATION_PREFERENCES;
                    break;
                case "ACTION_APP_ERROR":
                    action = Intent.ACTION_APP_ERROR;
                    break;
                case "ACTION_ASSIST":
                    action = Intent.ACTION_ASSIST;
                    break;
                case "ACTION_ATTACH_DATA":
                    action = Intent.ACTION_ATTACH_DATA;
                    break;
                case "ACTION_BUG_REPORT":
                    action = Intent.ACTION_BUG_REPORT;
                    break;
                case "ACTION_CALL":
                    action = Intent.ACTION_CALL;
                    break;
                case "ACTION_CALL_BUTTON":
                    action = Intent.ACTION_CALL_BUTTON;
                    break;
                case "ACTION_CARRIER_SETUP":
                    action = Intent.ACTION_CARRIER_SETUP;
                    break;
                case "ACTION_CHOOSER":
                    action = Intent.ACTION_CHOOSER;
                    break;
                case "ACTION_CREATE_DOCUMENT":
                    action = Intent.ACTION_CREATE_DOCUMENT;
                    break;
                case "ACTION_DELETE":
                    action = Intent.ACTION_DELETE;
                    break;
                case "ACTION_DIAL":
                    action = Intent.ACTION_DIAL;
                    break;
                case "ACTION_EDIT":
                    action = Intent.ACTION_EDIT;
                    break;
                case "ACTION_FACTORY_TEST":
                    action = Intent.ACTION_FACTORY_TEST;
                    break;
                case "ACTION_GET_CONTENT":
                    action = Intent.ACTION_GET_CONTENT;
                    break;
                case "ACTION_INSERT":
                    action = Intent.ACTION_INSERT;
                    break;
                case "ACTION_INSERT_OR_EDIT":
                    action = Intent.ACTION_INSERT_OR_EDIT;
                    break;
                case "ACTION_INSTALL_PACKAGE":
                    //noinspection deprecation
                    action = Intent.ACTION_INSTALL_PACKAGE;
                    break;
                case "ACTION_MAIN":
                    action = Intent.ACTION_MAIN;
                    break;
                case "ACTION_MANAGE_NETWORK_USAGE":
                    action = Intent.ACTION_MANAGE_NETWORK_USAGE;
                    break;
                case "ACTION_OPEN_DOCUMENT":
                    action = Intent.ACTION_OPEN_DOCUMENT;
                    break;
                case "ACTION_OPEN_DOCUMENT_TREE":
                    action = Intent.ACTION_OPEN_DOCUMENT_TREE;
                    break;
                case "ACTION_PASTE":
                    action = Intent.ACTION_PASTE;
                    break;
                case "ACTION_PICK":
                    action = Intent.ACTION_PICK;
                    break;
                case "ACTION_PICK_ACTIVITY":
                    action = Intent.ACTION_PICK_ACTIVITY;
                    break;
                case "ACTION_POWER_USAGE_SUMMARY":
                    action = Intent.ACTION_POWER_USAGE_SUMMARY;
                    break;
                case "ACTION_PROCESS_TEXT":
                    action = Intent.ACTION_PROCESS_TEXT;
                    break;
                case "ACTION_QUICK_CLOCK":
                    action = Intent.ACTION_QUICK_CLOCK;
                    break;
                case "ACTION_QUICK_VIEW":
                    action = Intent.ACTION_QUICK_VIEW;
                    break;
                case "ACTION_RUN":
                    action = Intent.ACTION_RUN;
                    break;
                case "ACTION_SEARCH":
                    action = Intent.ACTION_SEARCH;
                    break;
                case "ACTION_SEARCH_LONG_PRESS":
                    action = Intent.ACTION_SEARCH_LONG_PRESS;
                    break;
                case "ACTION_SEND":
                    action = Intent.ACTION_SEND;
                    break;
                case "ACTION_SENDTO":
                    action = Intent.ACTION_SENDTO;
                    break;
                case "ACTION_SEND_MULTIPLE":
                    action = Intent.ACTION_SEND_MULTIPLE;
                    break;
                case "ACTION_SET_WALLPAPER":
                    action = Intent.ACTION_SET_WALLPAPER;
                    break;
                case "ACTION_SHOW_APP_INFO":
                    action = Intent.ACTION_SHOW_APP_INFO;
                    break;
                case "ACTION_SYNC":
                    action = Intent.ACTION_SYNC;
                    break;
                case "ACTION_SYSTEM_TUTORIAL":
                    action = Intent.ACTION_SYSTEM_TUTORIAL;
                    break;
                case "ACTION_UNINSTALL_PACKAGE":
                    //noinspection deprecation
                    action = Intent.ACTION_UNINSTALL_PACKAGE;
                    break;
                case "ACTION_VIEW":
                    action = Intent.ACTION_VIEW;
                    break;
                case "ACTION_VOICE_COMMAND":
                    action = Intent.ACTION_VOICE_COMMAND;
                    break;
                case "ACTION_WEB_SEARCH":
                    action = Intent.ACTION_WEB_SEARCH;
                    break;
            }
            ppIntent._action = action;
        }

        ppIntent._categories = "";
        int i = 0;
        for (boolean selected : categoryIndices) {
            if (selected) {
                if (!ppIntent._categories.isEmpty())
                    //noinspection StringConcatenationInLoop
                    ppIntent._categories = ppIntent._categories + "|";
                //noinspection StringConcatenationInLoop
                ppIntent._categories = ppIntent._categories + categoryArray[i];
            }
            ++i;
        }

        ppIntent._flags = "";
        i = 0;
        for (boolean selected : flagIndices) {
            if (selected) {
                if (!ppIntent._flags.isEmpty())
                    //noinspection StringConcatenationInLoop
                    ppIntent._flags = ppIntent._flags + "|";
                //noinspection StringConcatenationInLoop
                ppIntent._flags = ppIntent._flags + flagArray[i];
            }
            ++i;
        }

        if (intentExtraKeyName1.getText() != null)
            ppIntent._extraKey1 = intentExtraKeyName1.getText().toString();
        else
            ppIntent._extraKey1 = "";
        if (intentExtraKeyValue1.getText() != null)
            ppIntent._extraValue1 = intentExtraKeyValue1.getText().toString();
        else
            ppIntent._extraValue1 = "";
        ppIntent._extraType1 = intentExtraSpinner1.getSelectedItemPosition();

        if (intentExtraKeyName2.getText() != null)
            ppIntent._extraKey2 = intentExtraKeyName2.getText().toString();
        else
            ppIntent._extraKey2 = "";
        if (intentExtraKeyValue2.getText() != null)
            ppIntent._extraValue2 = intentExtraKeyValue2.getText().toString();
        else
            ppIntent._extraValue2 = "";
        ppIntent._extraType2 = intentExtraSpinner2.getSelectedItemPosition();

        if (intentExtraKeyName3.getText() != null)
            ppIntent._extraKey3 = intentExtraKeyName3.getText().toString();
        else
            ppIntent._extraKey3 = "";
        if (intentExtraKeyValue3.getText() != null)
            ppIntent._extraValue3 = intentExtraKeyValue3.getText().toString();
        else
            ppIntent._extraValue3 = "";
        ppIntent._extraType3 = intentExtraSpinner3.getSelectedItemPosition();

        if (intentExtraKeyName4.getText() != null)
            ppIntent._extraKey4 = intentExtraKeyName4.getText().toString();
        else
            ppIntent._extraKey4 = "";
        if (intentExtraKeyValue4.getText() != null)
            ppIntent._extraValue4 = intentExtraKeyValue4.getText().toString();
        else
            ppIntent._extraValue4 = "";
        ppIntent._extraType4 = intentExtraSpinner4.getSelectedItemPosition();

        if (intentExtraKeyName5.getText() != null)
            ppIntent._extraKey5 = intentExtraKeyName5.getText().toString();
        else
            ppIntent._extraKey5 = "";
        if (intentExtraKeyValue5.getText() != null)
            ppIntent._extraValue5 = intentExtraKeyValue5.getText().toString();
        else
            ppIntent._extraValue5 = "";
        ppIntent._extraType5 = intentExtraSpinner5.getSelectedItemPosition();
    }

    private void enableOKButton() {
        boolean enableOK = (!intentNameEditText.getText().toString().isEmpty());
        okButton.setEnabled(enableOK);
    }

    static Intent createIntent(PPIntent ppIntent) {
        Intent intent;
        try {
            intent = new Intent();
            if ((ppIntent._packageName != null) && (!ppIntent._packageName.isEmpty()) &&
                    (ppIntent._className != null) && (!ppIntent._className.isEmpty()))
                intent.setComponent(new ComponentName(ppIntent._packageName, ppIntent._className));

            if ((ppIntent._action != null) && (!ppIntent._action.isEmpty()))
                intent.setAction(ppIntent._action);

            if ((ppIntent._data != null) && (!ppIntent._data.isEmpty()))
                intent.setData(Uri.parse(ppIntent._data));

            if ((ppIntent._mimeType != null) && (!ppIntent._mimeType.isEmpty()))
                intent.setType(ppIntent._mimeType);

            if ((ppIntent._categories != null) && (!ppIntent._categories.isEmpty())) {
                String[] splits = ppIntent._categories.split("\\|");
                for (String category : splits) {
                    switch (category) {
                        case "CATEGORY_ALTERNATIVE":
                            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                            break;
                        case "CATEGORY_APP_BROWSER":
                            intent.addCategory(Intent.CATEGORY_APP_BROWSER);
                            break;
                        case "CATEGORY_APP_CALCULATOR":
                            intent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
                            break;
                        case "CATEGORY_APP_CALENDAR":
                            intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                            break;
                        case "CATEGORY_APP_CONTACTS":
                            intent.addCategory(Intent.CATEGORY_APP_CONTACTS);
                            break;
                        case "CATEGORY_APP_EMAIL":
                            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                            break;
                        case "CATEGORY_APP_GALLERY":
                            intent.addCategory(Intent.CATEGORY_APP_GALLERY);
                            break;
                        case "CATEGORY_APP_MAPS":
                            intent.addCategory(Intent.CATEGORY_APP_MAPS);
                            break;
                        case "CATEGORY_APP_MARKET":
                            intent.addCategory(Intent.CATEGORY_APP_MARKET);
                            break;
                        case "CATEGORY_APP_MESSAGING":
                            intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                            break;
                        case "CATEGORY_APP_MUSIC":
                            intent.addCategory(Intent.CATEGORY_APP_MUSIC);
                            break;
                        case "CATEGORY_BROWSABLE":
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            break;
                        case "CATEGORY_CAR_DOCK":
                            intent.addCategory(Intent.CATEGORY_CAR_DOCK);
                            break;
                        case "CATEGORY_CAR_MODE":
                            intent.addCategory(Intent.CATEGORY_CAR_MODE);
                            break;
                        case "CATEGORY_DEFAULT":
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            break;
                        case "CATEGORY_DESK_DOCK":
                            intent.addCategory(Intent.CATEGORY_DESK_DOCK);
                            break;
                        case "CATEGORY_DEVELOPMENT_PREFERENCE":
                            intent.addCategory(Intent.CATEGORY_DEVELOPMENT_PREFERENCE);
                            break;
                        case "CATEGORY_EMBED":
                            intent.addCategory(Intent.CATEGORY_EMBED);
                            break;
                        case "CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST":
                            intent.addCategory(Intent.CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST);
                            break;
                        case "CATEGORY_HE_DESK_DOCK":
                            intent.addCategory(Intent.CATEGORY_HE_DESK_DOCK);
                            break;
                        case "CATEGORY_HOME":
                            intent.addCategory(Intent.CATEGORY_HOME);
                            break;
                        case "CATEGORY_INFO":
                            intent.addCategory(Intent.CATEGORY_INFO);
                            break;
                        case "CATEGORY_LAUNCHER":
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            break;
                        case "CATEGORY_LEANBACK_LAUNCHER":
                            intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                            break;
                        case "CATEGORY_LE_DESK_DOCK":
                            intent.addCategory(Intent.CATEGORY_LE_DESK_DOCK);
                            break;
                        case "CATEGORY_MONKEY":
                            intent.addCategory(Intent.CATEGORY_MONKEY);
                            break;
                        case "CATEGORY_OPENABLE":
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            break;
                        case "CATEGORY_PREFERENCE":
                            intent.addCategory(Intent.CATEGORY_PREFERENCE);
                            break;
                        case "CATEGORY_SAMPLE_CODE":
                            intent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
                            break;
                        case "CATEGORY_SELECTED_ALTERNATIVE":
                            intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
                            break;
                        case "CATEGORY_TAB":
                            intent.addCategory(Intent.CATEGORY_TAB);
                            break;
                        case "CATEGORY_TEST":
                            intent.addCategory(Intent.CATEGORY_TEST);
                            break;
                        case "CATEGORY_TYPED_OPENABLE":
                            intent.addCategory(Intent.CATEGORY_TYPED_OPENABLE);
                            break;
                        case "CATEGORY_UNIT_TEST":
                            intent.addCategory(Intent.CATEGORY_UNIT_TEST);
                            break;
                        case "CATEGORY_VOICE":
                            intent.addCategory(Intent.CATEGORY_VOICE);
                            break;
                        case "CATEGORY_VR_HOME":
                            intent.addCategory(Intent.CATEGORY_VR_HOME);
                            break;
                    }
                }
            }

            if ((ppIntent._flags != null) && (!ppIntent._flags.isEmpty())) {
                String[] splits = ppIntent._flags.split("\\|");
                for (String flag : splits) {
                    switch (flag) {
                        case "FLAG_ACTIVITY_BROUGHT_TO_FRONT":
                            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            break;
                        case "FLAG_ACTIVITY_CLEAR_TASK":
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            break;
                        case "FLAG_ACTIVITY_CLEAR_TOP":
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            break;
                        //case "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET":
                        //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        //    break;
                        case "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS":
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            break;
                        case "FLAG_ACTIVITY_FORWARD_RESULT":
                            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            break;
                        case "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY":
                            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                            break;
                        case "FLAG_ACTIVITY_LAUNCH_ADJACENT":
                            //if (Build.VERSION.SDK_INT >= 24)
                                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
                            break;
                        case "FLAG_ACTIVITY_MATCH_EXTERNAL":
                            if (Build.VERSION.SDK_INT >= 28)
                                intent.addFlags(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
                            break;
                        case "FLAG_ACTIVITY_MULTIPLE_TASK":
                            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            break;
                        case "FLAG_ACTIVITY_NEW_DOCUMENT":
                            //if (Build.VERSION.SDK_INT >= 21)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            break;
                        case "FLAG_ACTIVITY_NEW_TASK":
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            break;
                        case "FLAG_ACTIVITY_NO_ANIMATION":
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            break;
                        case "FLAG_ACTIVITY_NO_HISTORY":
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            break;
                        case "FLAG_ACTIVITY_NO_USER_ACTION":
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            break;
                        case "FLAG_ACTIVITY_PREVIOUS_IS_TOP":
                            intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            break;
                        case "FLAG_ACTIVITY_REORDER_TO_FRONT":
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            break;
                        case "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED":
                            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            break;
                        case "FLAG_ACTIVITY_RETAIN_IN_RECENTS":
                            //if (Build.VERSION.SDK_INT >= 21)
                                intent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
                            break;
                        case "FLAG_ACTIVITY_SINGLE_TOP":
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            break;
                        case "FLAG_ACTIVITY_TASK_ON_HOME":
                            intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            break;
                        case "FLAG_DEBUG_LOG_RESOLUTION":
                            intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
                            break;
                        case "FLAG_EXCLUDE_STOPPED_PACKAGES":
                            intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
                            break;
                        case "FLAG_FROM_BACKGROUND":
                            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                            break;
                        case "FLAG_GRANT_PERSISTABLE_URI_PERMISSION":
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            break;
                        case "FLAG_GRANT_PREFIX_URI_PERMISSION":
                            //if (Build.VERSION.SDK_INT >= 21)
                                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                            break;
                        case "FLAG_GRANT_READ_URI_PERMISSION":
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            break;
                        case "FLAG_GRANT_WRITE_URI_PERMISSION":
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            break;
                        case "FLAG_INCLUDE_STOPPED_PACKAGES":
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            break;
                        case "FLAG_RECEIVER_FOREGROUND":
                            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                            break;
                        case "FLAG_RECEIVER_NO_ABORT":
                            intent.addFlags(Intent.FLAG_RECEIVER_NO_ABORT);
                            break;
                        case "FLAG_RECEIVER_REGISTERED_ONLY":
                            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
                            break;
                        case "FLAG_RECEIVER_REPLACE_PENDING":
                            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                            break;
                        case "FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS":
                            if (Build.VERSION.SDK_INT >= 26)
                                intent.addFlags(Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS);
                            break;
                    }
                }
            }

            if ((ppIntent._extraKey1 != null) && (!ppIntent._extraKey1.isEmpty()) &&
                (ppIntent._extraValue1 != null) && (!ppIntent._extraValue1.isEmpty())) {
                switch (ppIntent._extraType1) {
                    case 0:
                        intent.putExtra(ppIntent._extraKey1, ppIntent._extraValue1);
                        break;
                    case 1:
                        intent.putExtra(ppIntent._extraKey1, Integer.valueOf(ppIntent._extraValue1));
                        break;
                    case 2:
                        intent.putExtra(ppIntent._extraKey1, Long.valueOf(ppIntent._extraValue1));
                        break;
                    case 3:
                        intent.putExtra(ppIntent._extraKey1, Float.valueOf(ppIntent._extraValue1));
                        break;
                    case 4:
                        intent.putExtra(ppIntent._extraKey1, Double.valueOf(ppIntent._extraValue1));
                        break;
                    case 5:
                        boolean value = ppIntent._extraValue1.equalsIgnoreCase("true") || ppIntent._extraValue1.equals("1");
                        intent.putExtra(ppIntent._extraKey1, value);
                        break;
                }
            }
            if ((ppIntent._extraKey2 != null) && (!ppIntent._extraKey2.isEmpty()) &&
                    (ppIntent._extraValue2 != null) && (!ppIntent._extraValue2.isEmpty())) {
                switch (ppIntent._extraType2) {
                    case 0:
                        intent.putExtra(ppIntent._extraKey2, ppIntent._extraValue2);
                        break;
                    case 1:
                        intent.putExtra(ppIntent._extraKey2, Integer.valueOf(ppIntent._extraValue2));
                        break;
                    case 2:
                        intent.putExtra(ppIntent._extraKey2, Long.valueOf(ppIntent._extraValue2));
                        break;
                    case 3:
                        intent.putExtra(ppIntent._extraKey2, Float.valueOf(ppIntent._extraValue2));
                        break;
                    case 4:
                        intent.putExtra(ppIntent._extraKey2, Double.valueOf(ppIntent._extraValue2));
                        break;
                    case 5:
                        boolean value = ppIntent._extraValue2.equalsIgnoreCase("true") || ppIntent._extraValue2.equals("1");
                        intent.putExtra(ppIntent._extraKey2, value);
                        break;
                }
            }
            if ((ppIntent._extraKey3 != null) && (!ppIntent._extraKey3.isEmpty()) &&
                    (ppIntent._extraValue3 != null) && (!ppIntent._extraValue3.isEmpty())) {
                switch (ppIntent._extraType3) {
                    case 0:
                        intent.putExtra(ppIntent._extraKey3, ppIntent._extraValue3);
                        break;
                    case 1:
                        intent.putExtra(ppIntent._extraKey3, Integer.valueOf(ppIntent._extraValue3));
                        break;
                    case 2:
                        intent.putExtra(ppIntent._extraKey3, Long.valueOf(ppIntent._extraValue3));
                        break;
                    case 3:
                        intent.putExtra(ppIntent._extraKey3, Float.valueOf(ppIntent._extraValue3));
                        break;
                    case 4:
                        intent.putExtra(ppIntent._extraKey3, Double.valueOf(ppIntent._extraValue3));
                        break;
                    case 5:
                        boolean value = ppIntent._extraValue3.equalsIgnoreCase("true") || ppIntent._extraValue3.equals("1");
                        intent.putExtra(ppIntent._extraKey3, value);
                        break;
                }
            }
            if ((ppIntent._extraKey4 != null) && (!ppIntent._extraKey4.isEmpty()) &&
                    (ppIntent._extraValue4 != null) && (!ppIntent._extraValue4.isEmpty())) {
                switch (ppIntent._extraType4) {
                    case 0:
                        intent.putExtra(ppIntent._extraKey4, ppIntent._extraValue4);
                        break;
                    case 1:
                        intent.putExtra(ppIntent._extraKey4, Integer.valueOf(ppIntent._extraValue4));
                        break;
                    case 2:
                        intent.putExtra(ppIntent._extraKey4, Long.valueOf(ppIntent._extraValue4));
                        break;
                    case 3:
                        intent.putExtra(ppIntent._extraKey4, Float.valueOf(ppIntent._extraValue4));
                        break;
                    case 4:
                        intent.putExtra(ppIntent._extraKey4, Double.valueOf(ppIntent._extraValue4));
                        break;
                    case 5:
                        boolean value = ppIntent._extraValue4.equalsIgnoreCase("true") || ppIntent._extraValue4.equals("1");
                        intent.putExtra(ppIntent._extraKey4, value);
                        break;
                }
            }
            if ((ppIntent._extraKey5 != null) && (!ppIntent._extraKey5.isEmpty()) &&
                    (ppIntent._extraValue5 != null) && (!ppIntent._extraValue5.isEmpty())) {
                switch (ppIntent._extraType5) {
                    case 0:
                        intent.putExtra(ppIntent._extraKey5, ppIntent._extraValue5);
                        break;
                    case 1:
                        intent.putExtra(ppIntent._extraKey5, Integer.valueOf(ppIntent._extraValue5));
                        break;
                    case 2:
                        intent.putExtra(ppIntent._extraKey5, Long.valueOf(ppIntent._extraValue5));
                        break;
                    case 3:
                        intent.putExtra(ppIntent._extraKey5, Float.valueOf(ppIntent._extraValue5));
                        break;
                    case 4:
                        intent.putExtra(ppIntent._extraKey5, Double.valueOf(ppIntent._extraValue5));
                        break;
                    case 5:
                        boolean value = ppIntent._extraValue5.equalsIgnoreCase("true") || ppIntent._extraValue5.equals("1");
                        intent.putExtra(ppIntent._extraKey5, value);
                        break;
                }
            }
        } catch (Exception e) {
            intent = null;
        }

        //PPApplication.logE("ApplicationEditorIntentActivity.createIntent", "intent="+intent);
        return intent;
    }

}
