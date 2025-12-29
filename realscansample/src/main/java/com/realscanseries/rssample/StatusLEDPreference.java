package com.realscanseries.rssample;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.realscanseries.rsalib.util.Logger;

public class StatusLEDPreference extends Preference {

    private Spinner spinner;
    private OnStatusLedApplyButtonClickListener onStatusLedApplyButtonClickListener;
    private String[] spinnerEntries;
    private String[] spinnerValues;
    private Button applyButton;

    public StatusLEDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_status_led_with_spinners);

        // Spinner 항목 및 값 정의
        spinnerEntries = context.getResources().getStringArray(R.array.mode_led_options_3);
        spinnerValues = context.getResources().getStringArray(R.array.mode_led_values_3);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        Button actionButton = (Button) holder.findViewById(R.id.button_statusled_apply);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("button_statusled_apply click ");
                if (onStatusLedApplyButtonClickListener != null) {
                    Logger.d("button_statusled_apply click onStatusColorModeApplyButtonClick");
                    onStatusLedApplyButtonClickListener.onStatusColorModeApplyButtonClick(
                            getSelectedSpinnerValue());
                }
            }
        });

        spinner = (Spinner) holder.findViewById(R.id.spinner_status_led);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, spinnerEntries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 기존 저장된 값 설정
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String savedValue = sharedPreferences.getString(getKey(), getDefaultValue());
        int savedIndex = getSavedIndex(savedValue);
        spinner.setSelection(savedIndex);
    }

    // 기본값 설정 (첫 번째 값을 기본값으로 사용)
    private String getDefaultValue() {
        return spinnerValues[0];
    }

    // 저장된 값의 인덱스를 찾는 메서드
    private int getSavedIndex(String value) {
        for (int i = 0; i < spinnerValues.length; i++) {
            if (spinnerValues[i].equals(value)) {
                return i;
            }
        }
        return 0; // 기본값으로 첫 번째 항목
    }

    // 선택된 값을 SharedPreferences에 저장
    private void saveSelectedValue(String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(getKey(), value);
        editor.apply();
    }

    public int getSelectedSpinnerValue() {
        int selectedIndex = spinner.getSelectedItemPosition();
        return Integer.parseInt(spinnerValues[selectedIndex]); // Convert String value to int
    }

    public interface OnStatusLedApplyButtonClickListener {
        void onStatusColorModeApplyButtonClick(int color);
    }

    // Set listener through this method in MainActivity
    public void setOnStatusLedApplyButtonClickListener(OnStatusLedApplyButtonClickListener listener) {
        this.onStatusLedApplyButtonClickListener = listener;
    }
}

