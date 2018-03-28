// @flow
import PropTypes from 'prop-types'
import { ViewPropTypes } from 'react-native'
import { LatLng } from '../PropTypes'
import Component from '../Component'

export default class Base extends Component<any> {
  static propTypes = {
    ...ViewPropTypes,

    /**
     * 路径规划成功事件
     */
    onCalculateRouteSuccess: PropTypes.func,

    /**
     * 路径规划失败事件
     */
    onCalculateRouteFailure: PropTypes.func,

    /**
     * 开始导航事件
     */
    onStartNavi: PropTypes.func,

    /**
     * 播报类型和播报文字回调
     */
    onGetNavigationText: PropTypes.func,

    /**
     * 从新规划路径事件
     */
    onReCalculateRouteForYaw: PropTypes.func,

    /**
     * 到达目的地事件
     */
    onArriveDestination: PropTypes.func,
  }

  /**
   * 路线规划
   */
  calculateRoute(start: LatLng, end: LatLng, way: LatLng[] = []) {
    this.sendCommand('calculateRoute', [start, end, way])
  }

  /**
   * 开始导航
   */
  start() {
    this.sendCommand('start')
  }

  /**
   * 终止导航
   */
  stop() {
    this.sendCommand('stop')
  }
}
