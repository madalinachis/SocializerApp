package licenta.socializerapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.util.Collections;
import java.util.List;

import licenta.socializerapp.Application;
import licenta.socializerapp.R;

/**
 * Activity that displays the settings screen.
 */
public class SettingsActivity extends AppCompatActivity {

    private List<Float> availableOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        float currentSearchDistance = Application.getSearchDistance();
        if (!availableOptions.contains(currentSearchDistance)) {
            availableOptions.add(currentSearchDistance);
        }
        Collections.sort(availableOptions);

        RadioGroup searchDistanceRadioGroup = (RadioGroup) findViewById(R.id.searchdistance_radiogroup);

        for (int index = 0; index < availableOptions.size(); index++) {
            float searchDistance = availableOptions.get(index);

            RadioButton button = new RadioButton(this);
            button.setId(index);
            button.setText(getString(R.string.settings_distance_format, (int) searchDistance));
            if (searchDistanceRadioGroup != null) {
                searchDistanceRadioGroup.addView(button, index);
            }

            if (currentSearchDistance == searchDistance) {
                if (searchDistanceRadioGroup != null) {
                    searchDistanceRadioGroup.check(index);
                }
            }
        }

        if (searchDistanceRadioGroup != null) {
            searchDistanceRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Application.setSearchDistance(availableOptions.get(checkedId));
                }
            });
        }

    }
}