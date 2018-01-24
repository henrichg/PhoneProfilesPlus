package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import sk.henrichg.phoneprofilesplus.billing.BillingProvider;

@SuppressWarnings("ConstantConditions")
public class DonationFragment extends Fragment {

    private List<SkuDetails> SKU_DETAILS = null;

    private View mLoadingView;
    private TextView mErrorTextView;
    private Spinner mGoogleSpinner;
    private Button btGoogle;

    private BillingProvider mBillingProvider;

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

        mLoadingView = getActivity().findViewById(R.id.donation_google_android_market_loading);
        mErrorTextView = getActivity().findViewById(R.id.donation_google_android_market_error_textview);

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

        mBillingProvider = (BillingProvider) getActivity();
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donateGoogleOnClick(/*View view*/) {
        final int index = mGoogleSpinner.getSelectedItemPosition();

        mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(index).getSku(), BillingClient.SkuType.INAPP);
    }

    public void updateGUIAfterBillingConnected() {
        // Start querying for SKUs
        PPApplication.logE(TAG, "handleManagerAndUiReady");
        final List<String> inAppSkus = mBillingProvider.getBillingManager()
                .getSkus(/*!mDebug, */BillingClient.SkuType.INAPP);
        mBillingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP,
                inAppSkus,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode,
                                                     List<SkuDetails> skuDetailsList) {
                        PPApplication.logE(TAG, "onSkuDetailsResponse responseCode="+responseCode);

                        String[] prices = new String[]{"1 €", "2 €", "3 €", "5 €", "8 €", "13 €", "20 €"};

                        if (skuDetailsList != null)
                            PPApplication.logE(TAG, "onSkuDetailsResponse skuDetailsList="+skuDetailsList.size());
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            if (skuDetailsList.size() > 0) {
                                SKU_DETAILS = new ArrayList<>();
                                for (int i = 0; i < inAppSkus.size(); i++) {
                                    for (int j = 0; j < skuDetailsList.size(); j++) {
                                        if (skuDetailsList.get(j).getSku().equals(inAppSkus.get(i))) {
                                            PPApplication.logE(TAG, "Found sku: " + skuDetailsList.get(j));
                                            SKU_DETAILS.add(skuDetailsList.get(j));
                                            prices[i] = skuDetailsList.get(j).getPrice();
                                            break;
                                        }
                                    }
                                }

                                // update the UI
                                displayAnErrorIfNeeded(BillingClient.BillingResponse.OK);

                                ArrayAdapter<CharSequence> adapter;
                                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                                        android.R.layout.simple_spinner_item, prices);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mGoogleSpinner.setAdapter(adapter);
                                btGoogle.setEnabled(true);
                            }
                            else {
                                displayAnErrorIfNeeded(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
                            }
                        }
                    }
                });
    }

    public void displayAnErrorIfNeeded(int response) {
        if (getActivity() == null || getActivity().isFinishing()) {
            PPApplication.logE(TAG, "No need to show an error - activity is finishing already");
            return;
        }

        mLoadingView.setVisibility(View.GONE);
        if (response != BillingClient.BillingResponse.OK) {
            mErrorTextView.setVisibility(View.VISIBLE);
            switch (response) {
                case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                    mErrorTextView.setText("BILLING_UNAVAILABLE");
                    break;
                case BillingClient.BillingResponse.DEVELOPER_ERROR:
                    mErrorTextView.setText("DEVELOPER_ERROR");
                    break;
                case BillingClient.BillingResponse.ERROR:
                    mErrorTextView.setText("ERROR");
                    break;
                case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                    mErrorTextView.setText("FEATURE_NOT_SUPPORTED");
                    break;
                case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                    mErrorTextView.setText("ITEM_ALREADY_OWNED");
                    break;
                case BillingClient.BillingResponse.ITEM_NOT_OWNED:
                    mErrorTextView.setText("ITEM_NOT_OWNED");
                    break;
                case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                    mErrorTextView.setText("ITEM_UNAVAILABLE");
                    break;
                case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
                    mErrorTextView.setText("SERVICE_DISCONNECTED");
                    break;
                case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                    mErrorTextView.setText("SERVICE_UNAVAILABLE");
                    break;
                case BillingClient.BillingResponse.USER_CANCELED:
                    mErrorTextView.setText("USER_CANCELED");
                    break;
            }
        }
        else
            mErrorTextView.setVisibility(View.GONE);
    }

}
