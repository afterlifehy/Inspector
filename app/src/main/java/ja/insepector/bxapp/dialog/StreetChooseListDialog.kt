package ja.insepector.bxapp.dialog

import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import ja.insepector.base.BaseApplication
import ja.insepector.base.bean.Street
import ja.insepector.base.dialog.VBBaseLibDialog
import ja.insepector.base.ext.gone
import ja.insepector.base.help.ActivityCacheManager
import ja.insepector.bxapp.R
import ja.insepector.bxapp.adapter.ChooseStreetAdapter
import ja.insepector.bxapp.databinding.DialogStreetListBinding

class StreetChooseListDialog(
    val streetList: MutableList<Street>,
    var streetChoosedList: MutableList<Street>,
    val callback: StreetChooseCallBack
) :
    VBBaseLibDialog<DialogStreetListBinding>(
        ActivityCacheManager.instance().getCurrentActivity()!!,
        ja.insepector.base.R.style.CommonBottomDialogStyle
    ), OnClickListener {

    var chooseStreetAdapter: ChooseStreetAdapter? = null

    init {
        initView()
    }

    private fun initView() {
        var height = SizeUtils.dp2px(60f) * (streetList.size + 1) + SizeUtils.dp2px(174f)
        if (height > ScreenUtils.getAppScreenHeight() - BarUtils.getStatusBarHeight()) {
            height = ScreenUtils.getAppScreenHeight() - BarUtils.getStatusBarHeight()
        }
        setLayout(ScreenUtils.getAppScreenWidth().toFloat(), height.toFloat())
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        binding.tvDialogTitle.gone()
        binding.rvStreet.setHasFixedSize(true)
        binding.rvStreet.layoutManager = LinearLayoutManager(BaseApplication.instance())
        chooseStreetAdapter = ChooseStreetAdapter(streetList, streetChoosedList)
        binding.rvStreet.adapter = chooseStreetAdapter

        binding.rtvOk.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rtv_ok -> {
                callback.chooseStreets()
                dismiss()
            }
        }
    }

    override fun getVbBindingView(): ViewBinding {
        return DialogStreetListBinding.inflate(layoutInflater)
    }

    override fun getHideInput(): Boolean {
        return true
    }

    override fun getWidth(): Float {
        return WindowManager.LayoutParams.MATCH_PARENT.toFloat()
    }

    override fun getHeight(): Float {
        return WindowManager.LayoutParams.WRAP_CONTENT.toFloat()
    }

    override fun getCanceledOnTouchOutside(): Boolean {
        return true
    }

    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }

    interface StreetChooseCallBack {
        fun chooseStreets()
    }
}