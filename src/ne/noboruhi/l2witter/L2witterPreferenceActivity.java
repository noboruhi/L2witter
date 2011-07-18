package ne.noboruhi.l2witter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class L2witterPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        ListPreference listPreference = (ListPreference)getPreferenceScreen().findPreference(getString(R.string.pref_stream_config_key));
        EditTextPreference edittextPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.pref_hashtag_config_key));
        edittextPreference.setSummary(edittextPreference.getText());
        String value = listPreference.getValue();
        if ("2".equals(value)) {
            edittextPreference.setEnabled(true);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        setResult(RESULT_FIRST_USER + 1);

        if(getString(R.string.pref_stream_config_key).equals(key)) {
            EditTextPreference edittextPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.pref_hashtag_config_key));
            String value = sharedPreferences.getString(key,"1");
            if ("2".equals(value)) {
                edittextPreference.setEnabled(true);
                String hashtag =  sharedPreferences.getString(getString(R.string.pref_hashtag_config_key), "#l2witter");
                edittextPreference.setSummary(hashtag);
            } else {
                edittextPreference.setEnabled(false);
            }
        } else if (getString(R.string.pref_stream_config_key).equals(key)) {
            String hashtagConfigKey = getString(R.string.pref_hashtag_config_key);
            EditTextPreference edittextPreference = (EditTextPreference)getPreferenceScreen().findPreference(hashtagConfigKey);
            String hashtag =  sharedPreferences.getString(getString(R.string.pref_hashtag_config_key), "#l2witter");
            edittextPreference.setSummary(hashtag);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
