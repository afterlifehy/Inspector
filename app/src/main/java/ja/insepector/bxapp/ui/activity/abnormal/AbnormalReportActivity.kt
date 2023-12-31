package ja.insepector.bxapp.ui.activity.abnormal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.TimeUtils
import ja.insepector.base.BaseApplication
import ja.insepector.base.arouter.ARouterMap
import ja.insepector.base.bean.Street
import ja.insepector.base.ds.PreferencesDataStore
import ja.insepector.base.ds.PreferencesKeys
import ja.insepector.base.ext.gone
import ja.insepector.base.ext.hide
import ja.insepector.base.ext.i18n
import ja.insepector.base.ext.show
import ja.insepector.base.ext.startArouter
import ja.insepector.base.util.ToastUtil
import ja.insepector.base.viewbase.VbBaseActivity
import ja.insepector.common.realm.RealmUtil
import ja.insepector.common.util.AppUtil
import ja.insepector.common.util.GlideUtils
import ja.insepector.common.view.keyboard.KeyboardUtil
import ja.insepector.common.view.keyboard.MyTextWatcher
import ja.insepector.bxapp.R
import ja.insepector.bxapp.adapter.CollectionPlateColorAdapter
import ja.insepector.bxapp.databinding.ActivityAbnormalReportBinding
import ja.insepector.bxapp.dialog.AbnormalClassificationDialog
import ja.insepector.bxapp.dialog.AbnormalStreetListDialog
import ja.insepector.bxapp.mvvm.viewmodel.AbnormalReportViewModel
import ja.insepector.common.event.RefreshParkingSpaceEvent
import ja.insepector.common.util.Constant
import ja.insepector.common.util.FileUtil
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Route(path = ARouterMap.ABNORMAL_REPORT)
class AbnormalReportActivity : VbBaseActivity<AbnormalReportViewModel, ActivityAbnormalReportBinding>(), OnClickListener {
    var collectionPlateColorAdapter: CollectionPlateColorAdapter? = null
    var collectioPlateColorList: MutableList<String> = ArrayList()
    var checkedColor = ""
    private lateinit var keyboardUtil: KeyboardUtil
    val widthType = 1

    var abnormalStreetListDialog: AbnormalStreetListDialog? = null
    var streetList: MutableList<Street> = ArrayList()

    var abnormalClassificationDialog: AbnormalClassificationDialog? = null
    var classificationList: MutableList<String> = ArrayList()
    var currentStreet: Street? = null

    var parkingNo = ""
    var orderNo = ""
    var carColor = ""
    var carLicense = ""
    var type = ""
    var simId = ""
    var loginName = ""

    var photoType = 10
    var plateBase64 = ""
    var panoramaBase64 = ""
    var plateImageBitmap: Bitmap? = null
    var plateFileName = ""
    var panoramaFileName = ""
    var panoramaImageBitmap: Bitmap? = null

    override fun initView() {
        binding.layoutToolbar.tvTitle.text = i18n(ja.insepector.base.R.string.泊位异常上报)
        GlideUtils.instance?.loadImage(binding.layoutToolbar.ivRight, ja.insepector.common.R.mipmap.ic_help)
        binding.layoutToolbar.ivRight.show()

        if (intent.getStringExtra(ARouterMap.ABNORMAL_CARLICENSE) != null) {
            parkingNo = intent.getStringExtra(ARouterMap.ABNORMAL_PARKING_NO)!!
            carLicense = intent.getStringExtra(ARouterMap.ABNORMAL_CARLICENSE)!!
        }

        collectioPlateColorList.add(Constant.BLUE)
        collectioPlateColorList.add(Constant.GREEN)
        collectioPlateColorList.add(Constant.YELLOW)
        collectioPlateColorList.add(Constant.YELLOW_GREEN)
        collectioPlateColorList.add(Constant.WHITE)
        collectioPlateColorList.add(Constant.BLACK)
        collectioPlateColorList.add(Constant.OTHERS)
        binding.rvPlateColor.isNestedScrollingEnabled = false
        binding.rvPlateColor.setHasFixedSize(true)
        binding.rvPlateColor.layoutManager = LinearLayoutManager(BaseApplication.instance(), LinearLayoutManager.HORIZONTAL, false)
        collectionPlateColorAdapter = CollectionPlateColorAdapter(widthType, collectioPlateColorList, this)
        binding.rvPlateColor.adapter = collectionPlateColorAdapter

        initKeyboard()
    }

