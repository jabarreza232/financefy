package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import id.co.evolution.financefy.R
import id.co.evolution.financefy.helper.TinyDb
import id.co.evolution.financefy.helper.Tools
import id.co.evolution.financefy.model.ModelPrimaryColor
import id.co.evolution.financefy.model.ModelSavings
import id.co.evolution.financefy.model.ModelUser
import java.util.*

class DialogCreateUser(
    val context: Context,
    val inflater: LayoutInflater
) : View.OnClickListener {

    val dialog: Dialog = Dialog(context)
    val dialogView: View = inflater.inflate(R.layout.dialog_create_user, null)


    lateinit var dialogCreateUserCallback: DialogCreateUserCallback
    var jumlah = ""
    var type = ""
    var type_currency = ""
    var category = ""
    var date_target = ""
    var proccess_value = ""
    var arrayCategoryFromResource: Int = 0
    var isAddAccount = false
    private lateinit var arrayCategory: Array<String>
    private lateinit var dialogCalculator: DialogCalculator
    private var user: ModelUser = ModelUser()
    private var countSavings: Int = 0
    private var savings: ModelSavings = ModelSavings()
    private lateinit var fragmentManager: FragmentManager
    var cur_calendar = Calendar.getInstance()

    private var etName: TextInputEditText
    private var tilName: TextInputLayout
    private var etTarget: TextInputEditText
    private var tilTarget: TextInputLayout
    private var etTitle: TextInputEditText
    private var tilTitle: TextInputLayout
    private var placeTarget: RelativeLayout
    private var placeDate: RelativeLayout
    private var placeTypeCurrencyTarget: RelativeLayout
    private var txtType: TextView
    private var txtTypeCurrency: TextView
    private var txtTypeCurrencyTarget: TextView
    private var txtDate: TextView
    private var txtHeaderTargetSavings: TextView
    private var viewLineHeaderSavings: View
    private var txtCategory: TextView
    private var txtHeader: TextView
    private var btnSubmit: CardView
    private var btnCalculator: Button
    private var btnClose: ImageView
    private var imgDropDownTypeCurrency: ImageView
    private var imgDropDownCategory: ImageView
    private lateinit var tinyDb:TinyDb
    lateinit var locale:Locale


    init {
        dialog.setContentView(dialogView)
        etName = findViewById(R.id.et_name)
        txtHeader = findViewById(R.id.txt_header)
        tilName = findViewById(R.id.til_name)
        tilTarget = findViewById(R.id.til_target_value)
        etTarget = findViewById(R.id.et_target_value)
        tilTitle = findViewById(R.id.til_title)
        etTitle = findViewById(R.id.et_title)
        placeTarget = findViewById(R.id.place_target_value)
        placeTypeCurrencyTarget = findViewById(R.id.place_type_currency_target_savings)
        placeDate = findViewById(R.id.place_date)
        txtType = findViewById(R.id.txt_type)
        txtTypeCurrency = findViewById(R.id.txt_type_currency)
        txtTypeCurrencyTarget = findViewById(R.id.txt_type_currency_target)
        txtCategory = findViewById(R.id.txt_category)
        txtHeaderTargetSavings = findViewById(R.id.txt_header_target_savings)
        viewLineHeaderSavings = findViewById(R.id.view_line_target_savings)
        txtDate = findViewById(R.id.txt_date)
        btnSubmit = findViewById(R.id.cv_submit)
        btnClose = findViewById(R.id.img_close)
        btnCalculator = findViewById(R.id.btn_calculator)
        imgDropDownTypeCurrency = findViewById(R.id.img_dropdown_type_currency)
        imgDropDownCategory = findViewById(R.id.img_dropdown_category)
    }


    constructor (
        context: Context,
        inflater: LayoutInflater,
        fragmentManager: FragmentManager,
        countSavings:Int,
        userUpdate: ModelUser,
        savingsUpdate: ModelSavings?,
        dialogCreateUserCallback: DialogCreateUserCallback
    ) : this(context, inflater) {
        this.dialogCreateUserCallback = dialogCreateUserCallback
        this.fragmentManager = fragmentManager
        this.user = userUpdate
        this.savings = savingsUpdate ?: ModelSavings()
        this.tinyDb = TinyDb(context)
        this.countSavings = countSavings
        locale = if (user.type_currency.equals(
                "IDR",
                ignoreCase = true
            )
        ) Tools.getLocaleIDN() else Tools.getLocaleUS()

    }

    fun showDialogCreateUser(isAddAccount: Boolean,modelPrimaryColor:ModelPrimaryColor) {
        this.isAddAccount = isAddAccount
        val textHeader = if (isAddAccount) "Masukkan form User" else "Ubah form User"
        val textHeaderSavings = if (isAddAccount||countSavings==0) "Masukkan Form Target Menabung" else "Ubah Form Target Menabung"
        txtHeader.text = textHeader

        txtHeaderTargetSavings.text = textHeaderSavings
        Tools.setBackgroundTintView(btnCalculator, modelPrimaryColor)

        if (!isAddAccount) {
            txtTypeCurrency.isEnabled = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            user.let {
                etName.setText(it.name)
                category = it.category
                type = it.type
                type_currency = it.type_currency
                setUpCategory()
                txtType.text = it.type
                txtTypeCurrency.text = it.type_currency
                txtCategory.text = it.category
            }


            if (user.category.equals(context.getString(R.string.menabung))) {
                savings.let {
                    if (it.id>0){
                        etTitle.setText(it.title)
                        txtDate.text = it.date_target
                        txtTypeCurrencyTarget.text = it.type_currency
                        etTarget.setText(Tools.convertToCurrency(it.targetValue,locale))
                        date_target = it.date_target
                        jumlah = Tools.convertToCurrency(it.targetValue,locale).replace("[Rp,.$]".toRegex(), "")
                        proccess_value = Tools.convertToCurrency(it.processValue,locale).replace("[Rp,.$]".toRegex(), "")

                    }
                }
            }
            }
        }

        etTarget addTextChangedListener object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != jumlah) {
                    etTarget.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[Rp,.$]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        jumlah = Tools.convertToCurrency(parsed,locale)
                        etTarget.setText(Tools.convertToCurrency(parsed,locale))
                        etTarget.setSelection(Tools.convertToCurrency(parsed,locale).length)
                    }
                    etTarget.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }

        txtType onClick this
        txtTypeCurrency onClick this
        txtCategory onClick this
        placeDate onClick this
        btnSubmit onClick this
        btnClose onClick this
        btnCalculator onClick this


        val window = dialog.window
        val wlp = window!!.attributes

        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        dialog.window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
    }

    infix fun View.onClick(onclick: View.OnClickListener) {
        setOnClickListener(onclick)
    }

    private infix fun TextInputEditText.addTextChangedListener(textWatcher: TextWatcher) {
        addTextChangedListener(textWatcher)
    }


    private fun <T : View> findViewById(value: Int): T {
        return dialogView.findViewById(value)
    }

    private fun <T : ViewGroup> findViewGroupById(value: Int): T {

        return dialogView.findViewById(value)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_calculator -> {
                showDialogCalculator()
            }
            R.id.txt_type -> {
                showDialogType()
            }
            R.id.place_date -> {
                showDatePickerDialog()
            }
            R.id.txt_category -> {
                if (type.isNotEmpty()) {
                    showDialogCategory()
                } else {
                    Toast.makeText(context, "Pilih tipe terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.txt_type_currency -> {
                if (category.isNotEmpty()) {
                    showDialogTypeCurrency()
                } else {
                    Toast.makeText(context, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.cv_submit -> {
                val messageError: String
                val user = ModelUser()
                val savings = ModelSavings()

                try {
                    if (etName.text.toString().isEmpty()) {
                        messageError = "Silahkan input nama terlebih dahulu"
                        tilName.error = messageError
                        throw Exception(messageError)
                    } else {
                        tilName.error = null
                    }

                    if (type.isEmpty()) {
                        messageError = "Silahkan Pilih tipe terlebih dahulu"
                        throw Exception(messageError)
                    }

                    if (type_currency.isEmpty()) {
                        messageError = "Silahkan Pilih tipe mata uang terlebih dahulu"
                        throw Exception(messageError)
                    }




                    if(isAddAccount || savings.id>0||countSavings==0){
                        if (category == context.getString(R.string.menabung)) {
                            if (etTitle.text.toString().isEmpty()) {
                                messageError = "Silahkan input judul menabung terlebih dahulu"
                                tilTitle.error = messageError
                                throw Exception(messageError)
                            } else {
                                tilTitle.error = null
                            }
                            if (date_target.isEmpty()) {
                                messageError =
                                    "Silahkan input tanggal target menabung terlebih dahulu"
                                throw Exception(messageError)
                            } else {
                                txtDate.error = null
                            }

                            if (etTarget.text.toString().isEmpty()) {
                                messageError = "Silahkan input target menabung terlebih dahulu"
                                tilTarget.error = messageError
                                throw Exception(messageError)
                            } else {
                                tilTarget.error = null
                            }

//                        user.targetValue = Tools.replaceCurrencyStringToLong(jumlah)

                            savings.title = etTitle.text.toString()
                            savings.date_target = date_target
                            savings.processValue = 0
                            savings.targetValue = Tools.replaceCurrencyStringToLong(jumlah)
                            savings.type_currency = type_currency
                        }
                    }


                    if (!isAddAccount) {
                        user.id = this.user.id
                        if(this.savings.id>0)
                            savings.id = this.savings.id

                        savings.id_savings_user = user.id
                        if(etTitle.text.toString().isNotEmpty()){
                            savings.title = etTitle.text.toString()
                            savings.date_target = date_target
                            if(proccess_value.isNotEmpty()) savings.processValue = Tools.replaceCurrencyStringToLong(proccess_value)
                            savings.targetValue = Tools.replaceCurrencyStringToLong(jumlah)
                            savings.type_currency = type_currency
                        }
                    }



                    user.name = etName.text.toString()
                    user.type = type
                    user.category = category
                    user.type_currency = type_currency


                    dialogCreateUserCallback.onSubmit(user, savings)

                    dialog.dismiss()

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btn_calculator -> {
                dialogCalculator = DialogCalculator(context, inflater) { result: String? ->
                    jumlah = Tools.convertToCurrency(result,locale)
                    etTarget.setText(jumlah)
                }
                dialogCalculator.show()
            }

            R.id.img_close -> {
                dialog.dismiss()
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog =
            DatePickerDialog.newInstance { view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val calendar = Calendar.getInstance()
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = monthOfYear
                calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                val date_ship_milis = calendar.timeInMillis
                txtDate.text = Tools.getFormattedDateSimple(date_ship_milis)
                date_target = Tools.getFormattedDateSimple(date_ship_milis)
            }
        datePickerDialog.minDate = cur_calendar
        datePickerDialog.accentColor = context.resources.getColor(R.color.colorPrimary)

        datePickerDialog.show(fragmentManager, "PickerDialog")
    }

    private fun showDialogCalculator() {
        dialogCalculator =
            DialogCalculator(context, inflater) { result: String? ->
                jumlah = Tools.convertToCurrency(result,locale)
                etTarget.setText(jumlah)
            }
        dialogCalculator.show()
    }

    private fun showDialogCategory() {
        val dialogFinance = DialogFinance(context, object : DialogFinance.DialogFinanceCallback {
            override fun onSubmit(index: Int, result: String) {
                category = result
                setUpCategory()
                if (category.isNotEmpty()) txtCategory.text = category

                if(type_currency.isNotEmpty())
                    txtTypeCurrencyTarget.text = type_currency
            }
        })

        dialogFinance.showDialog(arrayCategoryFromResource,"Pilih Kategori")
    }

    private fun showDialogType() {
        val dialogFinance = DialogFinance(context, object : DialogFinance.DialogFinanceCallback {
            override fun onSubmit(index: Int, result: String) {
                type = result

                //TODO ketika memilih tipe usaha maka category tidak bisa di pilih
                //TODO Dan di set default false
                setUpCategory()

                category = if (type.equals(
                        context.getString(R.string.usaha),
                        ignoreCase = true
                    )
                ) arrayCategory[1] else arrayCategory[0]

                if(isAddAccount) setVisibilityPlaceSavings()
                else {
                    if(savings.id>0||countSavings==0) setVisibilityPlaceSavings()
                }

                txtCategory.text = category

                txtType.text = type

                if(category.equals(
                        context.getString(R.string.menabung),
                        ignoreCase = true
                    )) {
                    if(type_currency.isNotEmpty())
                    txtTypeCurrencyTarget.text = type_currency

                    if(jumlah.isNotEmpty()){
                        jumlah = Tools.convertToCurrency(jumlah.replace("[Rp,.$]".toRegex(), ""),locale)
                        etTarget.setText(jumlah)
                    }
                }
            }
        })
        dialogFinance.showDialog(R.array.type_user,"Pilih Tipe")
    }
    private fun showDialogTypeCurrency() {
        val dialogFinance = DialogFinance(context, object : DialogFinance.DialogFinanceCallback {
            override fun onSubmit(index: Int, result: String) {
                type_currency = result
                txtTypeCurrency.text = type_currency

                locale = if(type_currency.equals("IDR",true)){
                    Tools.getLocaleIDN()
                }else{
                    Tools.getLocaleUS()
                }

                if(category.equals(
                    context.getString(R.string.menabung),
                    ignoreCase = true
                )) {
                    if(type_currency.isNotEmpty())
                        txtTypeCurrencyTarget.text = type_currency

                    if(jumlah.isNotEmpty()){
                        jumlah = Tools.convertToCurrency(jumlah.replace("[Rp,.$]".toRegex(), ""),locale)
                        etTarget.setText(jumlah)
                    }
                }

            }
        })
        dialogFinance.showDialog(R.array.type_currency,"Pilih Tipe Mata Uang")
    }

    private fun setUpCategory() {

        txtCategory.isEnabled = type.equals(
            context.getString(R.string.pribadi),
            ignoreCase = true
        )

        arrayCategoryFromResource = R.array.category_user
        arrayCategory = context.resources.getStringArray(arrayCategoryFromResource)

        if(isAddAccount) setVisibilityPlaceSavings()
        else {
            if(savings.id>0||countSavings==0) setVisibilityPlaceSavings()
        }

    }

    fun setVisibilityPlaceSavings() {
        val isShow =category.equals(
            context.getString(R.string.menabung),
            ignoreCase = true
        )

        placeTarget.showHideView(isShow)
        tilTitle.showHideView(isShow)
        placeDate.showHideView(isShow)
        txtHeaderTargetSavings.showHideView(isShow)
        placeTypeCurrencyTarget.showHideView(isShow)
        viewLineHeaderSavings.showHideView(isShow)
    }
    fun setHideVisibilityPlaceSavings() {
        val isShow =false

        placeTarget.showHideView(isShow)
        tilTitle.showHideView(isShow)
        placeDate.showHideView(isShow)
        txtHeaderTargetSavings.showHideView(isShow)
        placeTypeCurrencyTarget.showHideView(isShow)
        viewLineHeaderSavings.showHideView(isShow)
    }

    private infix fun View.showHideView(isShow: Boolean){
      visibility = if(isShow)View.VISIBLE else View.GONE
    }

    interface DialogCreateUserCallback {
        fun onSubmit(modelUser: ModelUser, modelSavings: ModelSavings)
    }
}

