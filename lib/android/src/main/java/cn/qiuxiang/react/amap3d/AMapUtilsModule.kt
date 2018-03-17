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
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.*
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.route.*
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
class AMapUtilsModule(private val reactContext: ReactApplicationContext) :
        ReactContextBaseJavaModule(reactContext),
        Inputtips.InputtipsListener, AMapLocationListener,
        RouteSearch.OnRouteSearchListener,
        GeocodeSearch.OnGeocodeSearchListener {
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

    /**
     * 开始搜索路径规划方案
     */
    @ReactMethod
    fun searchRouteResult(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double) {
        val startLatLng = LatLonPoint(startLatitude, startLongitude)
        val endLatLng = LatLonPoint(endLatitude, endLongitude)
        val fromAndTo = RouteSearch.FromAndTo(startLatLng, endLatLng)
        val routeSearch = RouteSearch(reactContext)
        routeSearch.setRouteSearchListener(this)
        val query = RouteSearch.DriveRouteQuery(fromAndTo, 0, null, null, "")// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
        routeSearch.calculateDriveRouteAsyn(query)// 异步路径规划驾车模式查询
    }

    override fun onDriveRouteSearched(result: DriveRouteResult?, errorCode: Int) {
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.paths != null && result.paths.size > 0) {
                val path = result.paths.get(0)
                val data = Arguments.createMap()
                data.putInt("distance", path.distance.toInt())
                data.putInt("duration", path.duration.toInt())
                data.putInt("cost", result.taxiCost.toInt())
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onDriveRouteSearched", data)
            }
        } else {}
    }

    override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onWalkRouteSearched(p0: WalkRouteResult?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ReactMethod
    fun getLatlon(args: ReadableArray?) {
        if (args == null) {
            return
        } else {
            val geocoderSearch = GeocodeSearch(reactContext)
            geocoderSearch.setOnGeocodeSearchListener(this)
            val name = args.getString(0)
            val city = args.getString(1)
            val query = GeocodeQuery(name, city)// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
            geocoderSearch.getFromLocationNameAsyn(query)// 设置同步地理编码请求
        }
    }

    @ReactMethod
    fun getAddress(args: ReadableArray?) {
        if (args == null) {
            return
        } else {
            val geocoderSearch = GeocodeSearch(reactContext)
            geocoderSearch.setOnGeocodeSearchListener(this)
            val latLonPoint = LatLonPoint(args!!.getDouble(0), args.getDouble(1))
            val query = RegeocodeQuery(
                    latLonPoint,
                    200f,
                    GeocodeSearch.AMAP)// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            geocoderSearch.getFromLocationAsyn(query)// 设置异步逆地理编码请求
        }
    }

    override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size > 0) {
                val address = result.getGeocodeAddressList().get(0)
                val event = Arguments.createMap()
                event.putDouble("latitude", address.latLonPoint.latitude)
                event.putDouble("longitude", address.latLonPoint.longitude)
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGeocodeSearched", event)
            } else {
                val event = Arguments.createMap()
                event.putDouble("latitude", 0.0)
                event.putDouble("longitude", 0.0)
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGeocodeSearched", event)
            }
        } else {
            val event = Arguments.createMap()
            event.putDouble("latitude", 0.0)
            event.putDouble("longitude", 0.0)
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onGeocodeSearched", event)
        }
    }

    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.regeocodeAddress != null) {
                val address = result.regeocodeAddress
                val event = Arguments.createMap()
                event.putString("aoiName", address.aois.get(0).aoiName)
                event.putString("poiName", address.pois.get(0).toString())
                event.putString("city", address.city)
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onRegeocodeSearched", event)
            } else {
                val event = Arguments.createMap()
                event.putString("aoiName", "未知坐标位置")
                event.putString("poiName", "未知坐标位置")
                event.putString("city", "未知城市")
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onRegeocodeSearched", event)
            }
        } else {
            val event = Arguments.createMap()
            event.putString("aoiName", "未知坐标位置")
            event.putString("poiName", "未知坐标位置")
            event.putString("city", "未知城市")
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit("onRegeocodeSearched", event)
        }
    }
}

val Float.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
