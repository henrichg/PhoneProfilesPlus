package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import me.drakeet.support.toast.ToastCompat;
import sk.henrichg.phoneprofilesplus.billing.BillingProvider;

public class DonationFragment extends Fragment {

    private List<SkuDetails> SKU_DETAILS = null;

    private ProgressBar mLoadingView;
    private TextView mErrorTextView;
    private Spinner mGoogleSpinner;
    private Button btGoogle;

    private BillingProvider mBillingProvider;

    // Debug tag, for logging
    private static final String TAG = "DonationFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);
    }

    /*
    public static DonationFragment newInstance() {
        return new DonationFragment();
    }
    */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PPApplication.logE(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.donation_fragment, container, true);

        mLoadingView = root.findViewById(R.id.donation_google_android_market_loading);

        /*
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) && (getActivity() != null)) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
            int accentColor = a.getColor(0, 0);
            a.recycle();
            mLoadingView.getIndeterminateDrawable().setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        }
        */

        mErrorTextView = root.findViewById(R.id.donation_google_android_market_error_textview);

        // choose donation amount
        mGoogleSpinner = root.findViewById(
                R.id.donation_google_android_market_spinner);
        switch (ApplicationPreferences.applicationTheme(getActivity(), true)) {
            case "dark":
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
            /*case "dlight":
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
                break;*/
            default:
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }

        btGoogle = root.findViewById(
                R.id.donation_google_android_market_donate_button);
        //btGoogle.setAllCaps(false);
        btGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donateGoogleOnClick(/*v*/);
            }
        });

        mBillingProvider = (BillingProvider) getActivity();

        /*
        Button paypalButton = root.findViewById(R.id.donation_paypal_donate_button);
        //paypalButton.setAllCaps(false);
        paypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorTextView.setVisibility(View.GONE);
                String url = "https://www.paypal.me/HenrichGron";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {
                }
            }
        });
        */

        return root;
    }

    /**
     * Enables or disables "please wait" screen.
     */
    public void setWaitScreen(boolean set) {
        if (mLoadingView != null)
            mLoadingView.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donateGoogleOnClick(/*View view*/) {
        final int index = mGoogleSpinner.getSelectedItemPosition();

        mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(index));
        //mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(index).getSku(), BillingClient.SkuType.INAPP);
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

                                if (getActivity() != null) {
                                    ArrayAdapter<CharSequence> adapter;
                                    adapter = new ArrayAdapter<CharSequence>(getActivity(),
                                            android.R.layout.simple_spinner_item, prices);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    mGoogleSpinner.setAdapter(adapter);
                                    btGoogle.setEnabled(true);
                                }
                            }
                            else {
                                displayAnErrorIfNeeded(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
                            }
                        }
                    }
                });
    }

    public void purchaseSuccessful(List<Purchase> purchases) {
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                String sku = purchase.getSku();
                for (SkuDetails skuDetail : SKU_DETAILS) {
                    if (skuDetail.getSku().equals(sku)) {
                        PPApplication.logE(TAG, "purchaseSuccessful - sku=" + sku);
                        PPApplication.logE(TAG, "purchaseSuccessful - currency=" + skuDetail.getPriceCurrencyCode());
                        PPApplication.logE(TAG, "purchaseSuccessful - priceS=" + skuDetail.getPrice());
                        PPApplication.logE(TAG, "purchaseSuccessful - priceMicros=" + skuDetail.getPriceAmountMicros());
                        PPApplication.logE(TAG, "purchaseSuccessful - price=" + skuDetail.getPriceAmountMicros() / 1000000.0);
                        Answers.getInstance().logPurchase(new PurchaseEvent()
                                .putItemPrice(BigDecimal.valueOf(skuDetail.getPriceAmountMicros() / 1000000.0))
                                .putCurrency(Currency.getInstance(skuDetail.getPriceCurrencyCode()))
                                .putItemName("Donation")
                                //.putItemType("Apparel")
                                .putItemId(sku)
                                .putSuccess(true));
                    }
                }
            }
        }

        if (getActivity() != null) {
            PPApplication.setDonationDonated(getActivity().getApplicationContext());
            ToastCompat.makeText(getActivity().getApplicationContext(), getString(R.string.donation_thanks_dialog), Toast.LENGTH_LONG).show();
        }
    }

    public void purchaseUnsuccessful(List<Purchase> purchases) {
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                String sku = purchase.getSku();
                for (SkuDetails skuDetail : SKU_DETAILS) {
                    if (skuDetail.getSku().equals(sku)) {
                        PPApplication.logE(TAG, "purchaseUnsuccessful - sku=" + sku);
                        PPApplication.logE(TAG, "purchaseUnsuccessful - currency=" + skuDetail.getPriceCurrencyCode());
                        PPApplication.logE(TAG, "purchaseUnsuccessful - priceS=" + skuDetail.getPrice());
                        PPApplication.logE(TAG, "purchaseUnsuccessful - priceMicros=" + skuDetail.getPriceAmountMicros());
                        PPApplication.logE(TAG, "purchaseUnsuccessful - price=" + skuDetail.getPriceAmountMicros() / 1000000.0);
                        Answers.getInstance().logPurchase(new PurchaseEvent()
                                .putItemPrice(BigDecimal.valueOf(skuDetail.getPriceAmountMicros() / 1000000.0))
                                .putCurrency(Currency.getInstance(skuDetail.getPriceCurrencyCode()))
                                .putItemName("Donation")
                                //.putItemType("Apparel")
                                .putItemId(sku)
                                .putSuccess(false));
                    }
                }
            }
        }
    }

    public void displayAnErrorIfNeeded(int response) {
        if (getActivity() == null || getActivity().isFinishing()) {
            PPApplication.logE(TAG, "No need to show an error - activity is finishing already");
            return;
        }

        setWaitScreen(false);
        if (mErrorTextView != null) {
            if (response != BillingClient.BillingResponse.OK) {
                mErrorTextView.setVisibility(View.VISIBLE);
                switch (response) {
                    case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                        PPApplication.logE(TAG, "error=BILLING_UNAVAILABLE");
                        mErrorTextView.setText(R.string.donation_google_android_market_not_supported);
                        break;
                    case BillingClient.BillingResponse.DEVELOPER_ERROR:
                        PPApplication.logE(TAG, "error=DEVELOPER_ERROR");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.ERROR:
                        PPApplication.logE(TAG, "error=ERROR");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                        PPApplication.logE(TAG, "error=FEATURE_NOT_SUPPORTED");
                        mErrorTextView.setText(R.string.donation_google_android_market_not_supported);
                        break;
                    case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                        PPApplication.logE(TAG, "error=ITEM_ALREADY_OWNED");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.ITEM_NOT_OWNED:
                        PPApplication.logE(TAG, "error=ITEM_NOT_OWNED");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                        PPApplication.logE(TAG, "error=ITEM_UNAVAILABLE");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
                        PPApplication.logE(TAG, "error=SERVICE_DISCONNECTED");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                        PPApplication.logE(TAG, "error=SERVICE_UNAVAILABLE");
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponse.USER_CANCELED:
                        PPApplication.logE(TAG, "error=USER_CANCELED");
                        mErrorTextView.setVisibility(View.GONE);
                        break;
                }
            } else
                mErrorTextView.setVisibility(View.GONE);
        }
    }

}
