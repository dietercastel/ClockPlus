package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.aospdatetimepicker.Utils;
import com.philliphsu.clock2.util.AlarmController;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;

/**
 * Created by Phillip Hsu on 7/31/2016.
 */
public class ExpandedAlarmViewHolder extends BaseAlarmViewHolder {
    private static final String TAG = "ExpandedAlarmViewHolder";

    @Bind(R.id.ok) Button mOk;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;

    private final ColorStateList mDayToggleColors;

    public ExpandedAlarmViewHolder(ViewGroup parent, final OnListItemInteractionListener<Alarm> listener,
                                   AlarmController controller) {
        super(parent, R.layout.item_expanded_alarm, listener, controller);
        // Manually bind listeners, or else you'd need to write a getter for the
        // OnListItemInteractionListener in the BaseViewHolder for use in method binding.
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListItemDeleted(getAlarm());
            }
        });

        // TODO: We can now do method binding instead, because our superclass provides an API
        // to retrieve the interaction listener.
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAlarmAndWriteToDb();
            }
        });

        // https://code.google.com/p/android/issues/detail?id=177282
        // https://stackoverflow.com/questions/15673449/is-it-confirmed-that-i-cannot-use-themed-color-attribute-in-color-state-list-res
        // Programmatically create the ColorStateList for our day toggles using themed color
        // attributes, "since prior to M you can't create a themed ColorStateList from XML but you
        // can from code." (quote from google)
        // The first array level is analogous to an XML node defining an item with a state list.
        // The second level lists all the states considered by the item from the first level.
        // An empty list of states represents the default stateless item.
        int[][] states = {
                /*item 1*/{/*states*/android.R.attr.state_checked},
                /*item 2*/{/*states*/}
        };
        // TODO: Phase out Utils.getColorFromThemeAttr because it doesn't work for text colors.
        // WHereas getTextColorFromThemeAttr works for both regular colors and text colors.
        int[] colors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), android.R.attr.textColorHint)
        };
        mDayToggleColors = new ColorStateList(states, colors);
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindDays(alarm);
        bindRingtone();
        bindVibrate(alarm.vibrates());
    }

    @Override
    protected void bindLabel(boolean visible, String label) {
        super.bindLabel(true, label);
    }

    @OnClick(R.id.ok)
    void save() {
        // TODO
    }

//    @OnClick(R.id.delete)
//    void delete() {
//        // TODO
//    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                // The ringtone to show as selected when the dialog is opened
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSelectedRingtoneUri())
                // Whether to show "Default" item in the list
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        // The ringtone that plays when default option is selected
        //.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, DEFAULT_TONE);
        // TODO: This is VERY BAD. Use a Controller/Presenter instead.
        // The result will be delivered to MainActivity, and then delegated to AlarmsFragment.
        ((Activity) getContext()).startActivityForResult(intent, AlarmsFragment.REQUEST_PICK_RINGTONE);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // We didn't have to write code like this in EditAlarmActivity, because we never committed
    // any changes until the user explicitly clicked save. We have to do this here now because
    // we should commit changes as they are made.
    @OnCheckedChanged(R.id.vibrate)
    void onVibrateToggled() {
        // TODO
    }

    @OnCheckedChanged({ R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 })
    void onDayToggled() {
        // TODO
        Log.d("yooo", "Hello!");
    }
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void bindDays(Alarm alarm) {
        for (int i = 0; i < mDays.length; i++) {
            mDays[i].setTextColor(mDayToggleColors);
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(alarm.isRecurring(weekDay));
        }
    }

    private void bindRingtone() {
        int iconTint = Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint);

        Drawable ringtoneIcon = mRingtone.getCompoundDrawablesRelative()[0/*start*/];
        ringtoneIcon = DrawableCompat.wrap(ringtoneIcon.mutate());
        DrawableCompat.setTint(ringtoneIcon, iconTint);
        mRingtone.setCompoundDrawablesRelativeWithIntrinsicBounds(ringtoneIcon, null, null, null);

        String title = RingtoneManager.getRingtone(getContext(),
                getSelectedRingtoneUri()).getTitle(getContext());
        mRingtone.setText(title);
    }

    private void bindVibrate(boolean vibrates) {
        mVibrate.setChecked(vibrates);
    }

    private Uri getSelectedRingtoneUri() {
        // If showing an item for "Default" (@see EXTRA_RINGTONE_SHOW_DEFAULT), this can be one
        // of DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI to have the
        // "Default" item checked.
        //
        // Otherwise, use RingtoneManager.getActualDefaultRingtoneUri() to get the "actual sound URI".
        //
        // Do not use RingtoneManager.getDefaultUri(), because that just returns one of
        // DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI
        // depending on the type requested (i.e. what the docs calls "symbolic URI
        // which will resolved to the actual sound when played").
        String ringtone = getAlarm().ringtone();
        return ringtone.isEmpty() ?
                RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM)
                : Uri.parse(ringtone);
    }

    private boolean isRecurringDay(int weekDay) {
        // What position in the week is this day located at?
        int pos = DaysOfWeek.getInstance(getContext()).positionOf(weekDay);
        // Return the state of this day according to its button
        return mDays[pos].isChecked();
    }

    private void createNewAlarmAndWriteToDb() {
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = Alarm.builder()
                .hour(oldAlarm.hour()/*TODO*/)
                .minutes(oldAlarm.minutes()/*TODO*/)
                .label(mLabel.getText().toString())
                .ringtone(""/*TODO*/)
                .vibrates(mVibrate.isChecked())
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        // ----------------------------------------------
        // TOneverDO: precede copyMutableFieldsTo()
        newAlarm.setEnabled(mSwitch.isChecked());
        for (int i = SUNDAY; i <= SATURDAY; i++) {
            newAlarm.setRecurring(i, isRecurringDay(i));
        }
        // ----------------------------------------------
        getInteractionListener().onListItemUpdate(newAlarm, getAdapterPosition());
    }
}
