package com.raistlin.autosimulator.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.raistlin.autosimulator.R;
import com.raistlin.autosimulator.logic.data.OptionsData;

public class OptionsDialog extends DialogFragment {

    private static final String KEY_OPTIONS_DATA = "options_data";

    public interface OptionsDialogResult {
        void onResult(OptionsData data);
    }

    private OptionsDialogResult mResult;
    private EditText mMinSpeed;
    private EditText mMaxSpeed;
    private EditText mLinesCount;
    private EditText mAutoGeneration;
    private EditText mAutoForceStopSpeed;
    private EditText mAutoForceStopLength;
    private EditText mAutoSpeedStop;
    private EditText mAutoSpeedBoost;

    public static OptionsDialog newInstance(OptionsData options) {
        OptionsDialog dialog = new OptionsDialog();
        Bundle args = new Bundle();
        args.putSerializable(KEY_OPTIONS_DATA, options);
        dialog.setArguments(args);
        return dialog;
    }

    public OptionsDialog setOnDialogResult(OptionsDialogResult result) {
        mResult = result;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppDialog);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_options, container, false);
        getDialog().setTitle(R.string.options_dialog_title);

        OptionsData options = (OptionsData) getArguments().getSerializable(KEY_OPTIONS_DATA);
        mMinSpeed = (EditText) view.findViewById(R.id.options_min_speed);
        mMinSpeed.setText(String.valueOf(options.AutoMinSpeed));
        mMaxSpeed = (EditText) view.findViewById(R.id.options_max_speed);
        mMaxSpeed.setText(String.valueOf(options.AutoMaxSpeed));
        mLinesCount = (EditText) view.findViewById(R.id.options_lines_count);
        mLinesCount.setText(String.valueOf(options.LinesCount));
        mAutoGeneration = (EditText) view.findViewById(R.id.options_auto_generation);
        mAutoGeneration.setText(String.valueOf(options.GeneratorFrequency));
        mAutoForceStopSpeed = (EditText) view.findViewById(R.id.options_force_stop_speed);
        mAutoForceStopSpeed.setText(String.valueOf(options.AutoForceStopSpeed));
        mAutoForceStopLength = (EditText) view.findViewById(R.id.options_force_stop_length);
        mAutoForceStopLength.setText(String.valueOf(options.AutoForceStopLength));
        mAutoSpeedStop = (EditText) view.findViewById(R.id.options_speed_stop);
        mAutoSpeedStop.setText(String.valueOf(options.AutoStopSpeed));
        mAutoSpeedBoost = (EditText) view.findViewById(R.id.options_speed_boost);
        mAutoSpeedBoost.setText(String.valueOf(options.AutoBoostSpeed));

        Button ok = (Button) view.findViewById(R.id.options_button_ok);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                OptionsData data = buildOptionsData();
                mResult.onResult(data);
                dismiss();
            }
        });
        Button cancel = (Button) view.findViewById(R.id.options_button_cancel);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    protected OptionsData buildOptionsData() {
        OptionsData data = new OptionsData();
        data.AutoMinSpeed = Integer.valueOf(mMinSpeed.getText().toString());
        data.AutoMaxSpeed = Integer.valueOf(mMaxSpeed.getText().toString());
        data.LinesCount = Integer.valueOf(mLinesCount.getText().toString());
        data.GeneratorFrequency = Integer.valueOf(mAutoGeneration.getText().toString());
        data.AutoForceStopSpeed = Integer.valueOf(mAutoForceStopSpeed.getText().toString());
        data.AutoForceStopLength = Integer.valueOf(mAutoForceStopLength.getText().toString());
        data.AutoStopSpeed = Integer.valueOf(mAutoSpeedStop.getText().toString());
        data.AutoBoostSpeed = Integer.valueOf(mAutoSpeedBoost.getText().toString());
        return data;
    }

}
