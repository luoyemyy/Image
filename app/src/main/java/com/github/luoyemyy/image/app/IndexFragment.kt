package com.github.luoyemyy.image.app


import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.image.app.databinding.FragmentIndexBinding

class IndexFragment : Fragment() {

    private lateinit var mBinding: FragmentIndexBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentIndexBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            swipeRefreshLayout.setup(mPresenter.listLiveData)
        }
        mPresenter.listLiveData.loadInit(arguments)
    }

    inner class Adapter : AbsAdapter(this, mPresenter.listLiveData) {
        override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
            binding.setVariable(1, item)
            binding.executePendingBindings()
        }

        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_index_item
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun getItemClickViews(binding: ViewDataBinding): List<View> {
            return listOf(binding.root)
        }

        override fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {
            val item = getItem(vh.adapterPosition) as? TextItem ?: return
            when (item.key) {
                "image" -> findNavController().navigate(R.id.action_indexFragment_to_galleryFragment)
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsPresenter(mApp) {

        val listLiveData = object : ListLiveData() {
            override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
                return listOf(
                    TextItem("image")
                )
            }
        }
    }
}
