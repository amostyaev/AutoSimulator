package com.raistlin.autosimulator.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.raistlin.autosimulator.logic.data.AutoData;
import com.raistlin.autosimulator.logic.data.OptionsData;
import com.raistlin.autosimulator.logic.data.StatisticsData;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
public class HighwayController implements IHighwayPanelCallback {

    /**
     * Контекст приложения для фишек Android
     */
    protected final Context mContext;

    protected IHighwayPanel mPanel;
    protected final Highway mHighway;

    /**
     * Запущен ли эксперимент
     */
    private boolean mStarted = false;

    /**
     * Таймер обновления эксперимента
     */
    private Timer mUpdateTimer;
    /**
     * Частота обновления эксперимента
     */
    private int mUpdateTimerFrequency = 40;

    /**
     * Обратный отсчет до добавления одного нового автомобиля
     */
    private int mAutoGeneratorCounter = 0;

    private Random mRandomGenerator = new Random();
    /**
     * Количество автомобилей, которые нужно добавить
     */
    private int mAutosToAdd = 0;

    /**
     * Разница между максимальной и минимальной скоростью
     */
    private int mSpeedDelta;
    /**
     * Опции эксперимента
     */
    private OptionsData mOptions;
    /**
     * Статистика по эксперименту
     */
    private StatisticsData mStatistics = new StatisticsData();

    /**
     * Критическая секция для обновления эксперимента
     */
    private final Object mUpdateLock = new Object();

    /**
     * Слушатель широковещательных сообщений
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        /** Обработать сообщение - столкновение, машина уехала и т.д. */
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(CoreConst.BROADCAST_TYPE, CoreConst.BROADCAST_NONE);
            switch (type) {
                case CoreConst.BROADCAST_AUTO_CREATED:
                    mStatistics.incAutosCreated();
                    break;
                case CoreConst.BROADCAST_AUTO_DONE:
                    mStatistics.incAutosDone();
                    break;
                case CoreConst.BROADCAST_CRASH:
                    mStatistics.incCrashes();
                    break;
                case CoreConst.BROADCAST_FORCE_STOPS:
                    mStatistics.incForceStops();
                    break;
            }
        }

    };

    public HighwayController(Context context) {
        mContext = context;
        mHighway = new Highway(context);
    }

    /**
     * Запустить эксперимент
     */
    public void start() {
        mAutoGeneratorCounter = 0; // будет инициализировано после добавления машин
        mStatistics.clear();
        init();
        resume();
    }

    /**
     * Останавить эксперимент
     */
    public void stop() {
        pause();
        clear();
    }

    /**
     * Приостановить эксперимент
     */
    public void pause() {
        mStarted = false;
        stopTimer();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Продолжить эксперимент
     */
    public void resume() {
        mStarted = true;
        mContext.registerReceiver(mBroadcastReceiver,
                new IntentFilter(CoreConst.BROADCAST_INTENT));
        startTimer();
    }

    /**
     * Замедлить эксперимент
     */
    public void slower() {
        mUpdateTimerFrequency = Math.min(CoreConst.CORE_SPEED_MAX,
                mUpdateTimerFrequency + CoreConst.CORE_SPEED_DELTA);
        startTimer();
    }

    /**
     * Ускорить эксперимент
     */
    public void faster() {
        mUpdateTimerFrequency = Math.max(CoreConst.CORE_SPEED_MIN,
                mUpdateTimerFrequency - CoreConst.CORE_SPEED_DELTA);
        startTimer();
    }

    /**
     * Остановить таймер
     */
    protected void stopTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    /**
     * Запустить таймер
     */
    protected void startTimer() {
        stopTimer();
        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                checkAutoGenerate();
                update();
            }

        }, 0, mUpdateTimerFrequency);
    }

    /**
     * Установить опции эксперимента
     */
    public void setOptions(OptionsData options) {
        synchronized (mUpdateLock) {
            mStatistics.clear();

            OptionsData oldOptions = mOptions;
            mOptions = options;
            mSpeedDelta = options.AutoMaxSpeed - options.AutoMinSpeed;
            mAutoGeneratorCounter = options.GeneratorFrequency;
            CoreConst.AUTO_FORCED_STOP_MIN_SPEED = options.AutoForceStopSpeed;
            CoreConst.AUTO_FORCED_STOP_COUNT = options.AutoForceStopLength;
            CoreConst.AUTO_STOP_SPEED = options.AutoStopSpeed;
            CoreConst.AUTO_BOOST_SPEED = options.AutoBoostSpeed;
            if (mStarted) {
                if (oldOptions.LinesCount != options.LinesCount) {
                    clear();
                    init();
                }
            }
        }
    }

    /**
     * Получить текущие настройки
     */
    public OptionsData getOptions() {
        return mOptions;
    }

    /**
     * Получить статистику по эксперименту
     */
    public StatisticsData getStatistics() {
        return mStatistics;
    }

    /**
     * Проверить, нужно ли добавлять новый автомобиль
     */
    protected void checkAutoGenerate() {
        --mAutoGeneratorCounter;
        if (mAutoGeneratorCounter <= 0) {
            synchronized (mUpdateLock) {
                mAutoGeneratorCounter = mOptions.GeneratorFrequency;
                ++mAutosToAdd;
                int attemptsCount = 0;
                while (mAutosToAdd > 0 && attemptsCount < mOptions.LinesCount) {
                    int lineIndex = mRandomGenerator.nextInt(mOptions.LinesCount);
                    if (mHighway.isLineAddAvailable(lineIndex)) {
                        int speed = mOptions.AutoMinSpeed + mRandomGenerator.nextInt(mSpeedDelta);
                        int skin = CoreDefines.AUTO_USE_SKINS ? mRandomGenerator.nextInt(AutoSkin.values().length) : 0;
                        mHighway.addAuto(lineIndex, new AutoData(speed, speed, AutoSkin.values()[skin]));
                        --mAutosToAdd;
                    }
                    ++attemptsCount;
                }
            }
        }
    }

    /**
     * Обновить эксперимент - сдвинуть машины и т.д.
     */
    protected void update() {
        synchronized (mUpdateLock) {
            mHighway.update();
            mPanel.update(mHighway);
        }
    }

    /**
     * Инициализирует автостраду -
     * заполняет автостраду полосами, а на полосы ставит автомобили
     */
    protected void init() {
        mHighway.init(mOptions.LinesCount);
        mAutosToAdd = mRandomGenerator.nextInt(mOptions.LinesCount);
        checkAutoGenerate();
    }

    /**
     * Очистить автостраду
     */
    protected void clear() {
        synchronized (mUpdateLock) {
            mHighway.clear();
        }
    }

    /**
     * Установить графическую составляющую <br>
     *
     * @param highwayPanel объект, гдe будет отображаться автострада
     */
    public void setGuiPanel(IHighwayPanel highwayPanel) {
        mPanel = highwayPanel;
        highwayPanel.setCallback(this);
    }

    @Override
    public void onSizeChanged(int width, int height) {
        synchronized (mUpdateLock) {
            mHighway.setSize(width, height);
        }
    }

    @Override
    public void onTouchEvent(float x, float y) {
        synchronized (mUpdateLock) {
            mHighway.onTouchEvent(x, y);
        }
    }
}
