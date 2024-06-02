package mapleleaf.materialdesign.engine.initJNI

class EngineJNI {
    external fun getKernelPropLong(prop: String?): Long

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}