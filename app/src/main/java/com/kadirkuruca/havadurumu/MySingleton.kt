package com.kadirkuruca.havadurumu

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

/**
 * Created by Kadir on 3.03.2018.
 */

class MySingleton private constructor(context: Context) {
    private var mRequestQueue: RequestQueue? = null
    val imageLoader: ImageLoader

    // getApplicationContext() is key, it keeps you from leaking the
    // Activity or BroadcastReceiver if someone passes one in.
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mCtx?.applicationContext)
            }
            return mRequestQueue
        }

    init {
        mCtx = context
        mRequestQueue = requestQueue

        imageLoader = ImageLoader(mRequestQueue,
                object : ImageLoader.ImageCache {
                    private val cache = LruCache<String, Bitmap>(20)

                    override fun getBitmap(url: String): Bitmap {
                        return cache.get(url)
                    }

                    override fun putBitmap(url: String, bitmap: Bitmap) {
                        cache.put(url, bitmap)
                    }
                })
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue?.add(req)
    }

    companion object {
        private var mInstance: MySingleton? = null
        private var mCtx: Context? = null

        @Synchronized
        fun getInstance(context: Context): MySingleton? {
            if (mInstance == null) {
                mInstance = MySingleton(context)
            }
            return mInstance
        }
    }
}

