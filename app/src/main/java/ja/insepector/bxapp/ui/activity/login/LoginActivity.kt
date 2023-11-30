package ja.insepector.bxapp.ui.activity.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PathUtils
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.dialog.DialogHelp
import ja.insepector.base.ds.PreferencesDataStore
import ja.insepector.base.ds.PreferencesKeys
import ja.insepector.base.ext.i18N
import ja.insepector.base.util.ToastUtil
import ja.insepector.base.viewbase.VbBaseActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import ja.insepector.base.bean.UpdateBean
import ja.insepector.base.ext.startAct
import ja.insepector.bxapp.R
import ja.insepector.bxapp.databinding.ActivityLoginBinding
import ja.insepector.bxapp.mvvm.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : VbBaseActivity<LoginViewModel, ActivityLoginBinding>(), OnClickListener {
    var locationManager: LocationManager? = null
    var lat = 121.123212
    var lon = 31.434312
    var updateBean: UpdateBean? = null
    var locationEnable = false

    @SuppressLint("CheckResult", "MissingPermission")
    override fun initView() {
        var rxPermissions = RxPermissions(this@LoginActivity)
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe {
            if (it) {
                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val provider = LocationManager.NETWORK_PROVIDER
                locationManager?.requestLocationUpdates(provider, 1000, 1f, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        lat = location.latitude
                        lon = location.longitude
                        locationEnable = true
                    }

                    override fun onProviderDisabled(provider: String) {
                        locationEnable = false
                        ToastUtil.showToast(i18N(ja.insepector.base.R.string.请打开位置信息))
                    }

                    override fun onProviderEnabled(provider: String) {
                        locationEnable = true
                    }
                })
            }
        }
    }

    override fun initListener() {
        binding.tvForgetPw.setOnClickListener(this)
        binding.rtvLogin.setOnClickListener(this)
        binding.etAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.etPw.text.isNotEmpty() && p0!!.isNotEmpty()) {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_ff04a091
                        )
                    )
                } else {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_9904a091
                        )
                    )
                }
                binding.rtvLogin.delegate.init()
            }

        })
        binding.etPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.etAccount.text.isNotEmpty() && p0!!.isNotEmpty()) {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_ff04a091
                        )
                    )
                } else {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_9904a091
                        )
                    )
                }
                binding.rtvLogin.delegate.init()
            }

        })
    }

    override fun initData() {
        val param = HashMap<String, Any>()
        val jsonobject = JSONObject()
        jsonobject["version"] = AppUtils.getAppVersionCode()
        param["attr"] = jsonobject
        mViewModel.checkUpdate(param)
    }

    @SuppressLint("CheckResult", "MissingPermission")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_forgetPw -> {

            }

            R.id.rtv_login -> {
                if (binding.etAccount.text.isEmpty()) {
                    ToastUtil.showToast(i18N(ja.insepector.base.R.string.请输入账号))
                    return
                }
                if (binding.etPw.text.isEmpty()) {
                    i18N(ja.insepector.base.R.string.请输入密码)
                    return
                }
                var rxPermissions = RxPermissions(this@LoginActivity)
                rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe {
                    if (it) {
                        if (locationManager == null) {
                            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val provider = LocationManager.NETWORK_PROVIDER
                            locationManager?.requestLocationUpdates(provider, 1000, 1f, object : LocationListener {
                                override fun onLocationChanged(location: Location) {
                                    lat = location.latitude
                                    lon = location.longitude
                                    locationEnable = true
                                }

                                override fun onProviderDisabled(provider: String) {
                                    locationEnable = false
                                    ToastUtil.showToast(i18N(ja.insepector.base.R.string.请打开位置信息))
                                }

                                override fun onProviderEnabled(provider: String) {
                                    locationEnable = true
                                }
                            })
                        }
                        if (locationEnable) {
                            showProgressDialog(20000)
                            val param = HashMap<String, Any>()
                            val jsonobject = JSONObject()
                            jsonobject["loginName"] = binding.etAccount.text.toString()
                            jsonobject["password"] = binding.etPw.text.toString()
                            jsonobject["longitude"] = lon
                            jsonobject["latitude"] = lat
                            param["attr"] = jsonobject
                            mViewModel.login(param)
                        } else {
                            ToastUtil.showToast(i18N(ja.insepector.base.R.string.请打开位置信息))
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun startObserve() {
        super.startObserve()
        mViewModel.apply {
            loginLiveData.observe(this@LoginActivity) {
                dismissProgressDialog()
                startAct<StreetChooseActivity>(data = Bundle().apply {
                    putParcelable(ARouterMap.LOGIN_INFO, it)
                })
            }
            checkUpdateLiveDate.observe(this@LoginActivity) {
                updateBean = it
                if (updateBean?.state == "0" && updateBean?.force == "1") {
                    DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.发现新版本是否下载安装更新))
                        .setRightMsg(i18N(ja.insepector.base.R.string.确定)).setCancelable(false)
                        .isAloneButton(true)
                        .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                            override fun onLeftClickLinsener(msg: String) {
                            }

                            override fun onRightClickLinsener(msg: String) {
                                requestionPermission()
                            }

                        }).build(this@LoginActivity).showDailog()
                    runBlocking {
                        PreferencesDataStore(BaseApplication.instance()).putLong(
                            PreferencesKeys.lastCheckUpdateTime,
                            System.currentTimeMillis()
                        )
                    }
                } else if (updateBean?.state == "0" && updateBean?.force == "0") {
                    runBlocking {
                        val lastTime = PreferencesDataStore(BaseApplication.instance()).getLong(PreferencesKeys.lastCheckUpdateTime)
                        if (System.currentTimeMillis() - lastTime > 12 * 60 * 60 * 1000) {
                            DialogHelp.Builder().setTitle(i18N(ja.insepector.base.R.string.发现新版本是否下载安装更新))
                                .setRightMsg(i18N(ja.insepector.base.R.string.确定)).setCancelable(true)
                                .setLeftMsg(i18N(ja.insepector.base.R.string.取消)).setCancelable(true)
                                .setOnButtonClickLinsener(object : DialogHelp.OnButtonClickLinsener {
                                    override fun onLeftClickLinsener(msg: String) {
                                    }

                                    override fun onRightClickLinsener(msg: String) {
                                        requestionPermission()
                                    }

                                }).build(this@LoginActivity).showDailog()
                            PreferencesDataStore(BaseApplication.instance()).putLong(
                                PreferencesKeys.lastCheckUpdateTime,
                                System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
            errMsg.observe(this@LoginActivity) {
                dismissProgressDialog()
                ToastUtil.showToast(it.msg)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    fun requestionPermission() {
        var rxPermissions = RxPermissions(this@LoginActivity)
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
            if (it) {
                if (packageManager.canRequestPackageInstalls()) {
                    downloadFileAndInstall()
                } else {
                    val uri = Uri.parse("package:${AppUtils.getAppPackageName()}")
                    val intent =
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                    requestInstallPackageLauncher.launch(intent)
                }
            } else {

            }
        }
    }

    val requestInstallPackageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            downloadFileAndInstall()
        } else {

        }
    }

    fun downloadFileAndInstall() {
        ToastUtil.showToast(i18N(ja.insepector.base.R.string.开始下载更新))
        GlobalScope.launch(Dispatchers.IO) {
            FileDownloader.setup(this@LoginActivity)
            val path = "${PathUtils.getExternalDownloadsPath()}/${FileDownloadUtils.generateFileName(updateBean?.url)}.apk"
            FileDownloader.getImpl().create(updateBean?.url)
                .setPath(path)
                .setListener(object : FileDownloadListener() {
                    override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    }

                    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                        Log.v("123", "${(soFarBytes * 100f / totalBytes.toFloat()).toInt()}")
                    }

                    override fun completed(task: BaseDownloadTask?) {
                        AppUtils.installApp(path)
                    }

                    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    }

                    override fun error(task: BaseDownloadTask?, e: Throwable?) {
                        Log.v("123", e.toString())
                    }

                    override fun warn(task: BaseDownloadTask?) {
                    }

                }).start()
        }
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override fun providerVMClass(): Class<LoginViewModel> {
        return LoginViewModel::class.java
    }

    override val isFullScreen: Boolean
        get() = false

}