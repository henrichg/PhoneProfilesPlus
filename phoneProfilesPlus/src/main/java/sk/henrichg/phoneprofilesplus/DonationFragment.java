package sk.henrichg.phoneprofilesplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import sk.henrichg.phoneprofilesplus.util.IabHelper;
import sk.henrichg.phoneprofilesplus.util.IabResult;
import sk.henrichg.phoneprofilesplus.util.Inventory;
import sk.henrichg.phoneprofilesplus.util.Purchase;

@SuppressWarnings("ConstantConditions")
public class DonationFragment extends Fragment {

    // List of SKUs.
    // http://developer.android.com/google/play/billing/billing_testing.html
    private static final String[] CATALOG_DEBUG = new String[]{"android.test.purchased",
            "android.test.canceled", "android.test.refunded", "android.test.item_unavailable"};
    private static final String[] CATALOG_RELEASE = new String[]{"phoneprofilesplus.donation.1",
            "phoneprofilesplus.donation.2", "phoneprofilesplus.donation.3", "phoneprofilesplus.donation.5", "phoneprofilesplus.donation.8",
            "phoneprofilesplus.donation.13", "phoneprofilesplus.donation.20"};
    private static final String[] CATALOG_RELEASE_VALUES = new String[]{"1 €", "2 €", "3 €", "5 €", "8 €", "13 €", "20 €"};

    private Spinner mGoogleSpinner;
    private Button btGoogle;

    private final boolean mDebug = false;

    private Context appContext;

    // The helper object
    private IabHelper mHelper;
    private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;

    // Debug tag, for logging
    private static final String TAG = "DonationFragment";

    public static DonationFragment newInstance() {
        return new DonationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.donation_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appContext = getActivity().getApplicationContext();

        // choose donation amount
        mGoogleSpinner = getActivity().findViewById(
                R.id.donation_google_android_market_spinner);

        btGoogle = getActivity().findViewById(
                R.id.donation_google_android_market_donate_button);
        btGoogle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                donateGoogleOnClick(/*v*/);
            }
        });

        //noinspection SpellCheckingInspection
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2OCdv8Qm0HysEnlWAiKhNoQ7Xg9OfhHdJD9ERxlFEpKsGeToSqVbhojq4IuhTz7/vFf6QkxG/mYOMp+RbxKhiDrWIfk49hcUNT0sUNfsTNK580mU0PKmEyHDADw52kPLAlG9or/bqc/R9xhMqbsLTBzahkk8ybYmTAASDo1ksivemeFB5cNjQO+9aIDr90z7MjXp5JMPfnsMeWs800a83IEKd0J34cUpqxruPFKHqJZdgk9fM85BbV1xhv9E0uSMQFjbhHcL9D7xnX5CK9OSkkawzGvtuHuKgz24+/ItDyKoJuCm2lZCIbBxeOZtbHqGKBNblqW4w3n2ioetlMXjowIDAQAB";

        // Create the helper, passing it our context and the public key to verify signatures with
        if (mDebug) Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(mDebug);

        // listener for get inventory
        mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    return;
                }

                for (int i = 0; i < CATALOG_RELEASE.length; i++) {
                    String price = inventory.getSkuDetails(CATALOG_RELEASE[i]).getPrice();
                    CATALOG_RELEASE_VALUES[i] = price;
                }

                // update the UI
                ArrayAdapter<CharSequence> adapter;
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, CATALOG_RELEASE_VALUES);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mGoogleSpinner.setAdapter(adapter);
                btGoogle.setEnabled(true);

            }
        };

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        if (mDebug) Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (mDebug) Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    openDialog(R.string.donation_google_android_market_not_supported_title, getString(R.string.donation_google_android_market_not_supported));
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                if (!mDebug) {
                    List<String> skuList = Arrays.asList(CATALOG_RELEASE);
                    try {
                        mHelper.queryInventoryAsync(true, skuList, null, mQueryFinishedListener);
                    } catch (Exception e) {
                        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                                android.R.layout.simple_spinner_item, CATALOG_RELEASE_VALUES);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mGoogleSpinner.setAdapter(adapter);
                        btGoogle.setEnabled(true);
                    }
                }
                else {
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                            android.R.layout.simple_spinner_item, CATALOG_DEBUG);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mGoogleSpinner.setAdapter(adapter);
                    btGoogle.setEnabled(true);
                }

            }
        });
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mDebug) Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            try {
                mHelper.disposeWhenFinished();
            } catch (Exception ignored) {}
            mHelper = null;
        }
    }

    /** Verifies the developer payload of a purchase. */
    private boolean verifyDeveloperPayload(Purchase p) {
        //String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mDebug) Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                //setWaitScreen(false);
                return;
            }
            //noinspection ConstantConditions
            if (!verifyDeveloperPayload(purchase)) {
                Log.e(TAG, "Error purchasing. Authenticity verification failed.");
                //setWaitScreen(false);
                return;
            }

            if (mDebug) Log.d(TAG, "Purchase successful.");

            // do not show donation notification after purchase
            PPApplication.setDonationNotificationCount(appContext, AboutApplicationJob.MAX_DONATION_NOTIFICATION_COUNT);

            // directly consume in-app purchase, so that people can donate multiple times
            try {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                // show thanks openDialog
                // show thanks openDialog
                openDialog(R.string.donation_thanks_dialog_title, getString(R.string.donation_thanks_dialog));
            } catch (IabHelper.IabAsyncInProgressException e) {
                Log.e(TAG, "Error donate. Another async operation in progress.");
                //setWaitScreen(false);
                //return;
            }

        }
    };

    // Called when consumption is complete
    private final IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mDebug) Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                if (mDebug) Log.d(TAG, "Consumption successful. Provisioning.");
                //mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                //saveData();
                //alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            }
            else {
                Log.e(TAG, "Error while consuming: " + result);
            }
            //updateUi();
            //setWaitScreen(false);
            if (mDebug) Log.d(TAG, "End consumption flow.");
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mDebug) Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the fragment result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if (mDebug) Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donateGoogleOnClick(/*View view*/) {
        final int index;
        index = mGoogleSpinner.getSelectedItemPosition();
        if (mDebug) Log.d(TAG, "selected item in spinner: " + index);

        try {
            if (mDebug) {
                // when debugging, choose android.test.x item
                mHelper.launchPurchaseFlow(getActivity(),
                        CATALOG_DEBUG[index], 0, mPurchaseFinishedListener, null);
            } else {
                mHelper.launchPurchaseFlow(getActivity(),
                        CATALOG_RELEASE[index], 0, mPurchaseFinishedListener, null);
            }
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.e(TAG, "Error launching purchase flow. Another async operation in progress.");
            //setWaitScreen(false);
        } catch (Exception e) {
            Log.e(TAG, "Error launching purchase flow.");
        }
    }

    /**
     * Open dialog
     */
    private void openDialog(int title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.donation_button_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.show();
    }}
