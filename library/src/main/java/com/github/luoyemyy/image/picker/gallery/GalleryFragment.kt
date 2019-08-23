package com.github.luoyemyy.image.picker.gallery

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.aclin.permission.requestPermission
import com.github.luoyemyy.image.R
import com.github.luoyemyy.image.databinding.ImagePickerGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GalleryFragment : Fragment() {

    private lateinit var mBinding: ImagePickerGalleryBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mBehavior: BottomSheetBehavior<View>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ImagePickerGalleryBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBehavior = BottomSheetBehavior.from(mBinding.layoutBucket)
        mBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                } else {
                }
            }
        })
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.apply {
                adapter = ImageAdapter()
                layoutManager = StaggeredGridLayoutManager(mPresenter.getImageSpan(), RecyclerView.VERTICAL)
                addItemDecoration(GridDecoration.create(requireContext(), mPresenter.getImageSpan(), 1))
            }

            recyclerViewBucket.apply {
                setupLinear(BucketAdapter())
            }
        }
        requestPermission(this).granted {
            mPresenter.bucketLiveData.loadInit(null)
        }.denied {

        }.buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    inner class ImageAdapter : AbsAdapter(this, mPresenter.bucketLiveData.imageLiveData) {
        override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
            binding.setVariable(1, item)
            binding.executePendingBindings()
        }

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.image_picker_gallery_image
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableInit(): Boolean {
            return false
        }

        override fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
            return super.createContentBinding(inflater, parent, viewType)?.apply {
                root.layoutParams.width = mPresenter.getImageSize()
                root.layoutParams.height = mPresenter.getImageSize()
            }
        }
    }

    inner class BucketAdapter : AbsAdapter(this, mPresenter.bucketLiveData) {
        override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
            binding.setVariable(1, item)
            binding.executePendingBindings()
        }

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.image_picker_gallery_bucket
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun getItemClickViews(binding: ViewDataBinding): List<View> {
            return listOf(binding.root)
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            if (mBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                mPresenter.bucketLiveData.changeBucket(vh.adapterPosition)
                mBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }


    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {
        val bucketLiveData = BucketLiveData(mApp)

        private var mImageInfo: Pair<Int, Int>? = null

        fun getImageSpan(): Int {
            return getImageInfo().first
        }

        fun getImageSize(): Int {
            return getImageInfo().second
        }

        private fun getImageInfo() = mImageInfo ?: calculateImageItemSize()

        private fun calculateImageItemSize(): Pair<Int, Int> {
            val suggestSize = mApp.resources.displayMetrics.density * 80
            val screenWidth = mApp.resources.displayMetrics.widthPixels

            val span = (screenWidth / suggestSize).toInt()
            val size = screenWidth / span
            return Pair(span, size).apply {
                mImageInfo = this
            }
        }
    }
}