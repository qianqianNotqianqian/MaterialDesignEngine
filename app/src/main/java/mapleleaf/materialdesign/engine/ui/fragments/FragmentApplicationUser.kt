package mapleleaf.materialdesign.engine.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CheckBox
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.databinding.FragmentAppListBinding
import mapleleaf.materialdesign.engine.model.AppInfo
import mapleleaf.materialdesign.engine.ui.adapter.AdapterAppList
import mapleleaf.materialdesign.engine.ui.dialog.DialogAppOptions
import mapleleaf.materialdesign.engine.ui.dialog.DialogProgressBar
import mapleleaf.materialdesign.engine.ui.dialog.DialogSingleAppOptions
import mapleleaf.materialdesign.engine.utils.helper.AppListHelper
import mapleleaf.materialdesign.engine.utils.toast
import java.lang.ref.WeakReference

class FragmentApplicationUser(private val myHandler: Handler) : Fragment() {
    private lateinit var appListHelper: AppListHelper
    private var appList: ArrayList<AppInfo>? = null
    private lateinit var binding: FragmentAppListBinding
    private lateinit var progressBar: DialogProgressBar
    private var keywords = ""

    constructor() : this(Handler(Looper.getMainLooper()))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        progressBar = DialogProgressBar(requireActivity(), "FragmentApplicationUser")
        appListHelper = AppListHelper(requireContext())
        binding = FragmentAppListBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appList.addHeaderView(
            this.layoutInflater.inflate(
                R.layout.list_item_application_select_all,
                null
            )
        )

        val onItemLongClick = AdapterView.OnItemLongClickListener { parent, _, position, _ ->
            if (position < 1)
                return@OnItemLongClickListener true
            val adapter = (parent.adapter as HeaderViewListAdapter).wrappedAdapter
            val app = adapter.getItem(position - 1) as AppInfo
            DialogSingleAppOptions(requireActivity(), app, myHandler).showSingleAppOptions()
            true
        }

        binding.appList.onItemLongClickListener = onItemLongClick
        binding.fabApps.setOnClickListener {
            getSelectedAppShowOptions(requireActivity())
        }

        this.setList()
    }

    private fun getSelectedAppShowOptions(activity: Activity) {
        var adapter = binding.appList.adapter
        adapter = (adapter as HeaderViewListAdapter).wrappedAdapter
        val selectedItems = (adapter as AdapterAppList).getSelectedItems()
        if (selectedItems.size == 0) {
            toast(getString(R.string.app_selected_none))
            return
        }

        if (selectedItems.size == 1) {
            DialogSingleAppOptions(
                activity,
                selectedItems.first(),
                myHandler
            ).showSingleAppOptions()
        } else {
            DialogAppOptions(activity, selectedItems, myHandler).selectUserAppOptions()
        }
    }

    private fun setList() {
        progressBar.showDialog()
        Thread {
            appList = appListHelper.getUserAppList()
            myHandler.post {
                progressBar.hideDialog()
            }
            binding.appList.run {
                setListData(appList, this)
            }
        }.start()
    }

    private fun setListData(dl: ArrayList<AppInfo>?, lv: ListView) {
        if (dl == null)
            return
        myHandler.post {
            try {
                val adapterObj = AdapterAppList(requireContext(), dl, keywords)
                val adapter: WeakReference<AdapterAppList> = WeakReference(adapterObj)
                lv.adapter = adapterObj
                lv.onItemClickListener = OnItemClickListener { _, itemView, postion, _ ->
                    if (postion == 0) {
                        val checkBox: CheckBox = itemView.findViewById(R.id.select_state_all)
                        checkBox.isChecked = !checkBox.isChecked
                        if (adapter.get() != null) {
                            adapter.get()!!.setSelectStateAll(checkBox.isChecked)
                            adapter.get()!!.notifyDataSetChanged()
                        }
                    } else {
                        val checkBox: CheckBox = itemView.findViewById(R.id.select_state)
                        checkBox.isChecked = !checkBox.isChecked
                        val all = lv.findViewById<CheckBox>(R.id.select_state_all)
                        if (adapter.get() != null) {
                            all.isChecked = adapter.get()!!.getIsAllSelected()
                        }
                    }
                    binding.fabApps.visibility =
                        if (adapter.get()?.hasSelected() == true) View.VISIBLE else View.GONE
                }
                val all = lv.findViewById<CheckBox>(R.id.select_state_all)
                all.isChecked = false
                binding.fabApps.isVisible = false
            } catch (ex: Exception) {
                Log.e("FragmentAppUser", "在设置适配器和监听器时发生错误: ${ex.message}")
            }
        }
    }

    var searchText: String
        get() {
            return keywords
        }
        set(value) {
            if (keywords != value) {
                keywords = value
                binding.appList.run {
                    setListData(appList, this)
                }
            }
        }

    fun reloadList() {
        setList()
    }
}
