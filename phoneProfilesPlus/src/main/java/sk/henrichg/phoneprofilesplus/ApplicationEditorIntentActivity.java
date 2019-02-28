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
import android.widget.Button;
import android.widget.EditText;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ApplicationEditorIntentActivity extends AppCompatActivity {

    private Application application = null;
    private PPIntent ppIntent = null;

    private int startApplicationDelay;

    Button okButton;
    private EditText intentNameEditText;

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

        intentNameEditText = findViewById(R.id.application_editor_intent_intent_name);
        if (ppIntent != null)
            intentNameEditText.setText(ppIntent._name);
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

        okButton = findViewById(R.id.application_editor_intent_ok);
        enableOKButton();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ppIntent._name = intentNameEditText.getText().toString();
                if (application != null)
                    application.appLabel = ppIntent._name;

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
