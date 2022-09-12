package com.example.clearentsdkuidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;

import com.clearent.idtech.android.wrapper.ClearentDataSource;
import com.clearent.idtech.android.wrapper.ClearentWrapper;
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity;
import com.clearent.idtech.android.wrapper.ui.PaymentMethod;
import com.clearent.idtech.android.wrapper.ui.ClearentAction;
import com.example.clearentsdkuidemo.databinding.ActivityMainBinding;

import java.util.Objects;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    transactionOngoing = false;

                    // Check the result code of the action, if the activity returned RESULT_OK.
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        final Intent data = result.getData();

                        // Should never be null
                        if (data == null)
                            return;

                        Timber.d(String.valueOf(data.getIntExtra(ClearentSDKActivity.CLEARENT_RESULT_CODE, 0)));
                    }
                }
            }
    );

    private ActivityMainBinding binding;
    private Boolean transactionOngoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSdkListener();
        setupClickListeners();
    }

    private void setupSdkListener() {
        ClearentWrapper.INSTANCE.setListener(ClearentDataSource.INSTANCE);
    }

    private void setupClickListeners() {
        binding.pairReaderButton.setOnClickListener(
                view -> startSdkActivityForResult(new ClearentAction.Pairing())
        );
        binding.readersListButton.setOnClickListener(
                view -> startSdkActivityForResult(new ClearentAction.DevicesList())
        );
        binding.startTransactionButton.setOnClickListener(
                view -> startSdkActivityForResult(
                        new ClearentAction.Transaction(
                                Double.parseDouble(Objects.requireNonNull(binding.chargeAmountEditText.getText()).toString()),
                                false,
                                true,
                                binding.cardReaderSwitch.isChecked() ? PaymentMethod.CARD_READER : PaymentMethod.MANUAL_ENTRY
                        )
                )
        );
    }

    private void startSdkActivityForResult(ClearentAction clearentAction) {
        if (transactionOngoing)
            return;

        transactionOngoing = true;

        // ↓ this could be a method inside the ClearentSDKActivity

        // Now we create an intent to start the ClearentSDKActivity activity
        final Intent intent = new Intent(this, ClearentSDKActivity.class);

        // Pairing flow: we search for readers and then pair to one.
        if (clearentAction instanceof ClearentAction.Pairing) {
            final ClearentAction.Pairing action = (ClearentAction.Pairing) clearentAction;
            // Set the action for the ui
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_PAIR
            );
            // Set the hints option
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    action.getShowHints()
            );
        }
        // Devices List flow: we look at previously paired readers and select one to pair with.
        // We can also see some settings of the readers.
        else if (clearentAction instanceof ClearentAction.DevicesList)
            // Set the action for the ui
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_DEVICES
            );
            // Transaction Flow: we start a transaction with an amount on a reader, or by manually
            // entering the card details.
        else if (clearentAction instanceof ClearentAction.Transaction) {
            final ClearentAction.Transaction action = (ClearentAction.Transaction) clearentAction;
            // Set the action for the ui
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_TRANSACTION
            );
            // Set the amount for the transaction
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_AMOUNT_KEY,
                    action.getAmount()
            );
            // Set the hints options in case we go through the pairing flow
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    action.getShowHints()
            );
            // Set the signature option
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_SIGNATURE,
                    action.getShowSignature()
            );
            // Set the payment method
            intent.putExtra(
                    ClearentSDKActivity.CLEARENT_PAYMENT_METHOD,
                    (Parcelable) action.getPaymentMethod()
            );
        }
        activityLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClearentWrapper.INSTANCE.removeListener();
    }
}