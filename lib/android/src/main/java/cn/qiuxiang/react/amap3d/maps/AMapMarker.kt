package cn.qiuxiang.react.amap3d.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import cn.qiuxiang.react.amap3d.toPx
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.amap.api.trace.LBSTraceClient
import com.amap.api.trace.TraceListener
import com.amap.api.trace.TraceLocation
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.views.view.ReactViewGroup
import java.util.*

class AMapMarker(context: Context) : ReactViewGroup(context), AMapOverlay, TraceListener {
    companion object {
        private val COLORS = mapOf(
                "AZURE" to BitmapDescriptorFactory.HUE_AZURE,
                "BLUE" to BitmapDescriptorFactory.HUE_BLUE,
                "CYAN" to BitmapDescriptorFactory.HUE_CYAN,
                "GREEN" to BitmapDescriptorFactory.HUE_GREEN,
                "MAGENTA" to BitmapDescriptorFactory.HUE_MAGENTA,
                "ORANGE" to BitmapDescriptorFactory.HUE_ORANGE,
                "RED" to BitmapDescriptorFactory.HUE_RED,
                "ROSE" to BitmapDescriptorFactory.HUE_ROSE,
                "VIOLET" to BitmapDescriptorFactory.HUE_VIOLET,
                "YELLOW" to BitmapDescriptorFactory.HUE_YELLOW
        )
    }

    private var icon: View? = null
    private var bitmapDescriptor: BitmapDescriptor? = null
    private var anchorU: Float = 0.5f
    private var anchorV: Float = 1f
    var infoWindow: AMapInfoWindow? = null
    var mTraceClient: LBSTraceClient? = null

    var marker: Marker? = null
        private set

    var imageName: String = ""
    var drawableId: Int? = null

    var position: LatLng? = null
        set(value) {
            field = value
            marker?.position = value
        }

    var zIndex: Float = 0.0f
        set(value) {
            field = value
            marker?.zIndex = value
        }

    var title = ""
        set(value) {
            field = value
            marker?.title = value
        }

    var snippet = ""
        set(value) {
            field = value
            marker?.snippet = value
        }

    var flat: Boolean = false
        set(value) {
            field = value
            marker?.isFlat = value
        }

    var opacity: Float = 1f
        set(value) {
            field = value
            marker?.alpha = value
        }

    var draggable: Boolean = false
        set(value) {
            field = value
            marker?.isDraggable = value
        }

    var clickDisabled: Boolean = false
        set(value) {
            field = value
            marker?.isClickable = !value
        }

    var infoWindowDisabled: Boolean = false
        set(value) {
            field = value
            marker?.isInfoWindowEnable = !value
        }

