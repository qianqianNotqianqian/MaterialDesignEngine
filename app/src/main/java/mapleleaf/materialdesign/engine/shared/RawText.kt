package mapleleaf.materialdesign.engine.shared

import android.content.Context
import java.nio.charset.Charset

object RawText {
    fun getRawText(context: Context, id: Int): String {
        return try {
            String(
                context.resources.openRawResource(id).readBytes(),
                Charset.defaultCharset()
            ).replace(Regex("\r\n"), "\n").replace(Regex("\r\t"), "\t").replace(Regex("\r"), "\n")
        } catch (ex: Exception) {
            ""
        }
    }
}
