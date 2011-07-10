package ne.noboruhi.l2witter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class L2witterPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

}
