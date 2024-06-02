package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipFile
import kotlin.math.log10
import kotlin.math.pow

class ActivityApplicationDetails : UniversalActivityBase() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var nullList: AppCompatImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: AdapterAppDetail
    private lateinit var progressBar: ProgressBar
    private lateinit var loading: AppCompatImageView
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun getLayoutResourceId() = R.layout.activity_application_details

    @SuppressLint("ClickableViewAccessibility")
    override fun initializeComponents(savedInstanceState: Bundle?) {

        nullList = findViewById(R.id.null_list)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        loading = findViewById(R.id.loading)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        val packageName = intent.getStringExtra("packageName")
        if (packageName != null) {
            loadAppDetails(packageName)
            packageName.let {
                val appInfo = packageManager.getApplicationInfo(it, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                setToolbarTitle(label)
            }
        } else {
            nullList.isVisible = true
            progressBar.isVisible = false
            recyclerView.isVisible = false
            loading.isVisible = false
            toast("缺少必要参数:packageName")
        }

        adapter = AdapterAppDetail(this, ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        FastScrollerBuilder(recyclerView).build()
        progressBar.isIndeterminate = true

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()
        swipeRefreshLayout.isEnabled = false

        swipeRefreshLayout.setOnRefreshListener {
            if (packageName != null) {
                loadAppDetails(packageName)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun loadAppDetails(packageName: String) {
        lifecycleScope.launch {
            try {
                val details: List<String> = withContext(Dispatchers.Default) {
                    val details = mutableListOf<String>()
                    try {
                        val packageManager = packageManager
                        val appInfo =
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.GET_META_DATA
                            )
                        val packageInfo = packageManager.getPackageInfo(
                            packageName,
                            PackageManager.GET_PERMISSIONS or
                                    PackageManager.GET_SERVICES or
                                    PackageManager.GET_RECEIVERS or
                                    PackageManager.GET_ACTIVITIES or
                                    PackageManager.GET_SIGNING_CERTIFICATES
                        )

                        val servicesDeferred = async { packageInfo.services?.sortedBy { it.name } }
                        val receiversDeferred =
                            async { packageInfo.receivers?.sortedBy { it.name } }
                        val activitiesDeferred =
                            async { packageInfo.activities?.sortedBy { it.name } }
                        val providersDeferred = async {
                            packageManager.queryContentProviders(
                                packageName,
                                packageInfo.applicationInfo.uid,
                                0
                            ).sortedBy { it.name }
                        }

                        val services = servicesDeferred.await()
                        val receivers = receiversDeferred.await()
                        val activities = activitiesDeferred.await()
                        val providers = providersDeferred.await()

                        details.add("名称：\n${packageManager.getApplicationLabel(appInfo)}\n" + "通常是应用的通用名称，但有的会使用包名当作名称")
                        details.add("包名：\n${appInfo.packageName}\n软件的包名，一般不会作为名称")
                        details.add("版本名：\n${packageInfo.versionName}\n显示应用版本的名称，通常不是数字，有其他字符")
                        details.add("版本号：\n${packageInfo.longVersionCode}\n显示应用版本的代码，一定是是数字")
                        details.add("大小：\n${formatSize(getApplicationSize(appInfo.sourceDir))}\n显示应用的大小，不同算法有不同的大小")
                        details.add("目标 SDK 版本：\n${packageInfo.applicationInfo.targetSdkVersion}\n显示应用的目标 SDK 版本，可以用更高的安卓版本运行，但若差距太大可能不兼容")
                        details.add("最小 SDK 版本：\n${packageInfo.applicationInfo.minSdkVersion}\n显示应用的最小 SDK 版本，这决定了能够在特定安卓版本以上运行，低于安卓版本就不能安装")
                        details.add("flags：\n${appInfo.flags}\n用于配置应用程序行为或特性的机制，改变应用程序的行为或启用/禁用特定的功能")
                        details.add("uid：\n${appInfo.uid}\n用来唯一标识一个用户的数字，每个用户都有一个唯一的 UID")
                        details.add("安装路径：\n${appInfo.sourceDir}\n在操作系统中存储应用程序文件的位置，通常是在设备的内部存储或外部存储中的特定目录")
                        details.add("数据路径：\n${context.filesDir.path}/$packageName\nAndroid 应用程序的内部数据目录（根目录下的data），显示应用程序的完整数据路径")
                        details.add("外部数据路径：\n/storage/emulated/0/Android/data/$packageName\nAndroid 应用程序的外部数据目录（Android目录下的data），显示应用程序的完整数据路径")
                        details.add("native 路径：\n${appInfo.nativeLibraryDir}\n指存储应用程序本地代码库的目录")
                        details.add("类型：\n${if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) "系统应用" else "用户应用"}\n区分应用的种类，避免混淆导致不好管理")
                        details.add("MD5：\n${calculateAppMD5(appInfo.sourceDir)}\n获取应用的 MD5（Message Digest Algorithm 5）哈希值是一种常见的做法，可以用于验证文件的完整性")
                        details.add("进程名：\n${appInfo.processName}\n每个应用程序在其自己的进程中运行，进程名通常与应用的包名相同。但是，开发者可以通过设置 android:process 属性来指定应用程序的进程名，以实现特定的进程管理需求")

                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        if (intent != null) {
                            val componentName = intent.component
                            if (componentName != null) {
                                details.add("入口 Activity：\n${componentName.className}\n用户启动应用时首先显示的界面，也是应用的主要入口点")
                            }
                        } else {
                            details.add("入口 Activity：\n无法获取入口 Activity\n用户启动应用时首先显示的界面，也是应用的主要入口点")
                        }
                        if (appInfo.className != null) {
                            details.add("Application：\n${appInfo.className}\nAAndroid 应用的全局单例类，用于管理应用的全局状态和生命周期")
                        } else {
                            details.add("Application：\n无法获取Application\nAndroid 应用的全局单例类，用于管理应用的全局状态和生命周期")
                        }

                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val signingInfoDeferred = async { packageInfo.signingInfo }
                        val permissionsDeferred = async { packageInfo.requestedPermissions }

                        val signingInfo = signingInfoDeferred.await()
                        val permissions = permissionsDeferred.await()

                        signingInfo?.let {
                            val signatures = signingInfo.apkContentsSigners
                            for (signature in signatures) {
                                val signatureBytes = signature?.toByteArray()
                                if (signatureBytes != null) {
                                    details.add(
                                        "签名MD5：\n${getSignatureHash(
                                            signatureBytes,
                                            "MD5"
                                        )?.chunked(2)?.joinToString(":")}\n用于验证文件的完整性"
                                    )
                                    details.add(
                                        "签名SHA-1：\n${getSignatureHash(
                                            signatureBytes,
                                            "SHA-1"
                                        )?.chunked(2)?.joinToString(":")}\n一种加密哈希函数，常用于生成数据的唯一标识符，通常也用于验证文件的完整性和安全性"
                                    )
                                    details.add(
                                        "签名SHA-256：\n${getSignatureHash(
                                            signatureBytes,
                                            "SHA-256"
                                        )?.chunked(2)?.joinToString(":")}\nSHA-2家族中的一种哈希算法，用于生成256位长度的哈希值"
                                    )
                                }
                                val cert = signatureBytes?.let { parseCertificate(it) }
                                if (cert != null) {
                                    details.add("签名算法：\n${cert.sigAlgName}\n用于生成和验证数字签名的一种特定算法。数字签名是一种用于验证数据完整性、真实性和不可否认性的技术。发送方使用私钥对数据进行签名，接收方使用相应的公钥来验证签名")
                                    details.add("签名算法OID：\n${cert.sigAlgOID}\n用于标识数字签名算法的一种标准化表示方式。OID是一个由数字和点号组成的标识符，用于在国际上唯一标识各种对象、属性和操作")
                                    details.add("证书序列号：\n${cert.serialNumber}\n数字证书中的一个唯一标识符，由颁发该证书的证书颁发机构（CA）分配。它用于区分不同的证书，并在证书的生命周期内保持唯一性")
                                    details.add(
                                        "证书有效期：\n${sdf.format(cert.notBefore)} 到 ${sdf.format(cert.notAfter)}\n数字证书在发行后可被认为是有效的时间段")
                                    details.add("证书所有者：\n${cert.subjectDN.name}\n指持有数字证书的实体或组织。在SSL/TLS等安全通信协议中，数字证书用于验证通信方的身份，并确保通信的机密性和完整性")
                                    details.add("证书发行者：\n${cert.issuerDN.name}\n在SSL/TLS等安全通信协议中，证书发行者负责颁发数字证书的可信机构")

                                    val publicKey = cert.publicKey
                                    details.add("公钥格式：\n${publicKey.format}\n特定于算法，并且可能因算法的不同而有所差异")
                                    details.add("公钥算法：\n${publicKey.algorithm}\n一种密码学算法，用于加密和解密数据以及数字签名")
                                    if (publicKey is RSAPublicKey) {
                                        details.add("公钥指数：\n${publicKey.publicExponent}\nRSA加密算法中的一个参数，通常表示为 e。它是用于加密的公钥的一部分。在RSA加密过程中，明文数据会被使用公钥的指数进行加密，然后只能使用相应的私钥才能解密")
                                        details.add("模数大小：\n${publicKey.modulus.bitLength()}\n在RSA等公钥加密算法中，模数的大小决定了密钥的安全性")
                                        val modulusString =
                                            "00:" + publicKey.modulus.toString(16).toLowerCase(Locale.ROOT)
                                                .padStart(64, '0').chunked(2).joinToString(":")
                                        details.add("模数：\n$modulusString\n在数字签名过程中使用的一个重要参数。数字签名是一种用于验证数据完整性和真实性的技术，其中发送方使用其私钥对数据进行签名，而接收方使用发送方的公钥来验证签名")
                                    } else {
                                        details.add("签名：\n不支持的公钥类型\n无法获取签名信息")
                                    }
                                }
                            }
                        }

                        if (permissions != null && permissions.isNotEmpty()) {
                            details.add("权限：\n共 ${permissions.size} 个\n显示应用的详细权限信息")

                            val declaredPermissions =
                                packageInfo.requestedPermissions ?: emptyArray()

                            for (permission in permissions) {
                                if (!declaredPermissions.contains(permission)) {
                                    details.add("- $permission\n自定义权限")
                                } else {
                                    try {
                                        val permissionInfo =
                                            packageManager.getPermissionInfo(permission, 0)
                                        val permissionDetail =
                                            "- ${permissionInfo.name}\n> ${permissionInfo.loadLabel(packageManager)}\n> ${permissionInfo.loadDescription(packageManager) ?: "无描述"}"
                                        details.add(permissionDetail)
                                    } catch (e: PackageManager.NameNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            // 检查是否有未在权限列表中列出的自定义权限
                            for (declaredPermission in declaredPermissions) {
                                if (!permissions.contains(declaredPermission)) {
                                    details.add(
                                        "- $declaredPermission\n自定义权限"
                                    )
                                }
                            }
                        } else {
                            details.add("权限：\n应用没有请求任何权限\n显示应用的详细权限信息")
                        }

                        val items = listOf(
                            Pair("活动", activities),
                            Pair("服务", services),
                            Pair("广播接收器", receivers),
                            Pair("内容提供者", providers)
                        )

                        items.forEach { (label, itemList) ->
                            itemList?.let {
                                details.add("$label\n共 ${it.size} 个\n应用的组件，部分组件或界面不能直接打开")
                                it.forEach { item ->
                                    val exportedText = if (item.exported) "是" else "否"
                                    details.add(
                                        "- ${item.name}\n> 导出：$exportedText\n> ${item.loadLabel(packageManager)}"
                                    )
                                }
                            }
                        }

                        val dexFilesDeferred = async { getDexFiles(appInfo.sourceDir) }
                        val dexFiles = dexFilesDeferred.await()

                        if (dexFiles.isNotEmpty()) {
                            details.add(
                                "Dex文件：\n${TextUtils.join(",", dexFiles)}\n获取Dex文件的数量"
                            )
                        } else {
                            details.add("Dex文件：\n未找到 Dex 文件\n获取Dex文件的数量")
                        }

                        val sharedLibrariesDeferred = async { getSharedLibraries(appInfo) }
                        val sharedLibraries = sharedLibrariesDeferred.await()

                        if (sharedLibraries.isNotEmpty()) {
                            details.add(
                                "共享库文件：\n${sharedLibraries.let { TextUtils.join("\n", it) }}\n通常是指应用程序使用的本地代码库，这些库文件可以由多个应用程序共享，并在运行时被动态加载到应用程序的进程中。在 Android 中，共享库文件通常以 .so 扩展名结尾，是用 C、C++ 等语言编写的本地代码库。"
                            )
                        } else {
                            details.add("共享库文件：\n无法获取共享库文件列表或没有使用共享库文件\n通常是指应用程序使用的本地代码库，这些库文件可以由多个应用程序共享，并在运行时被动态加载到应用程序的进程中。在 Android 中，共享库文件通常以 .so 扩展名结尾，是用 C、C++ 等语言编写的本地代码库。")
                        }

                        withContext(Dispatchers.Main) {
                            adapter.updateItems(details)
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    } catch (e: CertificateException) {
                        throw RuntimeException(e)
                    }
                    return@withContext details
                }
                animatedVectorDrawable?.start()
                swipeRefreshLayout.isEnabled = true
                swipeRefreshLayout.isRefreshing = false
                adapter.updateItems(details)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("加载失败")
                    animatedVectorDrawable?.stop()
                    swipeRefreshLayout.isEnabled = true
                    swipeRefreshLayout.isRefreshing = false
                    loading.isVisible = false
                    progressBar.isVisible = false
                }
            } finally {
                withContext(Dispatchers.Main) {
                    animatedVectorDrawable?.stop()
                    swipeRefreshLayout.isEnabled = true
                    swipeRefreshLayout.isRefreshing = false
                    loading.isVisible = false
                    progressBar.isVisible = false
                }
            }
        }
    }

    private fun getSignatureHash(signatureBytes: ByteArray, algorithm: String): String? {
        try {
            val md = MessageDigest.getInstance(algorithm)
            val digest = md.digest(signatureBytes)
            val hexString = StringBuilder()
            for (b in digest) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseCertificate(signatureBytes: ByteArray): X509Certificate {
        val cf = CertificateFactory.getInstance("X.509")
        return cf.generateCertificate(ByteArrayInputStream(signatureBytes)) as X509Certificate
    }

    private fun getDexFiles(sourceDir: String): List<String> {
        val dexFiles: MutableList<String> = java.util.ArrayList()
        try {
            val zipFile = ZipFile(sourceDir)
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith("classes") && entry.name.endsWith(".dex")) {
                    dexFiles.add(entry.name)
                }
            }
            zipFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return dexFiles.sortedBy { extractNumber(it) }
    }

    private fun extractNumber(filename: String): Int {
        val regex = Regex("""classes(\d+)\.dex""")
        val matchResult = regex.find(filename)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun getSharedLibraries(appInfo: ApplicationInfo): List<String?> {
        return if (appInfo.sharedLibraryFiles != null) {
            listOf(*appInfo.sharedLibraryFiles)
        } else {
            java.util.ArrayList()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatSize(size: Long): String {
        if (size <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.1f %s", size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    private fun getApplicationSize(sourceDir: String?): Long {
        return if (sourceDir != null) File(sourceDir).length() else 0
    }

    private fun calculateAppMD5(filePath: String): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val fis = FileInputStream(filePath)
            val dis = DigestInputStream(fis, md)
            val buffer = ByteArray(8192)
            while (dis.read(buffer) != -1) {
                Log.d("DataReading", "Data read from stream")
            }
            // 计算散列值
            val digest = md.digest()
            val hexString = StringBuilder()
            for (b in digest) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            fis.close()
            dis.close()
            hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    class AdapterAppDetail(private val context: Context, private var items: List<String>) :
        RecyclerView.Adapter<AdapterAppDetail.ApplicationDetailsViewHolder>() {

        @SuppressLint("NotifyDataSetChanged")
        fun updateItems(newItems: List<String>) {
//            val diffResult = DiffUtil.calculateDiff(DiffCallback(items, newItems))
//            items = newItems
//            diffResult.dispatchUpdatesTo(this)
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ApplicationDetailsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application_detail, parent, false)
            return ApplicationDetailsViewHolder(view)
        }

        override fun onBindViewHolder(holder: ApplicationDetailsViewHolder, position: Int) {
            val item = items[position]
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
            holder.itemView.layoutParams = layoutParams
            holder.appDetails.text = item

            if (item.contains("导出：是")) {
                holder.appDetails.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else if (item.contains("导出：否")) {
                holder.appDetails.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                holder.appDetails.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            }

            holder.appDetails.setOnClickListener {
                val clickedPosition = holder.bindingAdapterPosition
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    val selectedItem = items[clickedPosition]
                    showCopyDialog(selectedItem)
                }
            }

            holder.appDetailsMaterialCardView.setOnClickListener {
                val clickedPosition = holder.bindingAdapterPosition
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    val selectedItem = items[clickedPosition]
                    showCopyDialog(selectedItem)
                }
            }

            holder.appDetailsHorizontalScrollView.isFillViewport = true

//            holder.appDetailsHorizontalScrollView.post {
//                val layoutParamsScrollView = holder.appDetailsHorizontalScrollView.layoutParams
//                val minScrollWidth = holder.appDetails.minWidth // 设置最小滚动宽度，根据实际情况调整
//                val textWidth = holder.appDetails.width
//                layoutParamsScrollView.width = maxOf(minScrollWidth, textWidth)
//                holder.appDetailsHorizontalScrollView.layoutParams = layoutParamsScrollView
//            }
        }

        override fun onViewAttachedToWindow(holder: ApplicationDetailsViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        @SuppressLint("InflateParams")
        private fun showCopyDialog(item: String) {
            MessageDialog(
                context.getString(R.string.dialog_title),
                item,
                "确定",
                "取消"
            )
                .setButtonOrientation(LinearLayout.HORIZONTAL)
                .setOkTextInfo(TextInfo().setFontColor(Color.parseColor("#EB5545")).setBold(true))
                .setCancelButton { _, _ ->
                    false
                }
                .setOkButton { _, _ ->
                    copyToClipboard(item)
                    true
                }.show()
        }

        private fun copyToClipboard(textToCopy: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("AppDetails", textToCopy)
            clipboard.setPrimaryClip(clip)
            toast("已复制到剪贴板")
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ApplicationDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val appDetails: TextView = itemView.findViewById(R.id.appDetails)
            val appDetailsMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.appDetailsMaterialCardView)
            val appDetailsHorizontalScrollView: HorizontalScrollView =
                itemView.findViewById(R.id.appDetailsHorizontalScrollView)

            init {
                val baseColor = ContextCompat.getColor(context, R.color.background_color)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                appDetailsMaterialCardView.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}
