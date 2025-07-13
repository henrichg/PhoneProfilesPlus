package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

//import me.drakeet.support.toast.ToastCompat;

public class DonationPayPalFragment extends Fragment {

    //private ProgressBar mLoadingView;
    //private TextView mErrorTextView;
    private GridView mPayPalGridView;
    //private AppCompatSpinner mGoogleSpinner;
    //private Button btGoogle;

    // Debug tag, for logging
    //private static final String TAG = "DonationPayPalFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        //noinspection deprecation
        setRetainInstance(true);
    }

    /*
    public static DonationFragment newInstance() {
        return new DonationFragment();
    }
    */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_donation_paypal, container, false);

        //mLoadingView = root.findViewById(R.id.donation_paypal_loading);

        //mErrorTextView = root.findViewById(R.id.donation_paypal_error_textview);

        // choose donation amount
        mPayPalGridView = root.findViewById(R.id.donation_paypal_grid);


//        mGoogleSpinner = root.findViewById(R.id.donation_google_android_market_spinner);
//        mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
/*        switch (ApplicationPreferences.applicationTheme(getActivity(), true)) {
            case "dark":
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
//            case "dlight":
//                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
//                break;
            default:
                mGoogleSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }*/

//        btGoogle = root.findViewById(
//                R.id.donation_google_android_market_donate_button);
//        btGoogle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                donateGoogleOnClick(-1);
//            }
//        });

        //mBillingProvider = (BillingProvider) getActivity();

        /*
        Button paypalButton = root.findViewById(R.id.donation_paypal_donate_button);
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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateGUIAfterBillingConnected();

        Button closeButton = view.findViewById(R.id.donation_paypal_activity_close);
        //noinspection DataFlowIssue
        closeButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
                //getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

//    /**
//     * Enables or disables "please wait" screen.
//     */
//    public void setWaitScreen(boolean set) {
//        if (mLoadingView != null)
//            mLoadingView.setVisibility(set ? View.VISIBLE : View.GONE);
//    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donatePayPalOnClick(/*int position*/) {
        //final int index = mGoogleSpinner.getSelectedItemPosition();

        //mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(position));
        //mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(index).getSku(), BillingClient.SkuType.INAPP);

        //String[] prices = new String[]{"1", "2", "3", "5", "8", "13", "20", "?"};

        //String url = "https://www.paypal.me/HenrichGron/" + prices[position] + "EUR";
        String url;
        //if (prices[position].equals("?"))
        //    url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U&currency_code=EUR";
        //else
        //    url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U&currency_code=EUR&amount=" + prices[position];
        url = PPApplication.PAYPAL_DONATION_URL;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            if ((getActivity() != null) && (!getActivity().isFinishing()))
                PPApplicationStatic.setDonationDonated(getActivity().getApplicationContext(), true);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    public void updateGUIAfterBillingConnected() {
        // Start querying for SKUs

        if (getActivity() != null) {
            //String[] prices = new String[]{"1 €", "2 €", "3 €", "5 €", "8 €", "13 €", "20 €", "? €"};
            String[] prices = new String[]{getString(R.string.donation_paypal_donate_button)};
            mPayPalGridView.setAdapter(new DonationPayPalAdapter(DonationPayPalFragment.this, prices));
            mPayPalGridView.setOnItemClickListener((parent, view, position, id) -> donatePayPalOnClick(/*position*/));
            mPayPalGridView.setEnabled(true);
        }
    }

    /*
    public void purchaseSuccessful(List<Purchase> purchases) {
        if (getActivity() != null) {
            PPApplication.setDonationDonated(getActivity().getApplicationContext());
            PPApplication.showToast(getActivity().getApplicationContext(), getString(R.string.donation_thanks_dialog), Toast.LENGTH_LONG);
        }
    }

    public void purchaseUnsuccessful(List<Purchase> purchases) {
    }

    public void displayAnErrorIfNeeded(int response) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        setWaitScreen(false);
        if (mErrorTextView != null) {
            if (response != BillingClient.BillingResponseCode.OK) {
                mErrorTextView.setVisibility(View.VISIBLE);
                switch (response) {
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                        mErrorTextView.setText(R.string.donation_google_android_market_not_supported);
                        break;
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                        mErrorTextView.setText(R.string.donation_google_error);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        mErrorTextView.setVisibility(View.GONE);
                        break;
                }
            } else
                mErrorTextView.setVisibility(View.GONE);
        }
    }
    */

}
