package com.raistlin.autosimulator.logic;

public enum AutoState {

    /**
     * Обычное состояние - машина беспрепятственно движется
     */
    Normal,

    /**
     * Машина притормаживает
     */
    Stopping,

    /**
     * Машина ускоряется
     */
    Boosting,

    /**
     * Машина перестраивается в левую полосу
     */
    Changing_Left,

    /**
     * Машина перестраивается в правую полосу
     */
    Changing_Right,

    /**
     * Машина в аварии
     */
    Crashed,

    /**
     * Принудительно тормозит
     */
    Force_Stopping
}
