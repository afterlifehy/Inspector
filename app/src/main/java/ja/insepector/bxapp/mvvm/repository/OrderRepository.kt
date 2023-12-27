package ja.insepector.bxapp.mvvm.repository

import ja.insepector.base.base.mvvm.BaseRepository
import ja.insepector.base.bean.DebtCollectionResultBean
import ja.insepector.base.bean.DebtUploadBean
import ja.insepector.base.bean.EndOrderInfoBean
import ja.insepector.base.bean.HttpWrapper
import ja.insepector.base.bean.OrderResultBean
import ja.insepector.base.bean.PayResultBean
import ja.insepector.base.bean.PicInquiryBean
import ja.insepector.base.bean.PayQRBean
import ja.insepector.base.bean.TicketPrintBean
import ja.insepector.base.bean.TicketPrintResultBean
import ja.insepector.base.bean.TransactionResultBean

class OrderRepository : BaseRepository() {

    /**
     * 订单查询
     */
    suspend fun orderInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<OrderResultBean> {
        return mServer.orderInquiry(param)
    }

    /**
     * 交易查询
     */
    suspend fun transactionInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<TransactionResultBean> {
        return mServer.transactionInquiry(param)
    }

    /**
     * 欠费查询
     */
    suspend fun debtInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<DebtCollectionResultBean> {
        return mServer.debtInquiry(param)
    }

    /**
     * 图片查询
     */
    suspend fun picInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PicInquiryBean> {
        return mServer.picInquiry(param)
    }

    /**
     * 预支付查询
     */
    suspend fun prePayFeeInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PayQRBean> {
        return mServer.prePayFeeInquiry(param)
    }

    /**
     * 离场订单查询
     */
    suspend fun endOrderInfo(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<EndOrderInfoBean> {
        return mServer.endOrderInfo(param)
    }

    /**
     * 欠费上传
     */
    suspend fun debtUpload(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<DebtUploadBean> {
        return mServer.debtUpload(param)
    }

    /**
     * 票据打印
     */
    suspend fun ticketPrint(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<TicketPrintBean> {
        return mServer.ticketPrint(param)
    }

    /**
     * 查询支付结果
     */
    suspend fun payResultInquiry(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PayResultBean> {
        return mServer.payResultInquiry(param)
    }

    /**
     *  根据订单查交易
     */
    suspend fun inquiryTransactionByOrderNo(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<TicketPrintResultBean> {
        return mServer.inquiryTransactionByOrderNo(param)
    }

    /**
     *  离场支付二维码
     */
    suspend fun endOrderQR(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PayQRBean> {
        return mServer.endOrderQR(param)
    }

    /**
     *  追缴二维码
     */
    suspend fun debtPayQr(param: @JvmSuppressWildcards Map<String, Any?>): HttpWrapper<PayQRBean> {
        return mServer.debtPayQr(param)
    }
}