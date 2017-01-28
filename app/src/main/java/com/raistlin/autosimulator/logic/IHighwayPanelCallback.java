package com.raistlin.autosimulator.logic;

/**
 * Интерфейс, который предоставляется панели рисования
 * для оповещения о событиях изменения
 *
 * @author Артем
 */
public interface IHighwayPanelCallback {

    /**
     * Вызывается, когда изменяется размер панели отображения
     *
     * @param width  - новая ширина
     * @param height - новая высота
     */
    void onSizeChanged(int width, int height);


    /**
     * Вызывается при нажатии на панель
     *
     * @param x - ордината
     * @param y - абсцисса
     */
    void onTouchEvent(float x, float y);
}
