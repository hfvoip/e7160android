package com.jhearing.e7160sl.Utils;

import android.content.Context;
import android.util.Log;

import com.ark.ArkException;
import com.ark.Library;
import com.ark.ProductManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SDKSUtils {

    private static final String TAG = SDKSUtils.class.getName();
    private static SDKSUtils singleton = new SDKSUtils();
    private static String ezlib = null;
    private static Library lib;
    private ProductManager productManager;


    public static SDKSUtils instance() {
        return singleton;
    }

    public static String extractAsset(Context context, String asset) throws IOException {
        File file = new File(context.getFilesDir(), asset);
        try {
            try (InputStream inputStream = context.getAssets().open(asset)) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not open:" + asset, e);
        }
        return file.getAbsolutePath();
    }

    public void extractLib(Context context) {
        if (ezlib == null) {
            try {
                //ezlib = extractAsset(context, "E7150SL.library");
                ezlib = extractAsset(context, "E7160SL.library");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                // Can't do much without assets
            }
            LoadProductManager();
        }
    }

    public ProductManager getProductManager() {
        return productManager;
    }


    public Library getLibrary() {
        return lib;
    }

    public void LoadProductManager() {
        // Ensure assets are present
        if (ezlib == null) {
            Log.e(TAG, "Can't run demo - missing libraries");
            return;
        }

        // Run through the demo
        try {
            productManager = ProductManager.getInstance();
            Log.d(TAG, String.format("We got a ProductManager ver=%08x", productManager.getVersion()));

            // See if we can load a Library
            lib = productManager.loadLibraryFromFile(ezlib);
            Log.d(TAG, "lib.isHasKey()=" + (lib.isHasKey() ? "true" : "false"));


        } catch (ArkException e) {
            Log.e(TAG, "ArkException:" + e.getMessage());
        }
    }

}
