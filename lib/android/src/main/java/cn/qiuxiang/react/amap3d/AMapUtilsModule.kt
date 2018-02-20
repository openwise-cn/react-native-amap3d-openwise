package cn.qiuxiang.react.amap3d

import android.content.res.Resources
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONArray

@Suppress("unused")
class AMapUtilsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), Inputtips.InputtipsListener {
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
                listJson.put(tipList.get(i).getName())
            }
            val data = Arguments.createMap()
            data.putString("tipList", listJson.toString())
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGetTipList", data)
        } else {
            return
        }
    }
}

val Float.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
