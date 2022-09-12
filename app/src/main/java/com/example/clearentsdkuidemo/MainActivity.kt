package com.example.clearentsdkuidemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity.Companion.CLEARENT_RESULT_CODE
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity
import com.clearent.idtech.android.wrapper.ui.PaymentMethod
import com.clearent.idtech.android.wrapper.ui.ClearentAction
import com.clearent.idtech.android.wrapper.ui.ClearentAction.*
import com.clearent.idtech.android.wrapper.util.TAG
import com.example.clearentsdkuidemo.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Check if we started an action with the ClearentSDKActivity, it might take a while
    // to start the activity and we don't want duplicate actions.
    private var transactionOngoing = false

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        transactionOngoing = false

        // Check the result code of the action, if the activity returned RESULT_OK.
        if (result.resultCode == Activity.RESULT_OK)
            Timber.d(TAG, result.data?.getIntExtra(CLEARENT_RESULT_CODE, 0).toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSdkListener()
        setupClickListeners()
    }

    private fun setupSdkListener() = ClearentWrapper.setListener(ClearentDataSource)

    private fun setupClickListeners() {
        binding.apply {
            pairReaderButton.setOnClickListener {
                startSdkActivityForResult(Pairing(false))
            }
            readersListButton.setOnClickListener {
                startSdkActivityForResult(DevicesList())
            }
            startTransactionButton.setOnClickListener {
                startSdkActivityForResult(
                    Transaction(
                        chargeAmountEditText.text.toString().toDouble(),
                        paymentMethod = if (cardReaderSwitch.isChecked)
                            PaymentMethod.CARD_READER
                        else
                            PaymentMethod.MANUAL_ENTRY
                    )
                )
            }
        }
    }

    private fun startSdkActivityForResult(clearentAction: ClearentAction) {
        if (transactionOngoing)
            return

        transactionOngoing = true

        // Now we create an intent to start the ClearentSDKActivity
        val intent = Intent(applicationContext, ClearentSDKActivity::class.java)

        when (clearentAction) {
            // Pairing flow: we search for readers and then pair to one.
            is Pairing -> {
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_PAIR
                )
                // Set the hints option
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    clearentAction.showHints
                )
            }
            // Devices List flow: we look at previously paired readers and select one to pair with.
            // We can also see some settings of the readers.
            is DevicesList ->
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_DEVICES
                )
            // Transaction Flow: we start a transaction with an amount on a reader, or by manually
            // entering the card details.
            is Transaction -> {
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_TRANSACTION
                )
                // Set the amount for the transaction
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_AMOUNT_KEY,
                    clearentAction.amount
                )
                // Set the hints options in case we go through the pairing flow
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    clearentAction.showHints
                )
                // Set the signature option
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_SIGNATURE,
                    clearentAction.showSignature
                )
                // Set the payment method
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_PAYMENT_METHOD,
                    clearentAction.paymentMethod as? Parcelable
                )
            }
        }
        activityLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ClearentWrapper.removeListener()
    }
}