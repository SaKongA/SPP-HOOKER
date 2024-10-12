package com.sakongapps.spp_hooker

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {
    private val targetPackageName = "com.huawei.audiogenesis"

    // 当目标包加载时执行
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != targetPackageName) return

        // 查找目标类 SppManager
        val targetClass = XposedHelpers.findClass(
            "com.huawei.audiobluetooth.layer.device.spp.SppManager",
            lpparam.classLoader
        )

        // 针对 write 和 receive 方法进行 Hook
        listOf("write", "receive").forEach { methodName ->
            targetClass.methods.find { it.name == methodName }?.let { method ->
                XposedBridge.log("发现目标方法：$methodName!")
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        logData(methodName, param.args[0] as ByteArray)
                    }
                })
            }
        }
    }

    // 记录数据包信息的通用方法
    private fun logData(methodName: String, byteArray: ByteArray) {
        XposedBridge.log("${if (methodName == "write") "W-发送" else "R-接收"}数据包 [Len]: ${byteArray.size}, [Data]: ${byteArray.contentToString()}")
        XposedBridge.log("-------------------------------------------")
    }
}