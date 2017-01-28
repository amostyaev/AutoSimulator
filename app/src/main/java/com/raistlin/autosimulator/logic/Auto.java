package com.raistlin.autosimulator.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.raistlin.autosimulator.logic.data.AutoData;
import com.raistlin.autosimulator.ui.AutoImagesFactory;

@SuppressWarnings("WeakerAccess")
public class Auto {

    /**
     * Последовательность шагов при перестроении
     */
    protected enum ChangeState {
        /**
         * Начинаем перестраиваться из полосы (сдвигаемся к краю)
         */
        From_Line,
        /**
         * Перестроились на другую полосу (находимся с краю новой полосы)
         */
        To_Line
    }

    /**
     * Текущее состояние автомобиля
     */
    protected AutoState mState = AutoState.Normal;
    /**
     * Фигурка машины
     */
    protected AutoSkin mSkin = AutoSkin.Green;
    /**
     * Положение автомобиля на автостраде
     */
    private float mPosition;
    /**
     * Расстояние до ближайшей машины при торможении
     */
    private float mDistanceBefore = Float.MAX_VALUE;
    /**
     * Текущая скорость автомобиля
     */
    private int mSpeed;
    /**
     * Скорость, которую автомобиль пытается сохранять
     */
    private int mDesiredSpeed;
    /**
     * Находимся ли в принудительном торможении
     */
    private boolean mForceStopping;
    /**
     * Состояние при перестроении
     */
    protected ChangeState mChangeState;
    /**
     * Счетчик перестроения
     */
    private int mChangingCounter;
    /**
     * Счетчик аварии
     */
    private int mCrashingCounter;
    /**
     * Счетчик принудительного торможения
     */
    private int mForceStopCounter;

    /**
     * Ссылка на полосу автострады, в котором находится машина
     */
    protected HighwayLine mLine;
    /**
     * Ссылка на контекст приложения
     */
    protected final Context mContext;

    public Auto(Context context) {
        mContext = context;
    }

    /**
     * Инициализировать автомобиль
     */
    public void init(HighwayLine line, AutoData data) {
        updateLine(line);
        mSpeed = data.Speed;
        mDesiredSpeed = data.DesiredSpeed;
        mSkin = data.Skin;
        mPosition = -CoreConst.AUTO_BODY_LENGTH;
    }

    /**
     * Получить ширину полосы
     */
    public int getLineWidth() {
        return mLine.getWidth();
    }

    /**
     * Получить высоту полосы
     */
    public int getLineHeight() {
        return mLine.getHeight();
    }

    /**
     * Установить позицию полосы движения
     */
    public int getLinePosition() {
        return mLine.getPosition();
    }

    /**
     * Отрисовать автомобиль
     */
    public void render(Canvas c) {
        Bitmap auto = getBitmap();
        Rect r = getRect(auto);
        c.drawBitmap(auto, null, r, null);
        drawVisibility(c);
    }

    /**
     * Нарисовать границы зоны обзора автомобиля
     */
    protected void drawVisibility(Canvas c) {
        Paint pRed = new Paint();
        pRed.setColor(Color.RED);
        Paint pBlack = new Paint();
        pBlack.setColor(Color.BLACK);
        if (CoreDefines.AUTO_DRAW_VISIBILITY) {
            if (isVertical()) {
                c.drawLine(getLinePosition(), mPosition + getStopVisibilitySize(),
                        getLinePosition() + getLineWidth(), mPosition + getStopVisibilitySize(), pRed);
                c.drawLine(getLinePosition(), mPosition + getBoostVisibilitySize(),
                        getLinePosition() + getLineWidth(), mPosition + getBoostVisibilitySize(), pBlack);
                c.drawLine(getLinePosition(), mPosition, getLinePosition() + getLineWidth(), mPosition, pBlack);
            } else {
                c.drawLine(mPosition + getStopVisibilitySize(), getLinePosition(),
                        mPosition + getStopVisibilitySize(), getLinePosition() + getLineHeight(), pRed);
                c.drawLine(mPosition + getBoostVisibilitySize(), getLinePosition(),
                        mPosition + getBoostVisibilitySize(), getLinePosition() + getLineHeight(), pBlack);
                c.drawLine(mPosition, getLinePosition(), mPosition, getLinePosition() + getLineHeight(), pBlack);
            }
        }
    }

