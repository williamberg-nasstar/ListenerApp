package nu.mine.wberg.listenerapp.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

/**
 * http://stackoverflow.com/a/16071426
 */
public class GRadioGroup {

    List<RadioButton> radios = new ArrayList<>();

    /**
     * Constructor, which allows you to pass number of RadioButton instances,
     * making a group.
     *
     * @param radios
     *            One RadioButton or more.
     */
    public GRadioGroup(RadioButton... radios) {
        super();

        for (RadioButton rb : radios) {
            this.radios.add(rb);
            rb.setOnClickListener(onClick);
        }
    }

    OnClickListener onClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            for (RadioButton rb : radios) {
                rb.setChecked(false);
            }
            if (v instanceof RadioButton) {
                ((RadioButton) v).setChecked(true);
            }
        }
    };

}
