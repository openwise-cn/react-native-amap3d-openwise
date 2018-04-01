// @flow
import { NativeModules, NativeEventEmitter } from 'react-native'
import type EmitterSubscription from 'react-native/Libraries/vendor/emitter/EmitterSubscription'

const { AMapUtils } = NativeModules
const eventEmitter = new NativeEventEmitter(AMapUtils)

export type Callback = (state: {
  tipList: string,
}) => void

export default {
  /**
   * 计算两点距离
   */
  distance: (lat1: number, lng1: number, lat2: number, lng2: number): Promise<number> => AMapUtils.distance(lat1, lng1, lat2, lng2),
  getPoiListByKeyword: (key: string, city: string) => AMapUtils.getPoiListByKeyword(key, city),
  addGetTipListListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onGetTipList', callback),
  getLocation: () => AMapUtils.getLocation(),
  getLocationOnce: () => AMapUtils.getLocationOnce(),
  addGetLocationListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onGetLocation', callback),
  searchRouteResult: (startLatitude: number, startLongitude: number, endLatitude: number, endLongitude: number) => AMapUtils.searchRouteResult(startLatitude, startLongitude, endLatitude, endLongitude),
  getLatlon: (name: string, city: string) => AMapUtils.getLatlon(name, city),
  addGeocodeSearchedListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onGeocodeSearched', callback),
  getAddress: (latitude: number, longitude: number) => AMapUtils.getAddress(latitude, longitude),
  addRegeocodeSearchedListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onRegeocodeSearched', callback),
  addDriveRouteSearchedListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onDriveRouteSearched', callback),
  convertLatlng: (latitude: number, longitude: number, coordType: String) : Promise<String> => AMapUtils.convertLatlng(latitude, longitude, coordType),
}
