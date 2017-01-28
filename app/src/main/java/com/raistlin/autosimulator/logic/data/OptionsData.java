package com.raistlin.autosimulator.logic.data;

import java.io.Serializable;

public class OptionsData implements Serializable {

    private static final long serialVersionUID = 4330030078955785872L;

    public int LinesCount = 5;
    public int GeneratorFrequency = 10;
    public int AutoMinSpeed = 50;
    public int AutoMaxSpeed = 100;
    public int AutoForceStopSpeed = 20;
    public int AutoForceStopLength = 100;
    public int AutoStopSpeed = 3;
    public int AutoBoostSpeed = 5;
}
