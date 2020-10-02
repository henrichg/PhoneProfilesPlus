package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class GitHubAssetsScreenshotActivity extends AppCompatActivity {

    static final String EXTRA_IMAGE = "image";

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_github_assets_screenshot);
        setTitle("GitHub \"Assets\" " + getString(R.string.github_assets_screenshot_label));

        ImageView imageView = findViewById(R.id.github_assets_screenshot_activity_image);
        imageView.setContentDescription("GitHub \"Assets\" " + getString(R.string.github_assets_screenshot_label));
        int image = getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.ic_empty);
        imageView.setImageResource(image);

    }

}
