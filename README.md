# react-native-amap3d-openwise [![npm version][version-badge]][npm]

[npm]: https://www.npmjs.com/package/react-native-amap3d-openwise
[version-badge]: https://badge.fury.io/js/react-native-amap3d-openwise.svg

在 [react-native-amap3d](https://github.com/qiuxiang/react-native-amap3d) 项目的基础上进行功能扩展

## onLocation 回调增加speed, 当前地址的数据
1. 修改 lib/android/src/main/java/cn/qiuxiang/react/amap3d/maps/AMapView.kt 文件, 在onLocation的emit中增加event地址信息.
