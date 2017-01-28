package com.raistlin.autosimulator.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.raistlin.autosimulator.R;
import com.raistlin.autosimulator.logic.HighwayController;
import com.raistlin.autosimulator.logic.data.OptionsData;
import com.raistlin.autosimulator.ui.OptionsDialog.OptionsDialogResult;

public class HighwayActivity extends AppCompatActivity {

    private static final String DIALOG_STATISTICS = "dialog_statistics";
    private static final String DIALOG_OPTIONS = "dialog_options";

    private HighwayController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highway);
        AutoImagesFactory.initInstance(getAssets());

        mController = (HighwayController) getLastCustomNonConfigurationInstance();
        if (mController == null) {
            mController = new HighwayController(getApplicationContext());
        }

        Button slower = (Button) findViewById(R.id.button_slower);
        slower.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mController.slower();
            }
        });

        Button faster = (Button) findViewById(R.id.button_faster);
        faster.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mController.faster();
            }
        });

        final HighwayPanel highwayPanel = (HighwayPanel) findViewById(R.id.panel_highway);
        mController.setGuiPanel(highwayPanel);
        mController.setOptions(new OptionsData());
        mController.start();
    }

    protected void showOptionsDialog() {
        OptionsDialog dialog = OptionsDialog.newInstance(mController.getOptions());
        dialog.setOnDialogResult(new OptionsDialogResult() {

            @Override
            public void onResult(OptionsData data) {
                mController.setOptions(data);
            }
        });
        dialog.show(getSupportFragmentManager(), DIALOG_OPTIONS);
    }


    private void showStatisticsDialog() {
        StatisticsDialog dialog = StatisticsDialog.newInstance(mController.getStatistics());
        dialog.show(getSupportFragmentManager(), DIALOG_STATISTICS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_options:
                showOptionsDialog();
                break;
            case R.id.menu_exit:
                finish();
                break;
            case R.id.menu_restart:
                mController.stop();
                mController.start();
                break;
            case R.id.menu_statistics:
                showStatisticsDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.highway, menu);
        return true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mController;
    }

    @Override
    protected void onPause() {
        mController.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.resume();
    }

}
