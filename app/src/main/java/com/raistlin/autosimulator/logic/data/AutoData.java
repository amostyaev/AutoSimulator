package com.raistlin.autosimulator.logic.data;

import com.raistlin.autosimulator.logic.AutoSkin;

import java.io.Serializable;

/**
 * Данные для инициализации объекта машины
 */
public class AutoData implements Serializable {
    private static final long serialVersionUID = 5712575159209566374L;

    /**
     * Скорость машины
     */
    public int Speed;
    /**
     * Предпочитаемая скорость
     */
    public int DesiredSpeed;
    /**
     * Форма машины - зеленая, желтая и т.п.
     */
    public AutoSkin Skin;

    public AutoData(int speed, int desiredSpeed, AutoSkin skin) {
        Speed = speed;
        DesiredSpeed = desiredSpeed;
        Skin = skin;
    }
}
