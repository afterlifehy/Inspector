package ja.insepector.bxapp.ui.activity.income

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.bean.IncomeCountingBean
import ja.insepector.base.ds.PreferencesDataStore
import ja.insepector.base.ds.PreferencesKeys
import ja.insepector.base.ext.i18N
import ja.insepector.base.ext.i18n
import ja.insepector.base.util.ToastUtil
import ja.insepector.base.viewbase.VbBaseActivity
import ja.insepector.common.realm.RealmUtil
import ja.insepector.common.util.BluePrint
import ja.insepector.bxapp.R
import ja.insepector.bxapp.databinding.ActivityIncomeCountingBinding
import ja.insepector.bxapp.mvvm.viewmodel.IncomeCountingViewModel
import ja.insepector.bxapp.pop.DatePop
import com.tbruyelle.rxpermissions3.RxPermissions
import com.zrq.spanbuilder.TextStyle
import ja.insepector.base.ext.show
import ja.insepector.bxapp.dialog.PromptDialog
import ja.insepector.common.util.AppUtil
import ja.insepector.common.util.GlideUtils
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

@Route(path = ARouterMap.INCOME_COUNTING)
class IncomeCountingActivity : VbBaseActivity<IncomeCountingViewModel, ActivityIncomeCountingBinding>(), OnClickListener {
    var datePop: DatePop? = null
    var startDate = ""
    var endDate = ""
    var incomeCountingBean: IncomeCountingBean? = null
    var loginName = ""
    var searchRange = "0"
    var promptDialog: PromptDialog? = null
    var colors = intArrayOf(ja.insepector.base.R.color.color_ff04a091, ja.insepector.base.R.color.color_ff04a091)
    var sizes = intArrayOf(27, 19)
    var styles = arrayOf(TextStyle.BOLD, TextStyle.NORMAL)
    var colors2 = intArrayOf(ja.insepector.base.R.color.color_ff282828, ja.insepector.base.R.color.color_ff282828)
    var sizes2 = intArrayOf(23, 19)

    override fun initView() {
        binding.layoutToolbar.tvTitle.text = i18N(ja.insepector.base.R.string.营收盘点)
        GlideUtils.instance?.loadImage(binding.layoutToolbar.ivRight, ja.insepector.common.R.mipmap.ic_calendar)
        binding.layoutToolbar.ivRight.show()
    }

    override fun initListener() {
        binding.layoutToolbar.flBack.setOnClickListener(this)
        binding.layoutToolbar.ivRight.setOnClickListener(this)
        binding.tvTotalIncomeTitle.setOnClickListener(this)
        binding.rtvPrint.setOnClickListener(this)
    }

