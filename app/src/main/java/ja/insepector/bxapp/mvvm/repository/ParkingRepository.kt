package ja.insepector.bxapp.mvvm.repository

import ja.insepector.base.base.mvvm.BaseRepository
import ja.insepector.base.bean.HttpWrapper
import ja.insepector.base.bean.ParkingLotResultBean
import ja.insepector.base.bean.ParkingSpaceBean
import ja.insepector.base.bean.ParkingSpaceResultBean

//import ja.insepector.base.bean.ParkingLotResultBean
//import ja.insepector.base.bean.ParkingSpaceBean
//import ja.insepector.base.bean.PayResultBean
//import ja.insepector.base.bean.QRPayBean

class ParkingRepository : BaseRepository() {

    /**
     * 停车场泊位列表
     */
    suspend fun getParkingLotList(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<ParkingLotResultBean> {
        return mServer.getParkingLotList(param)
    }

    /**
     * 场内停车费查询
     */
    suspend fun parkingSpace(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<ParkingSpaceResultBean> {
        return mServer.parkingSpace(param)
    }

    /**
     * 下单
     */
    suspend fun placeOrder(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<Any> {
        return mServer.placeOrder(param)
    }

//    /**
//     *  场内支付
//     */
//    suspend fun insidePay(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<QRPayBean> {
//        return mServer.insidePay(param)
//    }
//
//    /**
//     *  支付结果
//     */
//    suspend fun payResult(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PayResultBean> {
//        return mServer.payResult(param)
//    }
}