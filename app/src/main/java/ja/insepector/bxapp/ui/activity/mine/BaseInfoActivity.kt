package ja.insepector.bxapp.ui.activity.mine

import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.bean.Street
import ja.insepector.base.ext.i18N
import ja.insepector.base.viewbase.VbBaseActivity
import ja.insepector.common.realm.RealmUtil
import ja.insepector.common.util.GlideUtils
import ja.insepector.bxapp.R
import ja.insepector.bxapp.databinding.ActivityBaseInfoBinding
import ja.insepector.bxapp.mvvm.viewmodel.BaseInfoViewModel

@Route(path = ARouterMap.BASE_INFO)
class BaseInfoActivity : VbBaseActivity<BaseInfoViewModel, ActivityBaseInfoBinding>(), OnClickListener {
    var streetList: MutableList<Street> = ArrayList()
    override fun initView() {
        GlideUtils.instance?.loadImage(binding.layoutToolbar.ivBack, ja.insepector.common.R.mipmap.ic_back_white)
        binding.layoutToolbar.tvTitle.text = i18N(ja.insepector.base.R.string.基本信息)
        binding.layoutToolbar.tvTitle.setTextColor(ContextCompat.getColor(BaseApplication.instance(), ja.insepector.base.R.color.white))

    }

    override fun initListener() {
        binding.layoutToolbar.flBack.setOnClickListener(this)
    }

    override fun initData() {
        streetList = RealmUtil.instance?.findCheckedStreetList() as MutableList<Street>
        for (i in streetList) {
            val street = View.inflate(BaseApplication.instance(), R.layout.item_baseinfo_street, null)
            street.findViewById<TextView>(R.id.tv_street).text = i.streetName
            binding.llStreet.addView(street)
        }
        mViewModel.getBaseInfo()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fl_back -> {
                onBackPressedSupport()
            }
        }
    }

    override fun startObserve() {
        super.startObserve()
        mViewModel.apply {
            baseInfoLiveData.observe(this@BaseInfoActivity) {
                binding.tvName.text = it[0]
                binding.tvAccount.text = it[1]
                binding.tvPhoneNum.text = it[2]
            }
        }
    }

    override fun providerVMClass(): Class<BaseInfoViewModel>? {
        return BaseInfoViewModel::class.java
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityBaseInfoBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override val isFullScreen: Boolean
        get() = true

    override fun marginStatusBarView(): View? {
        return binding.layoutToolbar.ablToolbar
    }
}