    override fun initListener() {
        binding.layoutToolbar.flBack.setOnClickListener(this)
        binding.layoutToolbar.ivRight.setOnClickListener(this)
        binding.cbLotName.setOnClickListener(this)
        binding.cbAbnormalClassification.setOnClickListener(this)
        binding.rflAbnormalClassification.setOnClickListener(this)
        binding.rflRecognize.setOnClickListener(this)
        binding.rflTakePhoto.setOnClickListener(this)
        binding.rflTakePhoto2.setOnClickListener(this)
        binding.ivPlateDelete.setOnClickListener(this)
        binding.ivPanoramaDelete.setOnClickListener(this)
        binding.rivPlate.setOnClickListener(this)
        binding.rivPanorama.setOnClickListener(this)
        binding.rflReport.setOnClickListener(this)
        binding.root.setOnClickListener(this)
        binding.llBerthAbnormal2.setOnClickListener(this)
    }

    override fun initData() {
        RealmUtil.instance?.findCheckedStreetList()?.let { streetList.addAll(it) }
        runBlocking {
            simId = PreferencesDataStore(BaseApplication.instance()).getString(PreferencesKeys.simId)
            loginName = PreferencesDataStore(BaseApplication.instance()).getString(PreferencesKeys.loginName)
        }
        currentStreet = RealmUtil.instance?.findCurrentStreet()
        if (streetList.size == 1) {
            binding.cbLotName.hide()
            binding.rflLotName.setOnClickListener(null)
        } else {
            binding.cbLotName.show()
            binding.rflLotName.setOnClickListener(this)
        }
        binding.tvLotName.text = currentStreet?.streetName
        binding.rtvStreetNo.text = currentStreet?.streetNo
        binding.retParkingNo.setText(parkingNo.replaceFirst(currentStreet?.streetNo + "-", ""))

        classificationList.add(i18n(ja.insepector.base.R.string.无法关单))
        classificationList.add(i18n(ja.insepector.base.R.string.订单丢失))
        classificationList.add(i18n(ja.insepector.base.R.string.车牌录入错误))


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyboard() {
        keyboardUtil = KeyboardUtil(binding.kvKeyBoard) {
            binding.etPlate.requestFocus()
            keyboardUtil.changeKeyboard(true)
            keyboardUtil.setEditText(binding.etPlate)
        }

        binding.etPlate.addTextChangedListener(MyTextWatcher(null, null, true, keyboardUtil))

        binding.etPlate.setOnTouchListener { v, p1 ->
            (v as EditText).requestFocus()
            keyboardUtil.changeKeyboard(true)
            val clickPosition = v.getOffsetForPosition(p1!!.x, p1.y)
            keyboardUtil.setEditText(v, clickPosition)
            keyboardUtil.showKeyboard(show = {
                val location = IntArray(2)
                v.getLocationOnScreen(location)
                val editTextPosY = location[1]

                val screenHeight = window!!.windowManager.defaultDisplay.height
                val distanceToBottom: Int = screenHeight - editTextPosY - v.getHeight()

                if (binding.kvKeyBoard.height > distanceToBottom) {
                    // 当键盘高度超过输入框到屏幕底部的距离时，向上移动布局
                    binding.llBerthAbnormal.translationY = (-(binding.kvKeyBoard.height - distanceToBottom)).toFloat()
                }
            }, hide = {
                binding.llBerthAbnormal.translationY = 0f
            })
            true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (keyboardUtil.isShow()) {
                keyboardUtil.hideKeyboard()
            } else {
                return super.onKeyDown(keyCode, event)
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        if (keyboardUtil.isShow()) {
            keyboardUtil.hideKeyboard()
        }
        when (v?.id) {
            R.id.fl_back -> {
                onBackPressedSupport()
            }

            R.id.iv_right -> {
                startArouter(ARouterMap.ABNORMAL_HELP)
            }

            R.id.cb_lotName -> {
                showAbnormalStreetListDialog()
            }

            R.id.rfl_lotName -> {
                binding.cbLotName.isChecked = true
                showAbnormalStreetListDialog()
            }

            R.id.cb_abnormalClassification -> {
                showAbnormalClassificationDialog()
            }

            R.id.rfl_abnormalClassification -> {
                binding.cbAbnormalClassification.isChecked = true
                showAbnormalClassificationDialog()
            }

            R.id.rfl_recognize -> {
                ARouter.getInstance().build(ARouterMap.SCAN_PLATE).navigation(this@AbnormalReportActivity, 1)
            }

            R.id.rfl_takePhoto -> {
                photoType = 10
                takePhoto()
            }

            R.id.rfl_takePhoto2 -> {
                photoType = 11
                takePhoto()
            }

            R.id.iv_plateDelete -> {
                binding.rflTakePhoto.show()
                binding.rflPlateImg.gone()
                plateImageBitmap = null
                plateBase64 = ""
                plateFileName = ""
            }

            R.id.iv_panoramaDelete -> {
                binding.rflTakePhoto2.show()
                binding.rflPanoramaImg.gone()
                panoramaImageBitmap = null
                panoramaBase64 = ""
                panoramaFileName = ""
            }

            R.id.riv_plate -> {
                photoType = 10
                takePhoto()
            }

            R.id.riv_panorama -> {
                photoType = 11
                takePhoto()
            }

            R.id.rfl_report -> {
                type = AppUtil.fillZero((classificationList.indexOf(binding.tvAbnormalClassification.text.toString()) + 1).toString())
                if (binding.retParkingNo.text.toString().isEmpty()) {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请填写泊位号))
                    return
                }
                if (type == "00") {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请选择异常分类))
                    return
                }
                if (type == "03" && binding.etPlate.text.toString().isEmpty()) {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请填写车牌))
                    return
                }
                if (type == "03") {
                    if (binding.etPlate.text.toString().length != 7 && binding.etPlate.text.toString().length != 8) {
                        ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.车牌长度只能是7位或8位))
                        return
                    }
                }
                if (type == "03" && checkedColor.isEmpty()) {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请选择车牌颜色))
                    return
                }
                if (plateImageBitmap == null) {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请上传车牌照))
                    return
                }
                if (panoramaImageBitmap == null) {
                    ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.请上传全景照))
                    return
                }
                plateImageBitmap = addTextWatermark(plateImageBitmap!!)
                panoramaImageBitmap = addTextWatermark(panoramaImageBitmap!!)
                convertBase64(plateImageBitmap!!, 10)
                convertBase64(panoramaImageBitmap!!, 11)
                val param = HashMap<String, Any>()
                val jsonobject = JSONObject()
                jsonobject["parkingNo"] = currentStreet?.streetNo + "-" + fillZero(binding.retParkingNo.text.toString())
                param["attr"] = jsonobject
                showProgressDialog(20000)
                mViewModel.inquiryOrderNoByParkingNo(param)
            }

            R.id.ll_berthAbnormal2, binding.root.id -> {

            }

            R.id.fl_color -> {
                checkedColor = v.tag as String
                collectionPlateColorAdapter?.updateColor(checkedColor, collectioPlateColorList.indexOf(checkedColor))
            }
        }
    }

    fun fillZero(value: String): String {
        if (value.length == 2) {
            return "0" + value
        } else if (value.length == 1) {
            return "00" + value
        } else {
            return value
        }
    }

    fun showAbnormalStreetListDialog() {
        abnormalStreetListDialog =
            AbnormalStreetListDialog(streetList, currentStreet!!, object : AbnormalStreetListDialog.AbnormalStreetCallBack {
                override fun chooseStreet(street: Street) {
                    currentStreet = street
                    binding.tvLotName.text = currentStreet?.streetName
                    binding.rtvStreetNo.text = currentStreet?.streetNo
                }
            })
        abnormalStreetListDialog?.show()
        abnormalStreetListDialog?.setOnDismissListener {
            binding.cbLotName.isChecked = false
        }
    }

    fun showAbnormalClassificationDialog() {
        abnormalClassificationDialog = AbnormalClassificationDialog(classificationList,
            binding.tvAbnormalClassification.text.toString(),
            object : AbnormalClassificationDialog.AbnormalClassificationCallBack {
                override fun chooseClassification(classification: String) {
                    binding.tvAbnormalClassification.text = classification
                    if (classification == i18n(ja.insepector.base.R.string.车牌录入错误)) {
                        binding.llPlate.show()
                        binding.rvPlateColor.show()
                        binding.rlTakePhoto.show()
                    } else {
                        binding.llPlate.gone()
                        binding.rvPlateColor.gone()
                        binding.rlTakePhoto.gone()
                    }
                }
            })
        abnormalClassificationDialog?.show()
        abnormalClassificationDialog?.setOnDismissListener {
            binding.cbAbnormalClassification.isChecked = false
        }
    }

    fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = createImageFile()
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "ja.insepector.bxapp.fileprovider",
            photoFile!!
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        takePictureLauncher.launch(takePictureIntent)
    }

    val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            var imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
            imageBitmap = ImageUtils.compressBySampleSize(imageBitmap, 10)
            imageBitmap = FileUtil.compressToMaxSize(imageBitmap, 50, false)
            ImageUtils.save(imageBitmap, imageFile, Bitmap.CompressFormat.JPEG)
            if (photoType == 10) {
                plateImageBitmap = imageBitmap
                GlideUtils.instance?.loadImage(binding.rivPlate, plateImageBitmap)
                binding.rflTakePhoto.hide()
                binding.rflPlateImg.show()
            } else {
                panoramaImageBitmap = imageBitmap
                GlideUtils.instance?.loadImage(binding.rivPanorama, panoramaImageBitmap)
                binding.rflTakePhoto2.hide()
                binding.rflPanoramaImg.show()
            }
            if (panoramaImageBitmap == null) {
                photoType = 11
                takePhoto()
            }
        }
    }

    fun addTextWatermark(imageBitmap: Bitmap): Bitmap? {
        var bitmap = ImageUtils.addTextWatermark(
            imageBitmap,
            TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"),
            16, Color.RED, 6f, 3f
        )
        bitmap = ImageUtils.addTextWatermark(
            bitmap,
            parkingNo + "   " + binding.etPlate.toString(),
            16, Color.RED, 6f, 19f
        )
        return bitmap
    }

    fun convertBase64(imageBitmap: Bitmap, type: Int) {
        val bytes = ConvertUtils.bitmap2Bytes(imageBitmap)

        if (type == 10) {
            plateBase64 = EncodeUtils.base64Encode2String(bytes)
            plateFileName = imageFile!!.name
        } else {
            panoramaBase64 = EncodeUtils.base64Encode2String(bytes)
            panoramaFileName = imageFile!!.name
        }
    }

    var currentPhotoPath = ""
    var imageFile: File? = null
    private fun createImageFile(): File? {
        // 创建图像文件名称
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(
            "PNG_${timeStamp}_", /* 前缀 */
            ".png", /* 后缀 */
            storageDir /* 目录 */
        )

        currentPhotoPath = imageFile!!.absolutePath
        return imageFile
    }

    fun uploadImg(orderNo: String, photo: String, name: String, type: Int) {
        val param = HashMap<String, Any>()
        val jsonobject = JSONObject()
        jsonobject["businessId"] = orderNo
        jsonobject["photoName"] = name
        jsonobject["photoType"] = type
        jsonobject["photoFormat"] = "png"
        jsonobject["photo"] = photo
        jsonobject["simId"] = simId
        param["attr"] = jsonobject
        mViewModel.picUpload(param)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val plate = data?.getStringExtra("plate")
                if (!plate.isNullOrEmpty()) {
                    val plateId = if (plate.contains("新能源")) {
                        plate.substring(plate.length - 8, plate.length)
                    } else {
                        plate.substring(plate.length.minus(7) ?: 0, plate.length)
                    }
                    binding.etPlate.setText(plateId)
                    binding.etPlate.setSelection(plateId.length)
                    if (plate.startsWith("蓝")) {
                        collectionPlateColorAdapter?.updateColor(Constant.BLUE, 0)
                    } else if (plate.startsWith("绿")) {
                        collectionPlateColorAdapter?.updateColor(Constant.GREEN, 1)
                    } else if (plate.startsWith("黄")) {
                        collectionPlateColorAdapter?.updateColor(Constant.YELLOW, 2)
                    } else if (plate.startsWith("黄绿")) {
                        collectionPlateColorAdapter?.updateColor(Constant.YELLOW_GREEN, 3)
                    } else if (plate.startsWith("白")) {
                        collectionPlateColorAdapter?.updateColor(Constant.WHITE, 4)
                    } else if (plate.startsWith("黑")) {
                        collectionPlateColorAdapter?.updateColor(Constant.BLACK, 5)
                    } else {
                        collectionPlateColorAdapter?.updateColor(Constant.OTHERS, 6)
                    }
                }
            }
        }
    }

    override fun startObserve() {
        super.startObserve()
        mViewModel.apply {
            inquiryOrderNoByParkingNoLiveData.observe(this@AbnormalReportActivity) {
                orderNo = it.orderNo
                val param = HashMap<String, Any>()
                val jsonobject = JSONObject()
                jsonobject["parkingNo"] = currentStreet?.streetNo + "-" + fillZero(binding.retParkingNo.text.toString())
                jsonobject["state"] = type
                jsonobject["remark"] = binding.retRemarks.text.toString()
                if (type == "03") {
                    jsonobject["carLicenseNew"] = binding.etPlate.text.toString()
                    jsonobject["carColor"] = checkedColor
                } else {
                    jsonobject["carLicenseNew"] = carLicense
                    jsonobject["carColor"] = carColor
                }
                jsonobject["orderNo"] = orderNo
                param["attr"] = jsonobject
                mViewModel.abnormalReport(param)
            }
            abnormalReportLiveData.observe(this@AbnormalReportActivity) {
                dismissProgressDialog()
                uploadImg(orderNo, plateBase64, plateFileName, 10)
                uploadImg(orderNo, panoramaBase64, panoramaFileName, 11)
                ToastUtil.showMiddleToast(i18n(ja.insepector.base.R.string.上报成功))
                EventBus.getDefault().post(RefreshParkingSpaceEvent())
                onBackPressedSupport()
            }
            errMsg.observe(this@AbnormalReportActivity) {
                dismissProgressDialog()
                ToastUtil.showMiddleToast(it.msg)
            }
        }
    }

    override fun getVbBindingView(): ViewBinding {
        return ActivityAbnormalReportBinding.inflate(layoutInflater)
    }

    override fun onReloadData() {
    }

    override val isFullScreen: Boolean
        get() = true

    override fun marginStatusBarView(): View {
        return binding.layoutToolbar.ablToolbar
    }

    override fun providerVMClass(): Class<AbnormalReportViewModel> {
        return AbnormalReportViewModel::class.java
    }

}