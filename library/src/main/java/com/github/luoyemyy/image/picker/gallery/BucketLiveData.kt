package com.github.luoyemyy.image.picker.gallery

import android.app.Application
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.aclin.mvp.ListLiveData
import com.github.luoyemyy.aclin.mvp.LoadType
import com.github.luoyemyy.aclin.mvp.Paging
import com.github.luoyemyy.image.R
import java.io.File

class BucketLiveData(private val mApp: Application) : ListLiveData() {

    private val mBuckets: MutableList<Bucket> = mutableListOf()
    private var mBucketMap: MutableMap<String, Bucket> = mutableMapOf()

    val selectBucketLiveData = MutableLiveData<Bucket>()

    val imageLiveData = object : ListLiveData() {
        override fun loadData(
            bundle: Bundle?,
            search: String?,
            paging: Paging,
            loadType: LoadType
        ): List<DataItem>? {
            return mBucketMap[getSelectBucketId()]?.images
        }

    }

    private val mContentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            loadRefresh()
        }
    }

    fun changeBucket(position: Int) {
        var selectPosition = -1
        var select: Bucket? = null
        mBuckets.forEachIndexed { index, bucket ->
            if (bucket.select) {
                selectPosition = index
            }
            bucket.select = index == position
            if (bucket.select) {
                select = bucket
            }
        }
        if (selectPosition != position) {
            imageLiveData.loadRefresh()
        }
        select?.apply {
            selectBucketLiveData.value = select
        }
    }

    override fun onActive() {
        mApp.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            mContentObserver
        )
    }

    override fun onInactive() {
        mApp.contentResolver.unregisterContentObserver(mContentObserver)
    }

    override fun loadData(
        bundle: Bundle?,
        search: String?,
        paging: Paging,
        loadType: LoadType
    ): List<DataItem>? {
        load(loadType.isInit())
        return mBuckets
    }

    override fun loadInitAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        imageLiveData.loadInit(null)
        return super.loadInitAfter(ok, items)
    }

    private fun getSelectBucketId(): String? {
        return mBuckets.firstOrNull { it.select }?.id
    }

    private fun load(init: Boolean) {

        val data = mApp.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf("_data", "bucket_id", "bucket_display_name", "date_added"),
            "mime_type like '%image/jp%' and _size > 0 ",
            null,
            "date_added DESC"
        )

        mBuckets.clear()
        mBucketMap.clear()

        val bucketAll =
            Bucket("bucketAll", mApp.getString(R.string.image_picker_gallery_bucket_all))
        mBuckets.add(bucketAll)
        mBucketMap[bucketAll.id] = bucketAll

        if (data != null) {
            while (data.moveToNext()) {
                val bucketId = data.getString(data.getColumnIndex("bucket_id"))
                val bucketName = data.getString(data.getColumnIndex("bucket_display_name"))
                val path = data.getString(data.getColumnIndex("_data"))
                if (!path.isNullOrEmpty() && File(path).exists()) {
                    val image = Image(path)
                    if (!bucketId.isNullOrEmpty() && !bucketName.isNullOrEmpty()) {
                        (mBucketMap[bucketId] ?: Bucket(bucketId, bucketName).apply {
                            mBucketMap[bucketId] = this
                            mBuckets.add(this)
                        }).images.add(image)
                    }
                    bucketAll.images.add(image)
                }
            }
        }
        data?.close()

        if (init) {
            bucketAll.select = true
            selectBucketLiveData.postValue(bucketAll)
        }
    }
}