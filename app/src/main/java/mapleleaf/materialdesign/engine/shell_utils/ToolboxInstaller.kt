package mapleleaf.materialdesign.engine.shell_utils

import android.content.Context
import android.os.Build
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shared.FileWrite.getPrivateFilePath
import mapleleaf.materialdesign.engine.shared.FileWrite.writePrivateFile
import java.io.File
import java.util.Locale

class ToolboxInstaller(private val context: Context) {
    fun install(): String {

        val installPath: String = context.getString(R.string.toolkit_install_path)
        val abi = Build.SUPPORTED_ABIS.joinToString(" ").toLowerCase(Locale.getDefault())
        val fileName = if (abi.contains("arm64")) "toybox-outside64" else "toybox-outside";
        val toyboxInstallPath = "$installPath/$fileName"
        val outsideToybox = getPrivateFilePath(context, toyboxInstallPath)

        if (!File(outsideToybox).exists()) {
            writePrivateFile(context.assets, toyboxInstallPath, toyboxInstallPath, context)
        }

        return outsideToybox
    }
}