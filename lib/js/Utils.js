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
  distance: (
    lat1: number,
    lng1: number,
    lat2: number,
    lng2: number,
  ): Promise<number> => AMapUtils.distance(lat1, lng1, lat2, lng2),
  getPoiListByKeyword: (key: string, city: string) => AMapUtils.getPoiListByKeyword(key, city),
  addGetTipListListener: (callback: Callback): EmitterSubscription => eventEmitter.addListener('onGetTipList', callback),
}
