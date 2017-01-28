package com.raistlin.autosimulator.logic;

public class CoreConst {

    /**
     * Длина корпуса автомобиля.
     * Меняется при смене ориентации экрана
     */
    public static volatile int AUTO_BODY_LENGTH;

    /**
     * До какой скорости автомобиль снижает скорость при принудительном торможении
     * Устанавливается контроллером и может меняться
     */
    public static volatile int AUTO_FORCED_STOP_MIN_SPEED;

    /**
     * Количество итераций в принудительном торможении
     * Устанавливается контроллером и может меняться
     */
    public static volatile int AUTO_FORCED_STOP_COUNT;

    /**
     * C каким ускорением автомобиль тормозит.
     * За одну итерацию автомобиль сбрасывает скорость на эту величину
     * Устанавливается в настройках
     */
    public static volatile int AUTO_STOP_SPEED;

    /**
     * C каким ускорением автомобиль разгоняется.
     * За одну итерацию автомобиль увеличивает скорость на эту величину
     * Устанавливается в настройках
     */
    public static volatile int AUTO_BOOST_SPEED;

    /**
     * Расстояние, которое проверяется при клике на автомобиль. В корпусах {@code mAutoSize}
     * Приходится делать больше, чем корпус, потому что иначе очень турдно попасть пальцем по машине
     */
    public static volatile int AUTO_FORCED_STOP_VISIBILITY_SIZE = 2;

    /**
     * Длины полосы в машинах.
     * Сколько автомобилей может поместиться на полосу подряд
     */
    public static final int LINE_LENGTH_IN_AUTOS = 18;

    /**
     * Расстояние, которое проверяется при добавлении автомобиля. В корпусах {@code mAutoSize}
     * Если в пределах этого расстояния есть автомобиль, то нельзя добавлять машину
     */
    public static final int AUTO_ADD_VISIBILITY_SIZE = 2;

    /**
     * Обзор автомобиля при торможении. В корпусах {@code mAutoSize}
     * Когда в пределах этого расстояния есть другой автомобиль, то тормозим
     */
    public static final int AUTO_STOP_VISIBILITY_SIZE = 3;

    /**
     * Обзор автомобиля при разгоне. В корпусах {@code mAutoSize}
     * Когда в пределах этого расстояния нет других автомобилей, то ускоряемся
     */
    public static final int AUTO_BOOST_VISIBILITY_SIZE = 4; // 5

    /**
     * Обзор автомобиля при перестроении. В корпусах {@code mAutoSize}
     * Когда в пределах этого расстояния нет других автомобилей, то перестраиваемся
     */
    public static final int AUTO_LINE_CHANGE_VISIBILITY_SIZE_BEFORE = 3;

    /**
     * Обзор автомобиля при перестроении. В корпусах {@code mAutoSize}
     * Когда в пределах этого расстояния нет других автомобилей, то перестраиваемся
     */
    public static final int AUTO_LINE_CHANGE_VISIBILITY_SIZE_AFTER = 4;

    /**
     * Обзор для торможения автомобиля при перестроении. В корпусах {@code mAutoSize}
     * Если в пределах этого расстояния есть автомобиль, то снижаем скорость, но продолжаем перестроение
     */
    public static final int AUTO_LINE_CHANGE_STOP_VISIBILITY_SIZE = 2;

    /**
     * C какой минимальной скоростью автомобиль можнт перестраиваться
     */
    public static final int AUTO_MIN_CHANGE_SPEED = 40;

    /**
     * За сколько итераций автомобиль покидает полосу и заезжает на нее
     */
    public static final int AUTO_LINE_CHANGE_SPEED = 10;

    /**
     * Cколько итераций автомобиль стоит в аварии
     */
    public static final int AUTO_CRASHED_STATE_LONG = 400;

    /**
     * Единица изменения скорости проведения эксперимента
     */
    public static final int CORE_SPEED_DELTA = 20;

    /**
     * Минимальная скорость проведения эксперимента
     */
    public static final int CORE_SPEED_MIN = 10;

    /**
     * Максимальная скорость проведения эксперимента
     */
    public static final int CORE_SPEED_MAX = 10000;

    // Messages
    public final static String BROADCAST_INTENT = "cast_highway_event";
    public final static String BROADCAST_TYPE = "cast_highway_type";
    // Event types
    public final static int BROADCAST_NONE = 0x00;
    public final static int BROADCAST_AUTO_CREATED = 0x01;
    public final static int BROADCAST_AUTO_DONE = 0x02;
    public final static int BROADCAST_CRASH = 0x03;
    public final static int BROADCAST_FORCE_STOPS = 0x04;

}
