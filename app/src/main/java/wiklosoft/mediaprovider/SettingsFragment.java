package wiklosoft.mediaprovider;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.AttributeSet;

import java.util.List;

import wiklosoft.mediaprovider.providers.MusicProvider;

/**
 * Created by Pawel Wiklowski on 08.11.15.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen screen = getPreferenceScreen();

        MusicService service =   MusicService.getService();
        final List<MusicProvider> providers = service.getMusicProviders();

        for(int i=0; i<providers.size();i++) {
            final MusicProvider provider = providers.get(i);

            Preference p = new Preference(getContext());
            p.setTitle(providers.get(i).getName());

            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    transaction.replace(R.id.container, provider.getSettingsFragment()).addToBackStack("").commit();

                    return false;
                }
            });

            screen.addPreference(p);



        }

    }


}
