package com.realscanseries.rssample;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.realscanseries.rsalib.util.Logger;


public class ModeLEDPreference extends Preference {

    private Spinner[] spinners = new Spinner[3];
    private OnModeLedApplyButtonClickListener onModeLedApplyButtonClickListener;
    private String[][] spinnerEntries;
    private String[][] spinnerValues;

    public ModeLEDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_modeled_with_spinners);

        spinnerEntries = new String[][]{
                context.getResources().getStringArray(R.array.mode_led_options_1), // ON, OFF
                context.getResources().getStringArray(R.array.mode_led_options_2),
                context.getResources().getStringArray(R.array.mode_led_options_3)
        };

        spinnerValues = new String[][]{
                context.getResources().getStringArray(R.array.mode_led_values_1),
                context.getResources().getStringArray(R.array.mode_led_values_2),
                context.getResources().getStringArray(R.array.mode_led_values_3)
        };
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        Button actionButton = (Button) holder.findViewById(R.id.button_modeled_apply);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("button_modeled_apply click ");
                if (onModeLedApplyButtonClickListener != null) {
                    Logger.d("button_modeled_apply click onLedColorModeApplyButtonClick");
                    onModeLedApplyButtonClickListener.onLedColorModeApplyButtonClick(
                            getSelectedSpinnerValue(0),
                            getSelectedSpinnerValue(1),
                            getSelectedSpinnerValue(2));
                }
            }
        });

        LinearLayout layout = (LinearLayout) holder.findViewById(R.id.spinner_modeled_container);
        layout.removeAllViews();  // Delete an existing view

        for (int i = 0; i < 3; i++) {
            Spinner spinner = new Spinner(getContext());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, spinnerEntries[i]);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinners[i] = spinner;

            // Set Spinner selection value
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String combinedValue = sharedPreferences.getString(getKey(), getDefaultCombinedValue());
            String[] values = combinedValue.split("-");
            spinner.setSelection(Integer.parseInt(values[i]) - 1);

            //Set listener on first spinner

            if (isEnabled() && i == 0) {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Disable second and third spinners when "OFF" (=2) is selected in first spinner
                        if (position == 1) { // OFF is selected
                            spinners[1].setEnabled(false);
                            spinners[2].setEnabled(false);
                            spinners[1].setSelection(0);
                            spinners[2].setSelection(0);
                        } else {
                            spinners[1].setEnabled(true);
                            spinners[2].setEnabled(true);
                        }
                        savePreference();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            } else {
                spinner.setEnabled(isEnabled());
            }

            // LayoutParams를 설정하여 각 Spinner가 동일한 공간을 차지하도록 설정
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            spinner.setLayoutParams(params);

            layout.addView(spinner); // Spinner 추가
        }
    }

    private void savePreference() {
        // Combine selected values ​​and save them as one string
        StringBuilder combinedValue = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            String value = spinnerValues[i][spinners[i].getSelectedItemPosition()];
            if (i > 0) {
                combinedValue.append("-");
            }
            combinedValue.append(value);
        }

        //Save to SharedPreferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(getKey(), combinedValue.toString());
        editor.apply();
    }

    //
    //Create default values (ON-1-1, etc.)
    private String getDefaultCombinedValue() {
        return "1-1-1";  // Default value for each spinner (ON, first options)
    }

    public int getSelectedSpinnerValue(int spinnerIndex) {
        if (spinnerIndex < 0 || spinnerIndex >= spinners.length) {
            throw new IllegalArgumentException("Invalid spinner index");
        }

        // Retrieves the location (index) of the currently selected item.
        int selectedPosition = spinners[spinnerIndex].getSelectedItemPosition();

        // Get the VALUE value of that position from the array (VALUE value is String, so convert to int)
        return Integer.parseInt(spinnerValues[spinnerIndex][selectedPosition]);
    }

    public interface OnModeLedApplyButtonClickListener {
        void onLedColorModeApplyButtonClick(int onoff, int ledmode, int color);
    }

    // Set listener through this method in MainActivity
    public void setOnModeLEDApplyButtonClickListener(OnModeLedApplyButtonClickListener listener) {
        this.onModeLedApplyButtonClickListener = listener;
    }
}



