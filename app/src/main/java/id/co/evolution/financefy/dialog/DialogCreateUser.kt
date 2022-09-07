package id.co.evolution.financefy.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import id.co.evolution.financefy.R
import id.co.evolution.financefy.helper.Tools
import id.co.evolution.financefy.model.ModelSavings
import id.co.evolution.financefy.model.ModelUser
import id.co.evolution.financefy.repository.SavingsRepository
import id.co.evolution.financefy.repository.UserRepository
import id.co.evolution.financefy.repository.UserRepository.InputUpdateUser
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class DialogCreateUser(
    val context: Context,
    val inflater: LayoutInflater,
    val userRepository: UserRepository

) : View.OnClickListener {

    val dialog: Dialog = Dialog(context)
    val dialogView: View = inflater.inflate(R.layout.dialog_create_user, null)


    lateinit var savingsRepository: SavingsRepository
    lateinit var dialogCreateUserCallback: DialogCreateUserCallback
    var jumlah = ""
    var type = ""
    var category = ""
    var date_target = ""
    var arrayCategoryFromResource: Int = 0
    var isAddAccount = false
    private lateinit var arrayCategory: Array<String>
    private lateinit var dialogCalculator: DialogCalculator
    private var user: ModelUser = ModelUser()
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
    private var txtType: TextView
    private var txtDate: TextView
    private var txtCategory: TextView
    private var btnSubmit: CardView
    private var btnCalculator: Button
    private var btnClose: ImageView

    init {
        dialog.setContentView(dialogView)
        etName = findViewById(R.id.et_name)
        tilName = findViewById(R.id.til_name)
        tilTarget = findViewById(R.id.til_target_value)
        etTarget = findViewById(R.id.et_target_value)
        tilTitle = findViewById(R.id.til_title)
        etTitle = findViewById(R.id.et_title)
        placeTarget = findViewById(R.id.place_target_value)
        placeDate = findViewById(R.id.place_date)
        txtType = findViewById(R.id.txt_type)
        txtCategory = findViewById(R.id.txt_category)
        txtDate = findViewById(R.id.txt_date)
        btnSubmit = findViewById(R.id.cv_submit)
        btnClose = findViewById(R.id.img_close)
        btnCalculator = findViewById(R.id.btn_calculator)
    }

    constructor (
        context: Context,
        inflater: LayoutInflater,
        userRepository: UserRepository,
        userUpdate: ModelUser
    ) : this(context, inflater, userRepository) {
        this.user = userUpdate
    }

    constructor (
        context: Context,
        inflater: LayoutInflater,
        userRepository: UserRepository,
        savingsRepository: SavingsRepository,
        fragmentManager: FragmentManager,
        userUpdate: ModelUser,
        dialogCreateUserCallback: DialogCreateUserCallback
    ) : this(context, inflater, userRepository) {
        this.dialogCreateUserCallback = dialogCreateUserCallback
        this.fragmentManager = fragmentManager
        this.savingsRepository = savingsRepository
        this.user = userUpdate
    }

    fun showDialogCreateUser(isAddAccount: Boolean) {
        this.isAddAccount = isAddAccount

        if (!isAddAccount) {
            user.let {
                etName.setText(it.name)
                txtType.text = it.type
                txtCategory.text = it.category
            }
        }

        etTarget addTextChangedListener object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != jumlah) {
                    etTarget.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[Rp,.]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val localeID = Locale("in", "ID")
                        var formatted = NumberFormat.getCurrencyInstance(localeID).format(parsed)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            formatted = formatted.replace(",00".toRegex(), "")
                        }
                        jumlah = formatted
                        etTarget.setText(formatted)
                        etTarget.setSelection(formatted.length)
                    }
                    etTarget.addTextChangedListener(this)
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }

        txtType onClick this
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
            R.id.cv_submit -> {
                val messageError: String
                val user = ModelUser()
                val savings = ModelSavings()

                try {
                    if (etName.text.toString().isEmpty()) {
                        messageError = "Silahkan input target menabung terlebih dahulu"
                        tilName.error = messageError
                        throw Exception(messageError)
                    } else {
                        tilName.error = null
                    }

                    if (type.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Silahkan Pilih tipe terlebih dahulu",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }






                    if (isAddAccount) {
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
                                Toast.makeText(context, messageError, Toast.LENGTH_SHORT).show()
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
                        }
                        savings.title = etTitle.text.toString()
                        savings.date_target = date_target
                        savings.processValue = 0
                        savings.targetValue = Tools.replaceCurrencyStringToLong(jumlah)
                    }else{
                        user.id = this.user.id
                    }

                    user.name = etName.text.toString()
                    user.type = type
                    user.category = category


                    dialogCreateUserCallback.onSubmit(user, savings)

                    dialog.dismiss()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.btn_calculator -> {
                dialogCalculator = DialogCalculator(context, inflater) { result: String? ->
                    jumlah = Tools.convertToCurrency(result)
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
                txtDate.setText(Tools.getFormattedDateSimple(date_ship_milis))
                date_target = Tools.getFormattedDateSimple(date_ship_milis)

            }
        datePickerDialog.setYearRange(
            cur_calendar.get(Calendar.YEAR),
            cur_calendar.get(Calendar.YEAR)
        )
        datePickerDialog.maxDate = cur_calendar
        datePickerDialog.accentColor = context.resources.getColor(R.color.colorPrimary)

        datePickerDialog.show(fragmentManager, "PickerDialog")
    }

    private fun showDialogCalculator() {
        dialogCalculator =
            DialogCalculator(context, inflater) { result: String? ->
                jumlah = Tools.convertToCurrency(result)
                etTarget.setText(jumlah)
            }
        dialogCalculator.show()
    }

    private fun showDialogCategory() {
        val dialogFinance = DialogFinance(context, object : DialogFinance.DialogFinanceCallback {
            override fun onSubmit(index: Int, result: String) {
                category = result
                if (isAddAccount) setVisibilityPlaceSavings()
                if (category.isNotEmpty()) txtCategory.text = category
            }
        })

        dialogFinance.showDialogCategory(arrayCategoryFromResource)
    }

    private fun showDialogType() {
        val dialogFinance = DialogFinance(context, object : DialogFinance.DialogFinanceCallback {
            override fun onSubmit(index: Int, result: String) {
                type = result
                arrayCategoryFromResource = R.array.category_user
                arrayCategory = context.resources.getStringArray(arrayCategoryFromResource)

                //TODO ketika memilih tipe usaha maka category tidak bisa di pilih
                //TODO Dan di set default false
                category = if (type.equals(
                        context.getString(R.string.usaha),
                        ignoreCase = true
                    )
                ) arrayCategory[1] else arrayCategory[0]

                if (isAddAccount) setVisibilityPlaceSavings()

                txtCategory.text = category
                txtCategory.isClickable = type.equals(
                    context.getString(R.string.pribadi),
                    ignoreCase = true
                )


                txtType.text = type
            }
        })
        dialogFinance.showDialogType(R.array.type_user)
    }

    fun setVisibilityPlaceSavings() {
        placeTarget.visibility = getVisibilityPlaceSavings()
        tilTitle.visibility = getVisibilityPlaceSavings()
        placeDate.visibility = getVisibilityPlaceSavings()
    }

    fun getVisibilityPlaceSavings(): Int {
        return if (category.equals(
                context.getString(R.string.menabung),
                ignoreCase = true
            )
        ) View.VISIBLE else View.GONE
    }

    interface DialogCreateUserCallback {
        fun onSubmit(modelUser: ModelUser, modelSavings: ModelSavings)
    }
}

