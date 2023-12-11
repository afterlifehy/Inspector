package ja.insepector.bxapp.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.AppUtils
import com.hyperai.hyperlpr3.HyperLPR3
import com.hyperai.hyperlpr3.bean.HyperLPRParameter
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.bean.Street
import ja.insepector.base.dialog.DialogHelp
import ja.insepector.base.ext.i18N
import ja.insepector.base.help.ActivityCacheManager
import ja.insepector.base.util.ToastUtil
import ja.insepector.base.viewbase.VbBaseActivity
import ja.insepector.common.realm.RealmUtil
import ja.insepector.common.util.AppUtil
import ja.insepector.common.util.BluePrint
import ja.insepector.bxapp.R
import ja.insepector.bxapp.databinding.ActivityMainBinding
import ja.insepector.bxapp.mvvm.viewmodel.MainViewModel
import ja.insepector.bxapp.pop.StreetPop
import com.tbruyelle.rxpermissions3.RxPermissions
import ja.insepector.base.bean.BlueToothDeviceBean
import ja.insepector.base.ext.startAct
import ja.insepector.bxapp.ui.activity.mine.LogoutActivity
import ja.insepector.bxapp.ui.activity.parking.ParkingLotActivity
import ja.insepector.bxapp.util.UpdateUtil

@Route(path = ARouterMap.MAIN)
class MainActivity : VbBaseActivity<MainViewModel, ActivityMainBinding>(), OnClickListener {
    var streetPop: StreetPop? = null
    var streetList: MutableList<Street> = ArrayList()
    var currentStreet: Street? = null

    override fun onSaveInstanceState(outState: Bundle) {
        // super.onSaveInstanceState(outState)
    }

    override fun initView() {
        initHyperLPR()
//        setStatusBarColor(ja.insepector.base.R.color.black, false)
    }

    override fun initListener() {
        binding.ivHead.setOnClickListener(this)
        binding.tvTitle.setOnClickListener(this)
        binding.llParkingLot.setOnClickListener(this)
        binding.flIncomeCounting.setOnClickListener(this)
        binding.flOrder.setOnClickListener(this)
        binding.flBerthAbnormal.setOnClickListener(this)
        binding.flLogout.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        streetList = RealmUtil.instance?.findCheckedStreetList() as MutableList<Street>
        currentStreet = RealmUtil.instance?.findCurrentStreet()
        connectBluePrint()

        if (currentStreet!!.streetName.indexOf("(") < 0) {
            binding.tvTitle.text = currentStreet!!.streetNo + currentStreet!!.streetName
        } else {
            binding.tvTitle.text =
                currentStreet!!.streetNo + currentStreet!!.streetName.substring(0, currentStreet!!.streetName.indexOf("("))
        }
    }

