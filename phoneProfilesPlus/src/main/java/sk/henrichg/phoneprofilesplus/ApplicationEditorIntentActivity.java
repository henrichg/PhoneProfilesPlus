package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationEditorIntentActivity extends AppCompatActivity {

    private Application application = null;
    private PPIntent ppIntent = null;

    private int startApplicationDelay;

    Button okButton;

    //private ScrollView intentScrollView;
    private EditText intentNameEditText;
    private EditText intentPackageName;
    private EditText intentClassName;
    private Spinner intentActionSpinner;
    private EditText intentActionEdit;
    private EditText intentData;
    private EditText intentMimeType;
    private TextView categoryTextView;
    private TextView flagsTextView;

    private EditText intentExtraKeyName1;
    private EditText intentExtraKeyValue1;
    private Spinner intentExtraSpinner1;
    private EditText intentExtraKeyName2;
    private EditText intentExtraKeyValue2;
    private Spinner intentExtraSpinner2;
    private EditText intentExtraKeyName3;
    private EditText intentExtraKeyValue3;
    private Spinner intentExtraSpinner3;
    private EditText intentExtraKeyName4;
    private EditText intentExtraKeyValue4;
    private Spinner intentExtraSpinner4;
    private EditText intentExtraKeyName5;
    private EditText intentExtraKeyValue5;
    private Spinner intentExtraSpinner5;

    String[] actionsArray;
    String[] categoryArray;
    boolean[] categoryIndices;
    String[] flagArray;
    boolean[] flagIndices;

    public static final String EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY = "dialogPreferenceStartApplicationDelay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_application_editor_intent);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
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

        /*
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.intent_editor_title);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }*/

        Intent intent = getIntent();
        application = intent.getParcelableExtra(ApplicationEditorDialog.EXTRA_APPLICATION);
        ppIntent = intent.getParcelableExtra(ApplicationEditorDialog.EXTRA_PP_INTENT);
        if (ppIntent == null)
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent=null");
        else
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent._id="+ppIntent._id);

        startApplicationDelay = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0);

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

        intentPackageName = findViewById(R.id.application_editor_intent_package_name);
        intentClassName = findViewById(R.id.application_editor_intent_class_name);
        intentData = findViewById(R.id.application_editor_intent_data);
        intentMimeType = findViewById(R.id.application_editor_intent_mime_type);

        intentActionSpinner = findViewById(R.id.application_editor_intent_action_spinner);
        intentActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
        intentCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.application_editor_intent_categories_dlg_title)
                                //.setIcon(getDialogIcon())
                                .setMultiChoiceItems(R.array.applicationEditorIntentCategoryArray, categoryIndices,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        categoryIndices[which] = isChecked;
                                    }
                                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //AlertDialog mDialog = builder.create();
                if (!isFinishing())
                    builder.show();
            }
        });

        flagsTextView = findViewById(R.id.application_editor_intent_flags_value);
        flagArray = getResources().getStringArray(R.array.applicationEditorIntentFlagArray);
        flagIndices = new boolean[flagArray.length];
        AppCompatImageButton intentFlagsButton = findViewById(R.id.application_editor_intent_flags_btn);
        intentFlagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.application_editor_intent_flags_dlg_title)
                                //.setIcon(getDialogIcon())
                                .setMultiChoiceItems(R.array.applicationEditorIntentFlagArray, flagIndices, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        flagIndices[which] = isChecked;
                                    }
                                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog mDialog = builder.create();
                if (!isFinishing())
                    mDialog.show();
            }
        });

        intentExtraKeyName1 = findViewById(R.id.application_editor_intent_extra_key_1);
        intentExtraKeyValue1 = findViewById(R.id.application_editor_intent_extra_value_1);
        intentExtraSpinner1  = findViewById(R.id.application_editor_intent_extra_type_spinner_1);
        intentExtraKeyName2 = findViewById(R.id.application_editor_intent_extra_key_2);
        intentExtraKeyValue2 = findViewById(R.id.application_editor_intent_extra_value_2);
        intentExtraSpinner2  = findViewById(R.id.application_editor_intent_extra_type_spinner_2);
        intentExtraKeyName3 = findViewById(R.id.application_editor_intent_extra_key_3);
        intentExtraKeyValue3 = findViewById(R.id.application_editor_intent_extra_value_3);
        intentExtraSpinner3  = findViewById(R.id.application_editor_intent_extra_type_spinner_3);
        intentExtraKeyName4 = findViewById(R.id.application_editor_intent_extra_key_4);
        intentExtraKeyValue4 = findViewById(R.id.application_editor_intent_extra_value_4);
        intentExtraSpinner4  = findViewById(R.id.application_editor_intent_extra_type_spinner_4);
        intentExtraKeyName5 = findViewById(R.id.application_editor_intent_extra_key_5);
        intentExtraKeyValue5 = findViewById(R.id.application_editor_intent_extra_value_5);
        intentExtraSpinner5  = findViewById(R.id.application_editor_intent_extra_type_spinner_5);

        actionsArray = getResources().getStringArray(R.array.applicationEditorIntentActionArray);
        if (ppIntent != null) {
            intentNameEditText.setText(ppIntent._name);
            intentPackageName.setText(ppIntent._packageName);
            intentClassName.setText(ppIntent._className);
            intentData.setText(ppIntent._data);
            intentMimeType.setText(ppIntent._mimeType);

            if ((ppIntent._action == null) || ppIntent._action.isEmpty()) {
                intentActionSpinner.setSelection(0);
                intentActionEdit.setText(R.string.empty_string);
                intentActionEdit.setEnabled(false);
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
                    intentActionEdit.setEnabled(false);
                } else {
                    intentActionSpinner.setSelection(Arrays.asList(actionsArray).indexOf(ppIntent._action));
                    intentActionEdit.setText(R.string.empty_string);
                    intentActionEdit.setEnabled(false);
                }
            }

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
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIntent();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ApplicationEditorDialog.EXTRA_PP_INTENT, ppIntent);
                returnIntent.putExtra(ApplicationEditorDialog.EXTRA_APPLICATION, application);
                returnIntent.putExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        Button cancelButton = findViewById(R.id.application_editor_intent_cancel);
        //cancelButton.setAllCaps(false);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        Button testButton = findViewById(R.id.application_editor_intent_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIntent();
                Intent testIntent = createIntent(ppIntent);
                boolean ok = false;
                if (testIntent != null) {
                    try {
                        testIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(testIntent);
                        ok = true;
                    } catch (Exception ignored) {}
                }
                if (!ok) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.application_editor_intent_test_title)
                                    //.setIcon(getDialogIcon())
                                    .setMessage(R.string.application_editor_intent_test_bad_data);
                    builder.setPositiveButton(android.R.string.ok, null);

                    AlertDialog mDialog = builder.create();
                    if (!isFinishing())
                        mDialog.show();
                }
            }
        });
    }

    private void saveIntent() {
        ppIntent._name = intentNameEditText.getText().toString();
        if (application != null)
            application.appLabel = ppIntent._name;
        ppIntent._packageName = intentPackageName.getText().toString().replaceAll(" ","");
        ppIntent._className = intentClassName.getText().toString().replaceAll(" ","");
        ppIntent._data = intentData.getText().toString();
        ppIntent._mimeType = intentMimeType.getText().toString().replaceAll(" ","");

        int actionSpinnerId = intentActionSpinner.getSelectedItemPosition();
        if (actionSpinnerId == 0)
            ppIntent._action = "";
        else
        if (actionSpinnerId == 1)
            ppIntent._action = intentActionEdit.getText().toString();
        else {
            ppIntent._action = actionsArray[actionSpinnerId];
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

        ppIntent._extraKey1 = intentExtraKeyName1.getText().toString();
        ppIntent._extraValue1 = intentExtraKeyValue1.getText().toString();
        ppIntent._extraType1 = intentExtraSpinner1.getSelectedItemPosition();
        ppIntent._extraKey2 = intentExtraKeyName2.getText().toString();
        ppIntent._extraValue2 = intentExtraKeyValue2.getText().toString();
        ppIntent._extraType2 = intentExtraSpinner2.getSelectedItemPosition();
        ppIntent._extraKey3 = intentExtraKeyName3.getText().toString();
        ppIntent._extraValue3 = intentExtraKeyValue3.getText().toString();
        ppIntent._extraType3 = intentExtraSpinner3.getSelectedItemPosition();
        ppIntent._extraKey4 = intentExtraKeyName4.getText().toString();
        ppIntent._extraValue4 = intentExtraKeyValue4.getText().toString();
        ppIntent._extraType4 = intentExtraSpinner4.getSelectedItemPosition();
        ppIntent._extraKey5 = intentExtraKeyName5.getText().toString();
        ppIntent._extraValue5 = intentExtraKeyValue5.getText().toString();
        ppIntent._extraType5 = intentExtraSpinner5.getSelectedItemPosition();
    }

    private void enableOKButton() {
        boolean enableOK = (!intentNameEditText.getText().toString().isEmpty());
        okButton.setEnabled(enableOK);
    }

    static Intent createIntent(PPIntent ppIntent) {
        /*
        intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.kuma.smartnotify","com.kuma.smartnotify.SmartNotifyMain"));
        intent.putExtra("CARMODEON",true);
        intent.putExtra("CARMODEOFF",true);
        */

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
                    if (!category.isEmpty())
                        intent.addCategory(category);
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
                        case "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET":
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            break;
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
                            if (Build.VERSION.SDK_INT >= 24)
                                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
                            break;
                        case "FLAG_ACTIVITY_MATCH_EXTERNAL":
                            intent.addFlags(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
                            break;
                        case "FLAG_ACTIVITY_MULTIPLE_TASK":
                            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            break;
                        case "FLAG_ACTIVITY_NEW_DOCUMENT":
                            if (Build.VERSION.SDK_INT >= 21)
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
                            if (Build.VERSION.SDK_INT >= 21)
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
                            if (Build.VERSION.SDK_INT >= 21)
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

        PPApplication.logE("ApplicationEditorIntentActivity.createIntent", "intent="+intent);
        return intent;
    }

}
