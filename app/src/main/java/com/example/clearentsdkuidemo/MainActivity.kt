package com.example.clearentsdkuidemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.clearent.idtech.android.wrapper.ui.ClearentSDKUi
import com.clearent.idtech.android.wrapper.ui.ClearentSDKUi.Companion.SDK_WRAPPER_RESULT_CODE
import com.clearent.idtech.android.wrapper.ui.PaymentMethod
import com.clearent.idtech.android.wrapper.ui.SDKWrapperAction
import com.clearent.idtech.android.wrapper.ui.SDKWrapperAction.*
import com.clearent.idtech.android.wrapper.util.TAG
import com.example.clearentsdkuidemo.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Check if we started an action with the ClearentSDKUi, it might take a while
    // to start the activity and we don't want duplicate actions.
    private var transactionOngoing = false

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        transactionOngoing = false

        // Check the result code of the action, if the activity returned RESULT_OK.
        if (result.resultCode == Activity.RESULT_OK)
            Timber.d(TAG, result.data?.getIntExtra(SDK_WRAPPER_RESULT_CODE, 0).toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSdkListener()
        setupClickListeners()
    }

    private fun setupSdkListener() = SDKWrapper.setListener(ClearentDataSource)

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

    private fun startSdkActivityForResult(sdkWrapperAction: SDKWrapperAction) {
        if (transactionOngoing)
            return

        transactionOngoing = true

        // ↓ this could be a method inside the ClearentSDKUi

        // Now we create an intent to start the ClearentSDKUi activity
        val intent = Intent(applicationContext, ClearentSDKUi::class.java)

        when (sdkWrapperAction) {
            // Pairing flow: we search for readers and then pair to one.
            is Pairing -> {
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
                    ClearentSDKUi.SDK_WRAPPER_ACTION_PAIR
                )
                // Set the hints option
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_SHOW_HINTS,
                    sdkWrapperAction.showHints
                )
            }
            // Devices List flow: we look at previously paired readers and select one to pair with.
            // We can also see some settings of the readers.
            is DevicesList ->
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
                    ClearentSDKUi.SDK_WRAPPER_ACTION_DEVICES
                )
            // Transaction Flow: we start a transaction with an amount on a reader, or by manually
            // entering the card details.
            is Transaction -> {
                // Set the action for the ui
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_ACTION_KEY,
                    ClearentSDKUi.SDK_WRAPPER_ACTION_TRANSACTION
                )
                // Set the amount for the transaction
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_AMOUNT_KEY,
                    sdkWrapperAction.amount
                )
                // Set the hints options in case we go through the pairing flow
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_SHOW_HINTS,
                    sdkWrapperAction.showHints
                )
                // Set the signature option
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_SHOW_SIGNATURE,
                    sdkWrapperAction.showSignature
                )
                // Set the payment method
                intent.putExtra(
                    ClearentSDKUi.SDK_WRAPPER_PAYMENT_METHOD,
                    sdkWrapperAction.paymentMethod as? Parcelable
                )
            }
        }
        activityLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        SDKWrapper.removeListener()
    }
}