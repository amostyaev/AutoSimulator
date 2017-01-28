package com.raistlin.autosimulator.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.raistlin.autosimulator.R;
import com.raistlin.autosimulator.logic.data.StatisticsData;

import java.util.Locale;

public class StatisticsDialog extends DialogFragment {

    private static final String KEY_STATISTICS_DATA = "statistics_data";

    public static StatisticsDialog newInstance(StatisticsData data) {
        StatisticsDialog dialog = new StatisticsDialog();
        Bundle args = new Bundle();
        args.putSerializable(KEY_STATISTICS_DATA, data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppDialog);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_statistics, container, false);
        getDialog().setTitle(R.string.statistics_dialog_title);

        StatisticsData data = (StatisticsData) getArguments().getSerializable(KEY_STATISTICS_DATA);
        TextView autosCreated = (TextView) view.findViewById(R.id.statistics_autos_created);
        autosCreated.setText(String.valueOf(data.getAutosCreated()));
        TextView autosDone = (TextView) view.findViewById(R.id.statistics_autos_done);
        autosDone.setText(String.valueOf(data.getAutosDone()));
        TextView crashes = (TextView) view.findViewById(R.id.statistics_crashes);
        crashes.setText(String.valueOf(data.getCrashes()));
        TextView forceStops = (TextView) view.findViewById(R.id.statistics_force_stops);
        forceStops.setText(String.valueOf(data.getForceStops()));
        double crashp = (double) data.getCrashes() / data.getAutosCreated() * 100.0;
        TextView crashPercent = (TextView) view.findViewById(R.id.statistics_crash_percent);
        crashPercent.setText(String.format(Locale.getDefault(), "%.2f%%", crashp));

        Button ok = (Button) view.findViewById(R.id.statistics_button_ok);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}
