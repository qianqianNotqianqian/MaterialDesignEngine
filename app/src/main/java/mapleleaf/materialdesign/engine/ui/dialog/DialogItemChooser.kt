package mapleleaf.materialdesign.engine.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AbsListView
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Filterable
import android.widget.TextView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.model.SelectItem
import mapleleaf.materialdesign.engine.ui.adapter.AdapterItemChooser

class DialogItemChooser(
    private val darkMode: Boolean,
    private var items: ArrayList<SelectItem>,
    private val multiple: Boolean = false,
    private var callback: Callback? = null,
) : DialogFullScreen(
    (if (items.size > 7) {
        R.layout.dialog_item_chooser
    } else {
        R.layout.dialog_item_chooser_small
    }),
    darkMode
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val absListView = view.findViewById<AbsListView>(R.id.item_list)
        setup(absListView)

        view.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
        view.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            this.onConfirm(absListView)
        }

        // 全选功能
        val selectAll = view.findViewById<CompoundButton?>(R.id.select_all)
        if (selectAll != null) {
            if (multiple) {
                val adapter = (absListView.adapter as AdapterItemChooser?)
                selectAll.visibility = View.VISIBLE
                selectAll.isChecked = items.filter { it.selected }.size == items.size
                selectAll.setOnClickListener {
                    adapter?.setSelectAllState((it as CompoundButton).isChecked)
                }
                adapter?.run {
                    setSelectStateListener(object : AdapterItemChooser.SelectStateListener {
                        override fun onSelectChange(selected: List<SelectItem>) {
                            selectAll.isChecked = selected.size == items.size
                        }
                    })
                }
            } else {
                selectAll.visibility = View.GONE
            }
        }

        // 长列表才有搜索
        if (items.size > 5) {
            val clearBtn = view.findViewById<View>(R.id.search_box_clear)
            val searchBox = view.findViewById<EditText>(R.id.search_box).apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s != null) {
                            clearBtn.visibility = if (s.length > 0) View.VISIBLE else View.GONE
                        }
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        (absListView.adapter as Filterable).getFilter()
                            .filter(if (s == null) "" else s.toString())
                    }
                })
            }
            clearBtn.visibility = if (searchBox.text.isNullOrEmpty()) View.GONE else View.VISIBLE
            clearBtn.setOnClickListener {
                searchBox.text = null
            }
        }

        updateTitle()
        updateMessage()
    }

    private var title: String = ""
    private var message: String = ""

    private fun updateTitle() {
        view?.run {
            findViewById<TextView?>(R.id.dialog_title)?.run {
                text = title
                visibility = if (title.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun updateMessage() {
        view?.run {
            findViewById<TextView?>(R.id.dialog_desc)?.run {
                text = message
                visibility = if (message.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    public fun setTitle(title: String): DialogItemChooser {
        this.title = title
        updateTitle()

        return this
    }

    public fun setMessage(message: String): DialogItemChooser {
        this.message = message
        updateMessage()

        return this
    }

    private fun setup(gridView: AbsListView) {
        gridView.adapter = AdapterItemChooser(gridView.context, items, multiple)
    }

    interface Callback {
        fun onConfirm(selected: List<SelectItem>, status: BooleanArray)
    }

    private fun onConfirm(gridView: AbsListView) {
        val adapter = (gridView.adapter as AdapterItemChooser)
        val items = adapter.getSelectedItems()
        val status = adapter.getSelectStatus()

        callback?.onConfirm(items, status)

        this.dismiss()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}
