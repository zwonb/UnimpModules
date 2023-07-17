package com.yidont.unimp.modules

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.yidont.library.utils.isLocationEnabled
import com.yidont.library.utils.setWifiEnabled
import com.yidont.library.utils.startWifiSetting
import io.dcloud.common.core.permission.PermissionControler
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniDestroyableModule
import io.dcloud.feature.uniapp.utils.UniLogUtils


class WiFiModule : UniDestroyableModule() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val wifiManager by lazy(LazyThreadSafetyMode.NONE) {
        val context = mUniSDKInstance?.context?.applicationContext
        context?.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }
    private var wifiSuccess: UniJSCallback? = null
    private var wifiFail: UniJSCallback? = null

    /**
     * WiFi是否开启 true
     */
    @UniJSMethod(uiThread = false)
    fun getWiFiEnabled(): Boolean {
        return wifiManager?.isWifiEnabled ?: false
    }

    /**
     * 获取当前连接wifi
     */
    @UniJSMethod(uiThread = true)
    fun getConnectedWifi(success: UniJSCallback, fail: UniJSCallback) {
        this.wifiSuccess = success
        this.wifiFail = fail
        val context = mUniSDKInstance.context as Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !isLocationEnabled(context)) {
            fail.invoke("未开启定位服务")
            return
        }
        PermissionControler.requestPermissions(context, permissions, 1)
    }

    /**
     * 获取当前wifi、附近wifi列表
     */
    @UniJSMethod(uiThread = true)
    fun getWifiList(success: UniJSCallback, fail: UniJSCallback) {
        this.wifiSuccess = success
        this.wifiFail = fail
        val context = mUniSDKInstance.context as Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !isLocationEnabled(context)) {
            fail.invoke("未开启定位服务")
            return
        }
        registerWifiScanReceiver()
        PermissionControler.requestPermissions(context, permissions, 2)
    }

    private var wifiScanReceiver: BroadcastReceiver? = null

    private fun registerWifiScanReceiver() {
        val context = mUniSDKInstance?.context ?: return
        unRegisterWifiScanReceiver()
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                wifiSuccess?.invoke(getScanResult(success))
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        try {
            context.registerReceiver(wifiScanReceiver, intentFilter)
        } catch (e: Exception) {
            UniLogUtils.e("注册wifi扫描广播出错", e)
        }
    }

    private fun unRegisterWifiScanReceiver() {
        val context = mUniSDKInstance?.context ?: return
        wifiScanReceiver ?: return
        try {
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            UniLogUtils.e("取消wifi扫描广播出错", e)
        }
    }

    /**
     * wifiListUpdated false扫描失败,使用旧结果
     */
    private fun getScanResult(wifiListUpdated: Boolean): JSONObject {
        val result = JSONObject()
        result["wifiListUpdated"] = wifiListUpdated
        result["currentWiFi"] = getCurrentWiFi()
        result["wifiList"] = getScanResultList()
        return result
    }

    /**
     * 获取当前连接wifi
     */
    @Suppress("DEPRECATION")
    private fun getCurrentWiFi(): JSONObject = JSONObject().apply {
        var ssid = ""
        var bssid = ""
        try {
            ssid = wifiManager?.connectionInfo?.ssid ?: ""
            bssid = wifiManager?.connectionInfo?.bssid ?: ""
        } catch (e: Exception) {
            UniLogUtils.e("connectionInfo 权限不足", e)
        }
        if (ssid.startsWith('"')) {
            ssid = ssid.trimStart { it == '"' }.trimEnd { it == '"' }
        }
        put("ssid", ssid)
        put("bssid", bssid)
        UniLogUtils.i("当前wifi：${this}")
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun getScanResultList() = JSONArray().apply {
        try {
            wifiManager?.scanResults?.forEach {
                val item = JSONObject().apply {
                    put("ssid", it.SSID)
                    put("bssid", it.BSSID)
                }
                add(item)
                UniLogUtils.i("scan wifi:${item}")
            }
        } catch (e: Exception) {
            UniLogUtils.e("scanResults 权限不足", e)
        }
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private fun wifiCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val transportWiFi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val bssid = try {
                @Suppress("DEPRECATION")
                wifiManager?.connectionInfo?.bssid ?: ""
            } catch (e: Exception) {
                ""
            }
            val map = mutableMapOf<String, Any>().apply {
                val rssi = wifiRSSI(networkCapabilities)
                put("isWiFi", transportWiFi)
                put("wifiRSSI", rssi)
                put("bssid", bssid)
            }
            UniLogUtils.i("wifi变化 $map")
            mUniSDKInstance?.fireGlobalEventCallback("onNetworkChanged", map)
        }
    }

    @UniJSMethod(uiThread = true)
    fun registerNetworkListener() {
        unregisterNetworkListener()
        networkCallback = wifiCallback().also {
            val connectivityManager =
                mUniSDKInstance.context.getSystemService(ConnectivityManager::class.java)
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            try {
                connectivityManager.registerNetworkCallback(request, it)
            } catch (e: Exception) {
                UniLogUtils.e("registerNetworkListener 出错", e)
            }
        }
    }

    @UniJSMethod(uiThread = true)
    fun unregisterNetworkListener() {
        networkCallback?.let {
            val connectivityManager =
                mUniSDKInstance.context.getSystemService(ConnectivityManager::class.java)
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                UniLogUtils.e("unregisterNetworkListener 出错", e)
            }
        }
    }

    @UniJSMethod(uiThread = true)
    fun closeWiFi() {
        setWifiEnabled(mUniSDKInstance.context, false)
    }

    @UniJSMethod(uiThread = true)
    fun jumpWifiSetting() {
        startWifiSetting(mUniSDKInstance.context)
    }

    private fun wifiRSSI(networkCapabilities: NetworkCapabilities) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            networkCapabilities.signalStrength
        } else {
            @Suppress("DEPRECATION")
            wifiManager?.connectionInfo?.rssi ?: -100
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            when (requestCode) {
                // 获取当前wifi
                1 -> {
                    val result = JSONObject()
                    result["currentWiFi"] = getCurrentWiFi()
                    wifiSuccess?.invoke(result)
                }

                // 获取wifi列表
                2 -> {
                    @Suppress("DEPRECATION")
                    val success = wifiManager?.startScan() ?: false
                    if (!success) {
                        // 由于短时间扫描过多，扫描请求可能遭到节流。9.0+前台运行两分钟最多4次，8.0+后台30分钟内最多1次
                        // 设备处于空闲状态，扫描已停用。
                        // WLAN 硬件报告扫描失败。
                        UniLogUtils.e("startScan 扫描失败，使用旧数据")
                    }
                }
            }
        } else {
            wifiFail?.invoke("未授予定位权限")
        }
    }

    override fun destroy() {
        unregisterNetworkListener()
        unRegisterWifiScanReceiver()
    }

}