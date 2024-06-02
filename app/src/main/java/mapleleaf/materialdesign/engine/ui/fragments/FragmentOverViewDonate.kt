package mapleleaf.materialdesign.engine.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import mapleleaf.materialdesign.engine.databinding.FragmentDonateBinding
import mapleleaf.materialdesign.engine.utils.toast

class FragmentOverViewDonate : Fragment() {

    private var binding: FragmentDonateBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.payPaypal.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/duduski")))
        }
        binding!!.payAlipay.setOnClickListener {

        }
        binding!!.payWechat.setOnClickListener {
            /*
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("weixin://dl/business/?ticket=wxp://f2f0YqS-OUviH9sQNUDgXJhOP3fld3htEqqO")))
            } catch (ex: Exception) {
                Toast.makeText(context!!, "暂不支持此方式！", Toast.LENGTH_SHORT).show()
            }
            */
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vtools.omarea.com/")))
            toast("暂不支持直接调起，请保存收款码然后使用微信扫码（在扫一扫界面从相册选择图片）！")
        }
        binding!!.payAlipayCode.setOnClickListener {
            //获取剪贴板管理器：
            val cm =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText("支付宝红包码", "511531087")
            // 将ClipData内容放到系统剪贴板里
            cm.setPrimaryClip(mClipData)
            toast("红包码已复制！")
            try {
                val packageManager = requireContext().applicationContext.packageManager
                val intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
                startActivity(intent)
            } catch (ex: Exception) {
            }
        }
        binding!!.payAlipayCommand.setOnClickListener {
            //获取剪贴板管理器：
            val cm =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText(
                "支付宝口令",
                "支付宝发红包啦！即日起还有机会额外获得余额宝消费红包！长按复制此消息，打开最新版支付宝就能领取！Z3DGmD87Rf"
            )
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData)
            toast("红包口令已复制！")
            try {
                val packageManager = requireContext().applicationContext.packageManager
                val intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
                startActivity(intent)
            } catch (ex: Exception) {
            }
        }
    }

    companion object {
        fun createPage(): Fragment {
            val fragment = FragmentOverViewDonate()
            return fragment
        }
    }
}