    override fun initData() {
        runBlocking {
            loginName = PreferencesDataStore(BaseApplication.instance()).getString(PreferencesKeys.loginName)
        }
        endDate = TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd")
        startDate = endDate.substring(0, 8) + "01"
        getIncomeCounting()
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fl_back -> {
                onBackPressedSupport()
            }

            R.id.iv_right -> {
                if (datePop == null) {
                    datePop = DatePop(BaseApplication.instance(), startDate, endDate, 1, object : DatePop.DateCallBack {
                        override fun selectDate(startTime: String, endTime: String) {
                            startDate = startTime
                            endDate = endTime
                            val difference = TimeUtils.getTimeSpan(endTime, startTime, SimpleDateFormat("yyyy-MM-dd"), TimeConstants.DAY)
                            if (difference > 90) {
                                ToastUtil.showMiddleToast(i18N(ja.insepector.base.R.string.查询时间间隔不得超过90天))
                                return
                            }
                            binding.rtvDateRange.text = "统计时间：${startDate}~${endDate}"
                            getIncomeCounting()
                        }

                    })
                }
                datePop?.showAsDropDown((v.parent) as Toolbar)
            }

            R.id.tv_totalIncomeTitle -> {
                promptDialog = PromptDialog(
                    i18N(ja.insepector.base.R.string.今日总收费和本月总收入均包含自主缴费金额和被追缴金额且不包含追缴他人金额),
                    "",
                    i18N(ja.insepector.base.R.string.确定),
                    object : PromptDialog.PromptCallBack {
                        override fun leftClick() {

                        }

                        override fun rightClick() {

                        }

                    }, true
                )
                promptDialog?.show()
            }

            R.id.rtv_print -> {
                incomeCountingBean?.loginName = loginName
                if (searchRange == "1") {
                    incomeCountingBean?.range = startDate + "~" + endDate
                }
                var str = "receipt,"
                var rxPermissions = RxPermissions(this@IncomeCountingActivity)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    rxPermissions.request(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN).subscribe {
                        if (it) {
                            ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.开始打印))
                            Thread {
                                BluePrint.instance?.zkblueprint(str + JSONObject.toJSONString(incomeCountingBean))
                            }.start()
                        }
                    }
                } else {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.开始打印))
                    Thread {
                        BluePrint.instance?.zkblueprint(str + JSONObject.toJSONString(incomeCountingBean))
                    }.start()
                }
            }
        }
    }

    fun getIncomeCounting() {
        showProgressDialog(20000)
        val param = HashMap<String, Any>()
        val jsonobject = JSONObject()
        jsonobject["startDate"] = startDate
        jsonobject["endDate"] = endDate
        jsonobject["loginName"] = loginName
        jsonobject["searchRange"] = searchRange
        param["attr"] = jsonobject
        mViewModel.incomeCounting(param)
    }

    override fun startObserve() {
        mViewModel.apply {
            incomeCountingLiveData.observe(this@IncomeCountingActivity) {
                dismissProgressDialog()
                incomeCountingBean = it
                if (incomeCountingBean?.list1 != null && incomeCountingBean?.list1!!.isNotEmpty()) {
                    val todayIncomeBean = incomeCountingBean?.list1!![0]
                    val strings = arrayOf(todayIncomeBean.payMoney, "元")
                    binding.tvTotalIncome.text = AppUtil.getSpan(strings, sizes, colors, styles)
                    val strings1 = arrayOf(todayIncomeBean.orderCount.toString(), "笔")
                    binding.tvOrderPlacedNum.text = AppUtil.getSpan(strings1, sizes2, colors2)
                    val strings2 = arrayOf(todayIncomeBean.refusePayCount.toString(), "笔")
                    binding.tvRefusePayNum.text = AppUtil.getSpan(strings2, sizes2, colors2)
                    val strings3 = arrayOf(todayIncomeBean.partPayCount.toString(), "笔")
                    binding.tvPartPaidNum.text = AppUtil.getSpan(strings3, sizes2, colors2)
                    val strings4 = arrayOf(todayIncomeBean.oweCount.toString(), "笔")
                    binding.tvRecoverNum.text = AppUtil.getSpan(strings4, sizes2, colors2)
                    val strings5 = arrayOf(todayIncomeBean.passMoney, "元")
                    binding.tvRecoverAmount.text = AppUtil.getSpan(strings5, sizes2, colors2)
                    val strings6 = arrayOf(todayIncomeBean.oweMoney, "元")
                    binding.tvChasingOthersAmount.text = AppUtil.getSpan(strings6, sizes2, colors2)
                    val strings7 = arrayOf(todayIncomeBean.onlineMoney, "元")
                    binding.tvPayIndependentlyAmount.text = AppUtil.getSpan(strings7, sizes2, colors2)

                }
                if (searchRange == "1") {
                    binding.rllMonth.show()
                    if (incomeCountingBean?.list1 != null && incomeCountingBean?.list1!!.isNotEmpty()) {
                        val rangeIncomeBean = incomeCountingBean?.list2!![0]
                        val strings8 = arrayOf(rangeIncomeBean.payMoney, "元")
                        binding.tvMonthTotalIncome.text = AppUtil.getSpan(strings8, sizes2, colors2)
                        val strings9 = arrayOf(rangeIncomeBean.orderCount.toString(), "笔")
                        binding.tvMonthOrderPlacedNum.text = AppUtil.getSpan(strings9, sizes2, colors2)
                    }
                }
                searchRange = "1"
            }
            errMsg.observe(this@IncomeCountingActivity) {
                dismissProgressDialog()
                ToastUtil.showMiddleToast(it.msg)
            }
        }
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityIncomeCountingBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override val isFullScreen: Boolean
        get() = true

    override fun marginStatusBarView(): View {
        return binding.layoutToolbar.ablToolbar
    }

    override fun providerVMClass(): Class<IncomeCountingViewModel> {
        return IncomeCountingViewModel::class.java
    }
}