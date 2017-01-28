package com.raistlin.autosimulator.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.raistlin.autosimulator.logic.data.AutoData;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class Highway {

    /**
     * Список полос автострады
     */
    private ArrayList<HighwayLine> mLines;

    /**
     * Ширина автострады (горизонталь)
     */
    private int mWidth = 0;
    /**
     * Высота автострады (вертикаль)
     */
    private int mHeight = 0;

    /**
     * Флаг направления
     */
    private boolean mVertical = true;

    /**
     * Ссылка на контекст приложения
     */
    protected final Context mContext;

    public Highway(Context context) {
        mContext = context;
    }

    /**
     * Отрисовать автостраду
     */
    public void render(Canvas c) {
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        c.drawRect(0, 0, mWidth, mHeight, p);
        for (HighwayLine line : mLines) {
            line.render(c);
        }
    }

    /**
     * Инициализировать автостраду, создать {@code linesCount} полос
     */
    public void init(int linesCount) {
        mLines = new ArrayList<>(linesCount);
        for (int i = 0; i < linesCount; ++i) {
            HighwayLine line = new HighwayLine(mContext);
            line.init(this);
            mLines.add(line);
        }
        if (mWidth != 0 && mHeight != 0) {
            setSize(mWidth, mHeight);
        }
    }

    /**
     * Добавить машину в полосу с номером {@code lineNumber}
     */
    public void addAuto(int lineNumber, AutoData data) {
        if (lineNumber < 0 || lineNumber >= mLines.size())
            throw new IllegalArgumentException("Wrong line number!");
        HighwayLine line = mLines.get(lineNumber);
        line.addAuto(data);
        mContext.sendBroadcast(BroadcastUtils.buildAutoCreatedIntent());
    }

    /**
     * Установить размер автострады
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public void setSize(int width, int height) {
        boolean vertical = width < height;
        if (mVertical != vertical) {
            mVertical = vertical;
            // если изменили размер при повороте, а не при первом запуске
            if (mWidth != 0 && mHeight != 0) {
                float scale = mVertical ? ((float) height / mWidth) : ((float) width / mHeight);
                scaleBy(scale);
            }
        }

        mWidth = width;
        mHeight = height;

        CoreConst.AUTO_BODY_LENGTH = (mVertical ? height : width) / CoreConst.LINE_LENGTH_IN_AUTOS;
        int lineWidth = mVertical ? width / mLines.size() : height / mLines.size();
        int x;
        x = mVertical ? 0 : (height - lineWidth);
        for (HighwayLine line : mLines) {
            if (mVertical) {
                line.setSize(lineWidth, height);
            } else {
                line.setSize(width, lineWidth);
            }
            line.setPosition(x);
            x = mVertical ? (lineWidth + x) : (x - lineWidth);
        }
    }

    /**
     * Пересчитать координаты
     *
     * @param scale - коэффициент изменения
     */
    private void scaleBy(float scale) {
        for (HighwayLine line : mLines) {
            line.scaleBy(scale);
        }
    }

    /**
     * Обновить эксперимент до следующей итерации
     */
    public void update() {
        for (HighwayLine line : mLines) {
            line.beginUpdate();
        }
        // чтобы корректно обработать машины, перестроившиеся в другие ряды
        for (HighwayLine line : mLines) {
            line.endUpdate();
        }
    }

    /**
     * Какая ориентация экрана
     */
    public boolean isVertical() {
        return mVertical;
    }

    /**
     * Проверить, свободна ли полоса, которая находится левее, чем {@code highwayLine}
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isLeftLineIntervalEmpty(HighwayLine highwayLine, float begin, float end) {
        int index = mLines.indexOf(highwayLine);
        if (index == mLines.size() - 1) {
            return false;
        } else {
            return mLines.get(index + 1).isIntervalEmpty(begin, end);
        }
    }

    /**
     * Перестроить машину в полосу, которая левее чем {@code highwayLine}
     */
    public void changeAutoLeft(HighwayLine highwayLine, Auto auto) {
        int index = mLines.indexOf(highwayLine);
        if (index == mLines.size() - 1) {
            throw new IllegalArgumentException("Cannot move auto to the left!");
        }
        mLines.get(index + 1).acceptAuto(auto);
    }

    /**
     * Проверить, свободна ли полоса, которая находится правее, чем {@code highwayLine}
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean isRightLineIntervalEmpty(HighwayLine highwayLine, float begin, float end) {
        int index = mLines.indexOf(highwayLine);
        if (index == 0) {
            return false;
        } else {
            return mLines.get(index - 1).isIntervalEmpty(begin, end);
        }
    }

    /**
     * Перестроить машину в полосу, которая правее чем {@code highwayLine}
     */
    public void changeAutoRight(HighwayLine highwayLine, Auto auto) {
        int index = mLines.indexOf(highwayLine);
        if (index == 0) {
            throw new IllegalArgumentException("Cannot move auto to the right!");
        }
        mLines.get(index - 1).acceptAuto(auto);
    }

    /**
     * Можно ли добавить в полосу новую машину
     */
    public boolean isLineAddAvailable(int lineIndex) {
        return mLines.get(lineIndex).isIntervalEmpty(-CoreConst.AUTO_BODY_LENGTH,
                CoreConst.AUTO_BODY_LENGTH * CoreConst.AUTO_ADD_VISIBILITY_SIZE);
    }

    /**
     * Очистить автостраду
     */
    public void clear() {
        mLines.clear();
    }

    /**
     * Обработать нажатие
     */
    public void onTouchEvent(float x, float y) {
        float lineWidth = ((float) (isVertical() ? mWidth : mHeight)) / mLines.size();
        int index = isVertical() ? ((int) (x / lineWidth)) : (mLines.size() - 1 - (int) (y / lineWidth));
        if (index >= 0 && index <= mLines.size() - 1) {
            mLines.get(index).onTouchEvent(isVertical() ? y : x);
        }
    }

    /**
     * Забронировать позицию для перестроения автомобиля
     */
    public void lockLeftLinePosition(HighwayLine highwayLine, Auto auto, float position) {
        int index = mLines.indexOf(highwayLine);
        if (index == mLines.size() - 1) {
            throw new IllegalArgumentException("Cannot lock auto position to the left!");
        }
        mLines.get(index + 1).lockPosition(auto, position);
    }

    /**
     * Забронировать позицию для перестроения автомобиля
     */
    public void lockRightLinePosition(HighwayLine highwayLine, Auto auto, float position) {
        int index = mLines.indexOf(highwayLine);
        if (index == 0) {
            throw new IllegalArgumentException("Cannot lock auto position to the right!");
        }
        mLines.get(index - 1).lockPosition(auto, position);
    }
}
