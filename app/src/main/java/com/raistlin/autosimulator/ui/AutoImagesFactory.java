package com.raistlin.autosimulator.ui;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.raistlin.autosimulator.logic.AutoSkin;
import com.raistlin.autosimulator.logic.AutoState;
import com.raistlin.autosimulator.logic.CoreDefines;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Класс, отвечающий за чтение изображений авто.
 * Умеет кешировать считанные изображени¤
 *
 * @author Артем
 */
@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public class AutoImagesFactory {

    private static final boolean BIG_SIZE = CoreDefines.AUTO_BIG_SIZE;

    protected static AutoImagesFactory mInstance;

    private static final String mImageSkins[] = {"green", "yellow", "police", "black", "white"};
    private static final String mImageStates[] = {"_normal.jpg", "_stopping.jpg",
            "_boosting.jpg", "_changing_left.jpg", "_changing_right.jpg",
            "_crashed.jpg", "_force_stopped.jpg"};

    private final Hashtable<Integer, Bitmap> mVerticalImages;
    private final Hashtable<Integer, Bitmap> mHorizontalImages;
    private final AssetManager mAssets;

    protected AutoImagesFactory(AssetManager assets) {
        mAssets = assets;
        mVerticalImages = new Hashtable<>();
        mHorizontalImages = new Hashtable<>();
    }

    private int getImageIndex(AutoState state, AutoSkin skin) {
        return state.ordinal() * 10 + skin.ordinal();
    }

    /**
     * Получить вертикальное изображение дл¤ машины в заданном состо¤нии
     */
    public Bitmap getVerticalImage(AutoState state, AutoSkin skin) {
        int key = getImageIndex(state, skin);
        if (mVerticalImages.containsKey(key)) {
            return mVerticalImages.get(key);
        } else {
            String filename = (BIG_SIZE ? "big/" : "small/") + "vertical/" +
                    mImageSkins[skin.ordinal()] + mImageStates[state.ordinal()];
            Bitmap b = getBitmap(filename);
            mVerticalImages.put(key, b);
            return b;
        }
    }

    /**
     * Получить горизонтальное изображение дл¤ машины в заданном состо¤нии
     */
    public Bitmap getHorizontalImage(AutoState state, AutoSkin skin) {
        int key = getImageIndex(state, skin);
        if (mHorizontalImages.containsKey(key)) {
            return mHorizontalImages.get(key);
        } else {
            String filename = (BIG_SIZE ? "big/" : "small/") + "horizontal/" +
                    mImageSkins[skin.ordinal()] + mImageStates[state.ordinal()];
            Bitmap b = getBitmap(filename);
            mHorizontalImages.put(key, b);
            return b;
        }
    }

    private Bitmap getBitmap(String filename) {
        try {
            return BitmapFactory.decodeStream(mAssets.open(filename, 0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AutoImagesFactory initInstance(AssetManager assets) {
        synchronized (AutoImagesFactory.class) {
            if (mInstance == null) {
                mInstance = new AutoImagesFactory(assets);
            }
        }
        return mInstance;
    }

    public static AutoImagesFactory getInstance() {
        if (mInstance == null) {
            throw new NullPointerException("Trying to use uninitialized images factory!");
        }
        return mInstance;
    }

}