    // процедуры, вычисляющие размеры областей видимости автомобиля
    // так как они разные в вертикальной и горизонтальной ориентации
    private int getStopVisibilitySize() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_STOP_VISIBILITY_SIZE;
    }

    private int getBoostVisibilitySize() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_BOOST_VISIBILITY_SIZE;
    }

    private int getLineChangeVisibilitySizeAfter() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_LINE_CHANGE_VISIBILITY_SIZE_AFTER;
    }

    private int getLineChangeVisibilitySizeBefore() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_LINE_CHANGE_VISIBILITY_SIZE_BEFORE;
    }

    private int getLineChangeStopVisibilitySize() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_LINE_CHANGE_STOP_VISIBILITY_SIZE;
    }

    private int getTouchVisibilitySize() {
        return CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_FORCED_STOP_VISIBILITY_SIZE;
    }

    /**
     * Получить изображение текущего состояния машины
     */
    private Bitmap getBitmap() {
        return isVertical() ? AutoImagesFactory.getInstance().getVerticalImage(mState, mSkin)
                : AutoImagesFactory.getInstance().getHorizontalImage(mState, mSkin);
    }

    /**
     * Получить прямоугольник, где должна отрисоваться машина
     */
    private Rect getRect(Bitmap auto) {
        Rect r;
        int autoWidth;
        int autoHeight;
        if (CoreDefines.AUTO_DRAW_CURRENT_SIZE) {
            autoWidth = isVertical() ? (int) ((float) auto.getWidth() / auto.getHeight()
                    * CoreConst.AUTO_BODY_LENGTH) : CoreConst.AUTO_BODY_LENGTH;
            autoHeight = isVertical() ? CoreConst.AUTO_BODY_LENGTH :
                    (int) ((float) auto.getHeight() / auto.getWidth() * CoreConst.AUTO_BODY_LENGTH);
        } else {
            autoWidth = auto.getWidth();
            autoHeight = auto.getHeight();
        }
        if (isVertical()) {
            int x;
            if (mState == AutoState.Changing_Left) {
                if (mChangeState == ChangeState.From_Line) {
                    x = getLinePosition() + (getLineWidth() - autoWidth) / 2 +
                            (getLineWidth() - autoWidth) * (CoreConst.AUTO_LINE_CHANGE_SPEED - mChangingCounter) /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                } else {
                    x = getLinePosition() +
                            (getLineWidth() - autoWidth) * (CoreConst.AUTO_LINE_CHANGE_SPEED - mChangingCounter) /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                }
            } else if (mState == AutoState.Changing_Right) {
                if (mChangeState == ChangeState.From_Line) {
                    x = getLinePosition() +
                            (getLineWidth() - autoWidth) * mChangingCounter /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                } else {
                    x = getLinePosition() + (getLineWidth() - autoWidth) / 2 +
                            (getLineWidth() - autoWidth) * mChangingCounter /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                }
            } else {
                x = getLinePosition() + (getLineWidth() - autoWidth) / 2;
            }
            r = new Rect(x, (int) mPosition, x + autoWidth, (int) mPosition + autoHeight);
        } else {
            int y;
            if (mState == AutoState.Changing_Left) {
                if (mChangeState == ChangeState.From_Line) {
                    y = getLinePosition() +
                            (getLineHeight() - autoHeight) * mChangingCounter /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                } else {
                    y = getLinePosition() + (getLineHeight() - autoHeight) / 2 +
                            (getLineHeight() - autoHeight) * mChangingCounter /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                }
            } else if (mState == AutoState.Changing_Right) {
                if (mChangeState == ChangeState.From_Line) {
                    y = getLinePosition() + (getLineHeight() - autoHeight) / 2 +
                            (getLineHeight() - autoHeight) * (CoreConst.AUTO_LINE_CHANGE_SPEED - mChangingCounter) /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                } else {
                    y = getLinePosition() +
                            (getLineHeight() - autoHeight) * (CoreConst.AUTO_LINE_CHANGE_SPEED - mChangingCounter) /
                                    CoreConst.AUTO_LINE_CHANGE_SPEED / 2;
                }
            } else {
                y = getLinePosition() + (getLineHeight() - autoHeight) / 2;
            }
            r = new Rect((int) mPosition, y, (int) mPosition + autoWidth, y + autoHeight);
        }
        return r;
    }

    /**
     * Передвинуть авто на одну итерацию
     */
    public void update() {
        // если в аварии
        if (isCrashed()) {
            doCrashedStub();
            return;
        }
        // если перестраиваемся
        if (isChanging()) {
            mChangingCounter--;
            checkStopWhenChange();
            if (mChangingCounter <= 0 && mChangeState == ChangeState.From_Line) {
                beginChangeLine();
                mChangingCounter = CoreConst.AUTO_LINE_CHANGE_SPEED;
            } else if (mChangingCounter <= 0 && mChangeState == ChangeState.To_Line) {
                mLine.unlockPosition(this);
                endChangeLine();
            }
            // если нужно тормозить
        } else if (needStop()) {
            // если не хотим останавливаться и есть возможность перестроиться
            if (!wantStop() && canChangeLineLeft()) {
                setState(AutoState.Changing_Left);
                lockLeftLine();
                mChangeState = ChangeState.From_Line;
                mChangingCounter = CoreConst.AUTO_LINE_CHANGE_SPEED;
                // ... смотрим другой ряд
            } else if (!wantStop() && canChangeLineRight()) {
                setState(AutoState.Changing_Right);
                lockRightLine();
                mChangeState = ChangeState.From_Line;
                mChangingCounter = CoreConst.AUTO_LINE_CHANGE_SPEED;
                // иначе придется тормозить
            } else {
                setState(AutoState.Stopping);
                doStop();
            }
            // если хотим разгоняться
        } else if (wantBoost()) {
            // если не можем разогнаться в текущей полосе
            if (!canBoost()) {
                // пробуем перестроиться влево
                if (canChangeLineLeft()) {
                    setState(AutoState.Changing_Left);
                    lockLeftLine();
                    mChangeState = ChangeState.From_Line;
                    mChangingCounter = CoreConst.AUTO_LINE_CHANGE_SPEED;
                    // ... пробуем вправо
                } else if (canChangeLineRight()) {
                    setState(AutoState.Changing_Right);
                    lockRightLine();
                    mChangeState = ChangeState.From_Line;
                    mChangingCounter = CoreConst.AUTO_LINE_CHANGE_SPEED;
                    // ... смирно едем прямо
                } else {
                    setState(AutoState.Normal);
                }
            } else {
                setState(AutoState.Boosting);
                mSpeed = Math.min(mDesiredSpeed, mSpeed + CoreConst.AUTO_BOOST_SPEED);
            }
            // иначе не рыпаемся
        } else if (canDropState()) {
            setState(AutoState.Normal);
        }
        // если мы приторможены
        if (mForceStopping) {
            doForceStopping();
        }
        move();
    }

    private void lockLeftLine() {
        mLine.lockLeftLinePosition(this, mPosition);
    }

    private void lockRightLine() {
        mLine.lockRightLinePosition(this, mPosition);
    }

    /**
     * Проверить, не нужно ли сбросить скорость
     */
    private void checkStopWhenChange() {
        if (mLine.getIntervalAutoCount(mPosition, mPosition + getLineChangeStopVisibilitySize()) > 1) {
            doStop();
        }
    }

    /**
     * Можем ли мы установить обычное состояние
     */
    private boolean canDropState() {
        return mState != AutoState.Force_Stopping;
    }

    private void setState(AutoState state) {
        if (mState != state) {
            // do before change
            switch (mState) {
                case Stopping:
                    if (state != AutoState.Force_Stopping) {
                        endFollowing();
                    }
                    break;
                case Force_Stopping:
                    if (state != AutoState.Stopping) {
                        mForceStopping = false;
                    }
                    break;
                case Boosting:
                case Changing_Left:
                case Changing_Right:
                case Crashed:
                case Normal:
                    break;
            }
            mState = state;
            // do after change
            switch (mState) {
                case Boosting:
                case Changing_Left:
                case Changing_Right:
                case Crashed:
                case Normal:
                case Stopping:
                    break;

                case Force_Stopping:
                    mForceStopping = true;
                    break;
            }
        }
    }

    private void endFollowing() {
        mDistanceBefore = Float.MAX_VALUE;
    }

    /**
     * Процесс торможения
     */
    private void doStop() {
        if (mForceStopping && mSpeed > CoreConst.AUTO_FORCED_STOP_MIN_SPEED) {
            mSpeed = Math.max(getMinPossibleSpeed(), mSpeed - CoreConst.AUTO_STOP_SPEED);
            return;
        }

        float before = mDistanceBefore;
        mDistanceBefore = mLine.getFreeDistance(this, mPosition, mPosition + getStopVisibilitySize());
        if (mDistanceBefore < before) {
            mSpeed = Math.max(getMinPossibleSpeed(), mSpeed - CoreConst.AUTO_STOP_SPEED);
        }
    }

    private int getMinPossibleSpeed() {
        if (isChanging()) {
            return CoreConst.AUTO_MIN_CHANGE_SPEED;
        } else {
            return 0;
        }
    }

    private void doForceStopping() {
        --mForceStopCounter;
        if (mForceStopCounter <= 0) {
            setState(AutoState.Normal);
        } else {
            setState(AutoState.Force_Stopping);
        }
    }

    /**
     * Действия при аварии - ждем N итераций
     */
    private void doCrashedStub() {
        mCrashingCounter--;
        if (mCrashingCounter == 0) {
            mLine.removeAuto(this);
        }
    }

    /**
     * Перестроиться в другую полосу
     */
    private void beginChangeLine() {
        if (mState == AutoState.Changing_Left) {
            mLine.changeAutoLeft(this);
            mChangeState = ChangeState.To_Line;
        } else {
            mLine.changeAutoRight(this);
            mChangeState = ChangeState.To_Line;
        }
    }

    /**
     * Закончить перестроение
     */
    private void endChangeLine() {
        setState(AutoState.Normal);
    }

    /**
     * Проверить, перестраиваемся ли машина сейчас
     */
    private boolean isChanging() {
        return mState == AutoState.Changing_Left || mState == AutoState.Changing_Right;
    }

    /**
     * Можем ли перестроиться в левый ряд
     */
    private boolean canChangeLineLeft() {
        return mSpeed >= CoreConst.AUTO_MIN_CHANGE_SPEED &&
                mLine.isLeftLineIntervalEmpty(mPosition - getLineChangeVisibilitySizeBefore(),
                        mPosition + getLineChangeVisibilitySizeAfter());
    }

    /**
     * Можем ли перестроиться в правый ряд
     */
    private boolean canChangeLineRight() {
        return mSpeed >= CoreConst.AUTO_MIN_CHANGE_SPEED &&
                mLine.isRightLineIntervalEmpty(mPosition - getLineChangeVisibilitySizeBefore(),
                        mPosition + getLineChangeVisibilitySizeAfter());
    }

    /**
     * Проверить, можно ли разгоняться в текущей полосе
     */
    private boolean canBoost() {
        return (mSpeed < mDesiredSpeed) &&
                mLine.getIntervalAutoCount(mPosition, mPosition + getBoostVisibilitySize()) == 1;
    }

    /**
     * Xотим ли мы разгоняться?
     */
    private boolean wantBoost() {
        return !mForceStopping && (mSpeed < mDesiredSpeed);
    }

    /**
     * Проверить, нужно ли останавливаться
     */
    private boolean needStop() {
        return (mForceStopping && mSpeed > CoreConst.AUTO_FORCED_STOP_MIN_SPEED) ||
                (mSpeed > 0) && ((mSpeed > mDesiredSpeed) ||
                        mLine.getIntervalAutoCount(mPosition, mPosition + getStopVisibilitySize()) > 1);
    }

    /**
     * А не хотим ли мы остановиться?
     */
    private boolean wantStop() {
        return mForceStopping || (mSpeed > mDesiredSpeed);
    }

    /**
     * Находится ли машина в аварии
     */
    private boolean isCrashed() {
        return mState == AutoState.Crashed;
    }

    /**
     * Сказать полосе проверить следующий интервал на наличие в нем
     * больше чем одной машины (себя) и сказать этим машинам,
     * что они попали в аварию
     */
    public void checkCollisionInterval() {
        if (mState != AutoState.Crashed) {
            mLine.checkCollisionsForAuto(this, mPosition, mPosition + CoreConst.AUTO_BODY_LENGTH);
        }
    }

    /**
     * Сказать машине, что она попала в аварию
     */
    public void setCrashed() {
        setState(AutoState.Crashed);
        mCrashingCounter = CoreConst.AUTO_CRASHED_STATE_LONG;
    }

    /**
     * Передвинуть машину в соответствии с текущей скоростью
     */
    private void move() {
        mPosition = (mPosition + getIterOffset());
        boolean endLineReached;
        endLineReached = isVertical() ? (mPosition > getLineHeight() && getLineHeight() > 0)
                : (mPosition > getLineWidth() && getLineWidth() > 0);
        if (endLineReached) {
            mLine.removeAuto(this);
            mContext.sendBroadcast(BroadcastUtils.buildAutoDoneIntent());
        }
    }

    /**
     * Получить смещение для одной итерации в зависимости от скорости автомобиля
     */
    protected float getIterOffset() {
        return mSpeed / 10;
    }

    /**
     * Находится ли машина в заданном интервале
     */
    public boolean isInInterval(float begin, float end) {
        return (mPosition >= begin) && (mPosition <= end);
    }

    /**
     * Какая ориентация экрана сейчас на устройстве
     */
    public boolean isVertical() {
        return mLine.isVertical();
    }

    /**
     * Обновить ссылку на родитель - полосу движения
     */
    public void updateLine(HighwayLine highwayLine) {
        mLine = highwayLine;
    }

    /**
     * Пересчитать координату
     */
    public void scaleBy(float scale) {
        // округлить до целой части
        mPosition = (int) (mPosition * scale);
    }

    /**
     * Проверить нажатие - нажати на нас или нет
     */
    public boolean checkTouch(float coord) {
        if (mPosition <= coord && coord <= mPosition + getTouchVisibilitySize()) {
            setState(AutoState.Force_Stopping);
            mForceStopCounter = CoreConst.AUTO_FORCED_STOP_COUNT;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Получить текущее положение
     */
    public float getPosition() {
        return mPosition;
    }

}
