package cn.qiuxiang.react.amap3d

import android.content.res.Resources
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
class AMapUtilsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), Inputtips.InputtipsListener, AMapLocationListener {
    override fun getName(): String {
        return "AMapUtils"
    }

    @ReactMethod
    fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double, promise: Promise) {
        promise.resolve(AMapUtils.calculateLineDistance(LatLng(lat1, lng1), LatLng(lat2, lng2)))
    }

    @ReactMethod
    fun getPoiListByKeyword(key: String, city: String ) {
        if (!key.isNullOrEmpty()) {
            var searchCity = ""
            if (city.isNullOrEmpty()) {
                searchCity = "长沙市"
            } else {
                searchCity = city
            }
            val inputquery = InputtipsQuery(key, searchCity)
            val inputTips = Inputtips(reactContext, inputquery)
            inputTips.setInputtipsListener(this)
            inputTips.requestInputtipsAsyn()
        }
    }

    override fun onGetInputtips(tipList: MutableList<Tip>?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS && tipList != null) {// 正确返回
            val listJson = JSONArray()
            for (i in tipList.indices) {
                val tip = tipList.get(i)
                val jsonTip = JSONObject()
                jsonTip.put("name", tip.name)
                jsonTip.put("address", tip.address)
                jsonTip.put("latitude", tip.point.latitude)
                jsonTip.put("longitude", tip.point.longitude)
                listJson.put(jsonTip)
            }
            val data = Arguments.createMap()
            data.putString("tipList", listJson.toString())
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGetTipList", data)
        } else {
            return
        }
    }

    @ReactMethod
    fun getLocation() {
        val locationClient = AMapLocationClient(reactContext)
        val locationOption = AMapLocationClientOption()
        //设置定位监听
        locationClient.setLocationListener(this)
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //设置定位间隔,单位毫秒,默认为2000ms
        locationOption.interval = 2000
        //设置定位参数
        locationClient.setLocationOption(locationOption)
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
        locationClient.startLocation()
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == 0) {
            val latLng = LatLng(amapLocation.latitude, amapLocation.longitude)
            val data = Arguments.createMap()
            data.putDouble("latitude", latLng.latitude)
            data.putDouble("longitude", latLng.longitude)
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGetLocation", data)
        } else {}
    }

    @ReactMethod
    fun convertLatlng(latitude: Double, longitude: Double, coordType: String?, promise:Promise) {
        val sourceLatLng = LatLng(latitude, longitude)
        var coord = CoordinateConverter.CoordType.GPS
        when(coordType){
            "aliyun" -> coord = CoordinateConverter.CoordType.ALIYUN
            "baidu" -> coord = CoordinateConverter.CoordType.BAIDU
            "google" -> coord = CoordinateConverter.CoordType.GOOGLE
            "mapabc" -> coord = CoordinateConverter.CoordType.MAPABC
            "mapbar" -> coord = CoordinateConverter.CoordType.MAPBAR
            "sosomap" -> coord = CoordinateConverter.CoordType.SOSOMAP
        }
        val converter = CoordinateConverter(reactContext)
        // CoordType.GPS 待转换坐标类型
        converter.from(coord)
        // sourceLatLng待转换坐标点
        converter.coord(sourceLatLng)
        // 执行转换操作
        val destLatLng = converter.convert()
        var ret = JSONObject()
        ret.put("latitude", destLatLng.latitude)
        ret.put("longitude", destLatLng.longitude)
        promise.resolve(ret.toString())
    }
}

val Float.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
