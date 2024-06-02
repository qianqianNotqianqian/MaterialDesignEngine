package mapleleaf.materialdesign.engine.shared

import android.content.Context
import mapleleaf.materialdesign.engine.utils.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

open class ObjectStorage<T : Serializable>(private val context: Context) {
    private val objectStorageDir = "objects/"
    protected fun getSaveDir(configFile: String): String {
        return FileWrite.getPrivateFilePath(context, objectStorageDir + configFile)
    }

    open fun load(configFile: String): T? {
        val file = File(getSaveDir(configFile))
        if (file.exists()) {
            var fileInputStream: FileInputStream? = null
            var objectInputStream: ObjectInputStream? = null
            try {
                fileInputStream = FileInputStream(file)
                objectInputStream = ObjectInputStream(fileInputStream)
                return objectInputStream.readObject() as T?
            } catch (ex: Exception) {
            } finally {
                try {
                    objectInputStream?.close()
                    fileInputStream?.close()
                } catch (ex: Exception) {
                }
            }
        }
        return null
    }

    open fun save(obj: T?, configFile: String): Boolean {
        val file = File(getSaveDir(configFile))
        val parentFile = file.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (obj != null) {
            var fileOutputStream: FileOutputStream? = null
            var objectOutputStream: ObjectOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                objectOutputStream = ObjectOutputStream(fileOutputStream)
                objectOutputStream.writeObject(obj)
                return true
            } catch (ex: Exception) {
                toast("存储配置失败！")
                return false
            } finally {
                try {
                    objectOutputStream?.close()
                    fileOutputStream?.close()
                } catch (ex: Exception) {
                }
            }
        } else {
            if (file.exists()) {
                file.delete()
            }
        }
        return true
    }

    open fun remove(configFile: String) {
        val file = File(getSaveDir(configFile))
        if (file.exists()) {
            file.delete()
        }
    }

    open fun exists(configFile: String): Boolean {
        return File(getSaveDir(configFile)).exists()
    }
}
