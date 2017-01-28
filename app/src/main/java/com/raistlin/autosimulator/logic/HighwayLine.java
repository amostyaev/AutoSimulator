package com.raistlin.autosimulator.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.raistlin.autosimulator.logic.data.AutoData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("WeakerAccess")
public class HighwayLine {

    /**
     * Список машин на этой полосе
     */
    private final ArrayList<Auto> mAutos = new ArrayList<>();
    /**
     * Очередь из машин, которые перестроились в другой ряд и подлежат удалению из текущего
     */
    private ConcurrentLinkedQueue<Auto> mRemoveRequest = new ConcurrentLinkedQueue<>();
    /**
     * Очередь из машин, которые перестроились в другой ряд и подлежат удалению из текущего
     */
    private ConcurrentLinkedQueue<Auto> mAddRequest = new ConcurrentLinkedQueue<>();
    private HashMap<Auto, Float> mLockedPositions = new HashMap<>();

    /**
     * Ширина полосы (горизонталь)
     */
    private int mWidth;
    /**
     * Высота полосы (вертикаль)
     */
    private int mHeight;
    /**
     * Положение полосы
     */
    private int mPosition;

    /**
     * Ссылка на автостраду
     */
    protected Highway mHighway;
    /**
     * Ссылка на контекст приложения
     */
    protected final Context mContext;

    public HighwayLine(Context context) {
        mContext = context;
    }

    /**
     * Инициализация полосы
     */
    public void init(Highway highway) {
        mHighway = highway;
    }

    /**
     * Добавить автомобиль в начало полосы
     */
    public void addAuto(AutoData data) {
        Auto auto = new Auto(mContext);
        auto.init(this, data);
        mAutos.add(auto);
    }

    /**
     * Установить размер полосы в пикселях
     */
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Установить смещение начала полосы
     */
    public void setPosition(int position) {
        mPosition = position;
    }

    /**
     * Отрисовать полосу автострады
     */
    public void render(Canvas c) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        if (isVertical()) {
            c.drawRect(mPosition, 0, mPosition + mWidth, mHeight, p);
        } else {
            c.drawRect(0, mPosition, mWidth, mPosition + mHeight, p);
        }
        for (Auto auto : mAutos) {
            auto.render(c);
        }
    }

    /**
     * Обновить полосу движения на одну итерацию
     */
    public void beginUpdate() {
        for (Auto auto : mAutos) {
            auto.update();
        }
    }

    /**
     * Закончить обновление полосы - завершить перестроения машин
     */
    public void endUpdate() {
        while (!mRemoveRequest.isEmpty()) {
            mAutos.remove(mRemoveRequest.poll());
        }
        while (!mAddRequest.isEmpty()) {
            Auto auto = mAddRequest.poll();
            auto.updateLine(this);
            mAutos.add(auto);
        }
        checkCollisions();
    }

    /**
     * Возвращает количество машин в интервале <br>
     * Полезно для машин, чтобы проверить нужно ли тормозить или можно ускоряться <br>
     * Также вызывается у соседней полосы для проверки перестроения
     *
     * @param begin - начало интервала
     * @param end   - конец интервала
     * @return - количество машин в интервале
     */
    public int getIntervalAutoCount(float begin, float end) {
        int result = 0;
        for (Auto auto : mAutos) {
            if (auto.isInInterval(begin, end)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Проверяет, нет ли в интервале машин, а также то, что этот интервал никем не заблокирован
     */
    public boolean isIntervalEmpty(float begin, float end) {
        return (getIntervalAutoCount(begin, end) == 0) &&
                !checkIntervalLocked(begin, end);
    }

    private boolean checkIntervalLocked(float begin, float end) {
        boolean result = false;
        for (float position : mLockedPositions.values()) {
            if (position >= begin && position <= end) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Возвращает расстояние до ближайшей машины в заданном интервале
     */
    public float getFreeDistance(Auto auto, float begin, float end) {
        float result = Float.MAX_VALUE;
        for (Auto auto2 : mAutos) {
            if (!auto2.equals(auto) && auto2.isInInterval(begin, end)) {
                if (auto2.getPosition() < result) {
                    result = auto2.getPosition();
                }
            }
        }
        result -= auto.getPosition();
        return result;
    }

    /**
     * <p>Проверяет, не столкнулись ли какие-нибудь машины на этой полосе</p>
     * Если столкновение произошло, то об этом сообщается машинам
     * и посылается широковещательное сообщение
     */
    protected void checkCollisions() {
        for (Auto auto : mAutos) {
            auto.checkCollisionInterval();
        }
    }

    /**
     * Проверяет, свободна ли левая полоса на заданном интервале
     */
    public boolean isLeftLineIntervalEmpty(float begin, float end) {
        return mHighway.isLeftLineIntervalEmpty(this, begin, end);
    }

    /**
     * Проверяет, свободна ли правая полоса на заданном интервале
     */
    public boolean isRightLineIntervalEmpty(float begin, float end) {
        return mHighway.isRightLineIntervalEmpty(this, begin, end);
    }

    /**
     * Перестроить машину в левую полосу
     */
    public void changeAutoLeft(Auto auto) {
        mRemoveRequest.add(auto);
        mHighway.changeAutoLeft(this, auto);
    }

    /**
     * Принять перестроившуюся машину
     */
    public void acceptAuto(Auto auto) {
        mAddRequest.add(auto);
    }

    /**
     * Перестроить машину в правую полосу
     */
    public void changeAutoRight(Auto auto) {
        mRemoveRequest.add(auto);
        mHighway.changeAutoRight(this, auto);
    }

    /**
     * Проверить авто на столкновение с другими
     */
    public void checkCollisionsForAuto(Auto auto, float begin, float end) {
        boolean crashed = false;
        for (Auto auto2 : mAutos) {
            if (!auto.equals(auto2) && auto2.isInInterval(begin, end)) {
                crashed = true;
                auto2.setCrashed();
            }
        }
        if (crashed) {
            auto.setCrashed();
            mContext.sendBroadcast(BroadcastUtils.buildCrashIntent());
        }
    }

    /**
     * Удалить авто после аварии или достижении конца полосы
     */
    public void removeAuto(Auto auto) {
        mRemoveRequest.add(auto);
    }

    /**
     * Какая ориентация экрана сейчас на устройстве
     */
    public boolean isVertical() {
        return mHighway.isVertical();
    }

    /**
     * Ширина полосы
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Высота полосы
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Позиция полосы
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * Пересчитать координаты
     */
    public void scaleBy(float scale) {
        for (Auto auto : mAutos) {
            auto.scaleBy(scale);
        }
    }

    /**
     * Обработать нажатие
     */
    public void onTouchEvent(float coord) {
        for (Auto auto : mAutos) {
            if (auto.checkTouch(coord)) {
                mContext.sendBroadcast(BroadcastUtils.buildAutoForceStopIntent());
                break;
            }

        }
    }

    public void unlockPosition(Auto auto) {
        mLockedPositions.remove(auto);
    }

    public void lockPosition(Auto auto, float position) {
        mLockedPositions.put(auto, position);
    }

    /**
     * Забронировать интервал на левой полосе для перестроения автомобиля
     */
    public void lockLeftLinePosition(Auto auto, float position) {
        mHighway.lockLeftLinePosition(this, auto, position);
    }

    /**
     * Забронировать интервал на правой полосе для перестроения автомобиля
     */
    public void lockRightLinePosition(Auto auto, float position) {
        mHighway.lockRightLinePosition(this, auto, position);
    }

}
