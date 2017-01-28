package com.raistlin.autosimulator.logic;


/**
 * Интерфейс, который реализуется классом, отображающим автостраду
 *
 * @author Артем
 */
public interface IHighwayPanel {

    /**
     * Перерисовать панель
     *
     * @param highway - автострада, которая перерисовывается
     *                на канве класса, реализующего интерфейс
     */
    void update(Highway highway);

    /**
     * Установить обратную связь для панели, куда будут отправляться
     * уведомления о системных изменениях (поворот экрана, изменение размера)
     *
     * @param callback
     */
    void setCallback(IHighwayPanelCallback callback);
}
