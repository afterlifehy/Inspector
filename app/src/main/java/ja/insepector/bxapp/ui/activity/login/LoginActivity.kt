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
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.AppUtils
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.ext.i18N
import ja.insepector.base.util.ToastUtil
import ja.insepector.base.viewbase.VbBaseActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import ja.insepector.base.bean.UpdateBean
import ja.insepector.base.ext.startAct
import ja.insepector.bxapp.R
import ja.insepector.bxapp.databinding.ActivityLoginBinding
import ja.insepector.bxapp.mvvm.viewmodel.LoginViewModel
import ja.insepector.bxapp.util.UpdateUtil

class LoginActivity : VbBaseActivity<LoginViewModel, ActivityLoginBinding>(), OnClickListener {
    var locationManager: LocationManager? = null
    var lat = 0.00
    var lon = 0.00
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
                        ToastUtil.showMiddleToast(i18N(ja.insepector.base.R.string.请打开位置信息))
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
                    binding.rtvLogin.setOnClickListener(this@LoginActivity)
                } else {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_9904a091
                        )
                    )
                    binding.rtvLogin.setOnClickListener(null)
                }
                binding.rtvLogin.delegate.init()
            }

        })
        binding.etAccount.setOnEditorActionListener { textView, i, keyEvent ->
            binding.etPw.requestFocus()
        }
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
                    binding.rtvLogin.setOnClickListener(this@LoginActivity)
                } else {
                    binding.rtvLogin.delegate.setBackgroundColor(
                        ContextCompat.getColor(
                            BaseApplication.instance(),
                            ja.insepector.base.R.color.color_9904a091
                        )
                    )
                    binding.rtvLogin.setOnClickListener(null)
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
                                    ToastUtil.showMiddleToast(i18N(ja.insepector.base.R.string.请打开位置信息))
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
                            ToastUtil.showMiddleToast(i18N(ja.insepector.base.R.string.请打开位置信息))
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
                if (updateBean?.state == "0") {
                    UpdateUtil.instance?.checkNewVersion(updateBean!!, object : UpdateUtil.UpdateInterface {
                        override fun requestionPermission() {
                            requestPermissions()
                        }
                    })
                }
            }
            errMsg.observe(this@LoginActivity) {
                dismissProgressDialog()
                ToastUtil.showMiddleToast(it.msg)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    fun requestPermissions() {
        var rxPermissions = RxPermissions(this@LoginActivity)
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