    var active: Boolean = false
        set(value) {
            field = value
            if (value) {
                marker?.showInfoWindow()
            } else {
                marker?.hideInfoWindow()
            }
        }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        icon = child
        icon?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateIcon() }
    }

    override fun add(map: AMap) {
        if (this.drawableId != null) {
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(this.drawableId!!)
        }
        marker = map.addMarker(MarkerOptions()
                .setFlat(flat)
                .icon(bitmapDescriptor)
                .alpha(opacity)
                .draggable(draggable)
                .position(position)
                .anchor(anchorU, anchorV)
                .infoWindowEnable(!infoWindowDisabled)
                .title(title)
                .snippet(snippet)
                .zIndex(zIndex))
        if (this.imageName == "ic_car"){
            marker!!.rotateAngle = Random().nextFloat() * 360
        }
        if (this.drawableId != null) {
            this.randomTrace()
        }
        this.clickDisabled = clickDisabled
        this.active = active
    }

    override fun remove() {
        marker?.destroy()
    }

    fun setIconColor(icon: String) {
        bitmapDescriptor = COLORS[icon.toUpperCase()]?.let {
            BitmapDescriptorFactory.defaultMarker(it)
        }
        marker?.setIcon(bitmapDescriptor)
    }

    fun updateIcon() {
        icon?.let {
            if (it.width != 0 && it.height != 0) {
                val bitmap = Bitmap.createBitmap(
                        it.width, it.height, Bitmap.Config.ARGB_8888)
                it.draw(Canvas(bitmap))
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                marker?.setIcon(bitmapDescriptor)
            }
        }
    }

    fun setImage(name: String) {
        this.imageName = name
        val drawable = context.resources.getIdentifier(name, "drawable", context.packageName)
        this.drawableId = drawable
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(drawable)
        marker?.setIcon(bitmapDescriptor)
    }

    fun setAnchor(x: Double, y: Double) {
        anchorU = x.toFloat()
        anchorV = y.toFloat()
        marker?.setAnchor(anchorU, anchorV)
    }

    fun lockToScreen(args: ReadableArray?) {
        if (args != null) {
            val x = args.getDouble(0).toFloat().toPx
            val y = args.getDouble(1).toFloat().toPx
            marker?.setPositionByPixels(x, y)
        }
    }

    fun moveTo(args: ReadableArray?) {
        if (args != null) {
            val latitude = args.getDouble(0)
            val longitude = args.getDouble(1)
            val point = LatLng(latitude, longitude)
            marker?.position = point
        }
    }

    /**
     * 通过轨迹纠偏随机移动
     */
    fun randomTrace() {
        // step1: 定义当前坐标点和随机移动到的坐标点
        var traceList: ArrayList<TraceLocation> = ArrayList()
        // 初始点
        var startLocation: TraceLocation = TraceLocation()
        startLocation.latitude = this.position!!.latitude   // 当前坐标点纬度
        startLocation.longitude = this.position!!.longitude   // 当前经度
        startLocation.speed = Random().nextFloat() * 5    // 随机速度, 5km/h内
        startLocation.bearing = Random().nextFloat() * 360  // 随机角度
        startLocation.time = System.currentTimeMillis() // 当前时间
        traceList.add(startLocation)
        // 结束点
        var endLocation: TraceLocation = TraceLocation()
        endLocation.latitude = this.position!!.latitude + 0.0002   // 移动后纬度
        endLocation.longitude = this.position!!.longitude + 0.0002   // 移动后经度
        endLocation.speed = Random().nextFloat() * 5    // 随机速度, 5km/h内
        endLocation.bearing = Random().nextFloat() * 360  // 随机角度
        endLocation.time = System.currentTimeMillis() + 1000 // 当前时间后1秒钟
        traceList.add(endLocation)


        // {"systime":"2016-08-03 16:22:24","lon":116.47918011697402,"loctime":1470212545000,"address":"","speed":0,"bearing":0.2504628896713257,"provider":"gps","accuracy":24,"lat":39.99821635754188}
        mTraceClient = LBSTraceClient.getInstance(context)
        var sequenceLineId = Random().nextInt(999) + 1
        mTraceClient!!.queryProcessedTrace(sequenceLineId, traceList, LBSTraceClient.TYPE_AMAP, this)
    }

    /**
     * 轨迹纠偏失败回调事件
     * lineID：用于标示一条轨迹，支持多轨迹纠偏，如果多条轨迹调起纠偏接口，则lineID需不同。
     * errorInfo：轨迹纠偏失败原因。
     */
    override fun onRequestFailed(lineID: Int, errorInfo: String?) {
        //      Log.d("AmapTraceDebug", errorInfo.toString())
    }
    /**
     * 轨迹纠偏进行中回调事件
     * lineID：用于标示一条轨迹，支持多轨迹纠偏，如果多条轨迹调起纠偏接口，则lineID需不同。
     * index：一条轨迹分割为多个段,标示当前轨迹段索引。
     * segments：一条轨迹分割为多个段，segments标示当前轨迹段经过纠偏后经纬度点集合。
     */
    override fun onTraceProcessing(lineID: Int, index: Int, segments: MutableList<LatLng>?) {
        //      Log.d("AmapTraceDebug", segments.toString())
    }

    /**
     * 轨迹纠偏完成回调事件
     * lineID：用于标示一条轨迹，支持多轨迹纠偏，如果多条轨迹调起纠偏接口，则lineID需不同。
     * linepoints：整条轨迹经过纠偏后点的经纬度集合。
     * distance：轨迹经过纠偏后总距离，单位米。
     * waitingtime：该轨迹中间停止时间，以GPS速度为参考，单位秒。
     */
    override fun onFinished(lineID: Int, linepoints: MutableList<LatLng>?, distance: Int, waitingtime: Int) {
        //      Log.d("AmapTraceDebug", linepoints.toString())
        marker!!.position = linepoints!![1]
        marker!!.rotateAngle = Random().nextFloat() * 360
    }

}
