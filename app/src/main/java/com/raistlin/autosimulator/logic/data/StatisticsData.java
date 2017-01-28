package com.raistlin.autosimulator.logic.data;

import java.io.Serializable;

public class StatisticsData implements Serializable {

    private static final long serialVersionUID = -9055555436643993528L;

    /**
     * Количество созданных машин
     */
    private int mAutosCreated;
    /**
     * Количество машин, проехавших по автостраде
     */
    private int mAutosDone;
    /**
     * Количество аварий
     */
    private int mCrashes;
    /**
     * Количество принудительных торможений
     */
    private int mForceStops;

    public StatisticsData() {
        clear();
    }

    public void clear() {
        mAutosCreated = 0;
        mAutosDone = 0;
        mCrashes = 0;
        mForceStops = 0;
    }

    public void incAutosCreated() {
        mAutosCreated++;
    }

    public int getAutosCreated() {
        return mAutosCreated;
    }

    public void incAutosDone() {
        mAutosDone++;
    }

    public int getAutosDone() {
        return mAutosDone;
    }

    public void incCrashes() {
        mCrashes++;
    }

    public int getCrashes() {
        return mCrashes;
    }

    public void incForceStops() {
        mForceStops++;
    }

    public int getForceStops() {
        return mForceStops;
    }

}
