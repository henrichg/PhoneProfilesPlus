package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Arrays;

public class ApplicationEditorIntentActivity extends AppCompatActivity {

    private Application application = null;
    private PPIntent ppIntent = null;

    private int startApplicationDelay;

    Button okButton;

    private EditText intentNameEditText;
    private EditText intentPackageName;
    private EditText intentClassName;
    private Spinner intentActionSpinner;
    private EditText intentActionEdit;
    private EditText intentData;
    private EditText intentMimeType;

    String[] actionsArray;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.intent_editor_title);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        Intent intent = getIntent();
        application = intent.getParcelableExtra(ApplicationEditorDialog.EXTRA_APPLICATION);
        ppIntent = intent.getParcelableExtra(ApplicationEditorDialog.EXTRA_PP_INTENT);
        if (ppIntent == null)
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent=null");
        else
            PPApplication.logE("ApplicationEditorIntentActivity.onCreate", "ppIntent._id="+ppIntent._id);

        startApplicationDelay = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0);

        okButton = findViewById(R.id.application_editor_intent_ok);

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
        }
        else {
            intentActionEdit.setEnabled(false);
        }

        enableOKButton();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ppIntent._name = intentNameEditText.getText().toString();
                if (application != null)
                    application.appLabel = ppIntent._name;
                ppIntent._packageName = intentPackageName.getText().toString();
                ppIntent._className = intentClassName.getText().toString();
                ppIntent._data = intentData.getText().toString();
                ppIntent._mimeType = intentMimeType.getText().toString();

                int actionSpinnerId = intentActionSpinner.getSelectedItemPosition();
                if (actionSpinnerId == 0)
                    ppIntent._action = "";
                else
                if (actionSpinnerId == 1)
                    ppIntent._action = intentActionEdit.getText().toString();
                else {
                    ppIntent._action = actionsArray[actionSpinnerId];
                }

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

    }

    private void enableOKButton() {
        boolean enableOK = (!intentNameEditText.getText().toString().isEmpty());
        okButton.setEnabled(enableOK);
    }

}