    @SuppressLint("CheckResult", "MissingPermission")
    fun connectBluePrint() {
        BluePrint.instance?.disConnect()
        if (RealmUtil.instance?.findCurrentDeviceList()!!.isNotEmpty()) {
            Thread {
                val device = RealmUtil.instance?.findCurrentDeviceList()!![0]
                if (device != null) {
                    val printResult = BluePrint.instance?.connet(device.address)
                    if (printResult != 0) {
                        runOnUiThread {
                            DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.打印机连接失败需要手动连接))
                                .setLeftMsg(i18N(ja.insepector.base.R.string.取消))
                                .setRightMsg(i18N(ja.insepector.base.R.string.去连接)).setCancelable(true)
                                .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                                    override fun onLeftClickLinsener(msg: String) {
                                    }

                                    override fun onRightClickLinsener(msg: String) {
//                                        val intent = Intent(this@MainActivity, MineActivity::class.java)
//                                        intent.putExtra(ARouterMap.MINE_BLUE_PRINT, 1)
//                                        startActivity(intent)
                                    }

                                }).build(this@MainActivity).showDailog()
                        }
                    }
                }
            }.start()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                var rxPermissions = RxPermissions(this@MainActivity)
                rxPermissions.request(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN).subscribe {
                    if (it) {
                        Thread {
                            BluePrint.instance?.disConnect()
                            val printList = BluePrint.instance?.blueToothDevice!!
                            if (printList.size == 1) {
                                val device = printList[0]
                                var connectResult = BluePrint.instance?.connet(device.address)
                                if (connectResult == 0) {
                                    RealmUtil.instance?.deleteAllDevice()
                                    RealmUtil.instance?.addRealm(BlueToothDeviceBean(device.address, device.name))
                                }
                            } else if (printList.size > 1) {
                                multipleDevice()
                            } else {
                                DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.未检测到已配对的打印设备))
                                    .setLeftMsg(i18N(ja.insepector.base.R.string.取消))
                                    .setRightMsg(i18N(ja.insepector.base.R.string.去配对)).setCancelable(true)
                                    .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                                        override fun onLeftClickLinsener(msg: String) {
                                        }

                                        override fun onRightClickLinsener(msg: String) {
                                            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                            startActivity(intent)
                                        }

                                    }).build(this@MainActivity).showDailog()
                            }
                        }.start()
                    }
                }
            } else {
                val printList = BluePrint.instance?.blueToothDevice!!
                if (printList.size == 1) {
                    val device = printList[0]
                    Thread {
                        var connectResult = BluePrint.instance?.connet(device.address)
                        if (connectResult == 0) {
                            RealmUtil.instance?.deleteAllDevice()
                            RealmUtil.instance?.addRealm(BlueToothDeviceBean(device.address, device.name))
                        } else {
                            DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.未检测到已配对的打印设备))
                                .setLeftMsg(i18N(ja.insepector.base.R.string.取消))
                                .setRightMsg(i18N(ja.insepector.base.R.string.去配对)).setCancelable(true)
                                .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                                    override fun onLeftClickLinsener(msg: String) {
                                    }

                                    override fun onRightClickLinsener(msg: String) {
                                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                        startActivity(intent)
                                    }

                                }).build(this@MainActivity).showDailog()
                        }
                    }.start()
                } else if (printList.size > 1) {
                    multipleDevice()
                }
            }
        }
    }

    fun multipleDevice() {
        DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.检测到存在多台打印设备需手动连接))
            .setLeftMsg(i18N(ja.insepector.base.R.string.取消))
            .setRightMsg(i18N(ja.insepector.base.R.string.去连接)).setCancelable(true)
            .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                override fun onLeftClickLinsener(msg: String) {
                }

                override fun onRightClickLinsener(msg: String) {
//                    val intent = Intent(this@MainActivity, MineActivity::class.java)
//                    intent.putExtra(ARouterMap.MINE_BLUE_PRINT, 1)
//                    startActivity(intent)
                }

            }).build(this@MainActivity).showDailog()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_head -> {
//                val intent = Intent(this@MainActivity, MineActivity::class.java)
//                intent.putExtra(ARouterMap.MINE_BLUE_PRINT, 0)
//                startActivity(intent)
            }

            R.id.tv_title -> {
                currentStreet = RealmUtil.instance?.findCurrentStreet()
                streetPop = StreetPop(this@MainActivity, currentStreet, streetList, object : StreetPop.StreetSelectCallBack {
                    override fun selectStreet(street: Street) {
                        val old = RealmUtil.instance?.findCurrentStreet()
                        RealmUtil.instance?.updateCurrentStreet(street, old)
                        if (street.streetName.indexOf("(") < 0) {
                            binding.tvTitle.text = street.streetNo + street.streetName
                        } else {
                            binding.tvTitle.text =
                                street.streetNo + street.streetName.substring(0, street.streetName.indexOf("("))
                        }
                    }
                })
                streetPop?.showAsDropDown((v.parent) as RelativeLayout)
                val upDrawable = ContextCompat.getDrawable(BaseApplication.instance(), ja.insepector.common.R.mipmap.ic_arrow_up)
                upDrawable?.setBounds(0, 0, upDrawable.intrinsicWidth, upDrawable.intrinsicHeight)
                binding.tvTitle.setCompoundDrawables(
                    null,
                    null,
                    upDrawable,
                    null
                )
                streetPop?.setOnDismissListener(object : PopupWindow.OnDismissListener {
                    override fun onDismiss() {
                        val downDrawable =
                            ContextCompat.getDrawable(BaseApplication.instance(), ja.insepector.common.R.mipmap.ic_arrow_down)
                        downDrawable?.setBounds(0, 0, downDrawable.intrinsicWidth, downDrawable.intrinsicHeight)
                        binding.tvTitle.setCompoundDrawables(
                            null,
                            null,
                            downDrawable,
                            null
                        )
                    }
                })
            }

            R.id.ll_parkingLot -> {
                startAct<ParkingLotActivity>()
            }

            R.id.fl_incomeCounting -> {
//                val intent = Intent(this@MainActivity, IncomeCountingActivity::class.java)
//                startActivity(intent)
            }

            R.id.fl_order -> {
//                val intent = Intent(this@MainActivity, OrderMainActivity::class.java)
//                startActivity(intent)
            }

            R.id.fl_berthAbnormal -> {
//                val intent = Intent(this@MainActivity, BerthAbnormalActivity::class.java)
//                startActivity(intent)
            }

            R.id.fl_logout -> {
                startAct<LogoutActivity>()
            }
        }
    }

    private fun initHyperLPR() {
        // 车牌识别算法配置参数
        val parameter = HyperLPRParameter()
            .setDetLevel(HyperLPR3.DETECT_LEVEL_LOW)
            .setMaxNum(1)
            .setRecConfidenceThreshold(0.85f)
        // 初始化(仅执行一次生效)
        HyperLPR3.getInstance().init(BaseApplication.instance(), parameter)
    }

    @SuppressLint("NewApi")
    override fun startObserve() {
        super.startObserve()
        mViewModel.apply {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    fun requestPermissions() {
        var rxPermissions = RxPermissions(this@MainActivity)
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
            if (it) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (packageManager.canRequestPackageInstalls()) {
                        UpdateUtil.instance?.downloadFileAndInstall()
                    } else {
                        val uri = Uri.parse("package:${AppUtils.getAppPackageName()}")
                        val intent =
                            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                        requestInstallPackageLauncher.launch(intent)
                    }
                } else {
                    UpdateUtil.instance?.downloadFileAndInstall()
                }
            } else {

            }
        }
    }

    val requestInstallPackageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            UpdateUtil.instance?.downloadFileAndInstall()
        } else {

        }
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {

    }

    override fun providerVMClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override val isFullScreen: Boolean
        get() = false

    override fun onBackPressedSupport() {
        if (AppUtil.isFastClick(1000)) {
            ActivityCacheManager.instance().getAllActivity().forEach {
                if (!it.isFinishing) {
                    it.finish()
                }
            }
        } else {
            ToastUtil.showMiddleToast(i18N(ja.insepector.base.R.string.再按一次退出程序))
        }
    }
}