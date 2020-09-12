package sk.henrichg.phoneprofilesplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class DonationPayPalAdapter extends BaseAdapter
{
    private final DonationPayPalFragment fragment;

    private final String[] prices;

    DonationPayPalAdapter(DonationPayPalFragment fragment, String[] prices)
    {
        this.fragment = fragment;
        this.prices = prices;
    }

//    public void release()
//    {
//        fragment = null;
//    }

    public int getCount()
    {
        return prices.length;
    }

    public Object getItem(int position)
    {
        return prices[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

    /*
    public int getItemId(Profile profile)
    {
        for (int i = 0; i < profileList.size(); i++)
        {
            if (profileList.get(i)._id == profile._id)
                return i;
        }
        return -1;
    }
    */
    /*
    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (!activityDataWrapper.profileListFilled)
            return -1;

        int pos = -1;

        for (int i = 0; i < activityDataWrapper.profileList.size(); i++)
        {
            ++pos;
            if (activityDataWrapper.profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }
    */
    static class ViewHolder {
          TextView price;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;

        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            vi = inflater.inflate(R.layout.donation_paypal_grid_item, parent, false);
            holder.price = vi.findViewById(R.id.donation_paypal_grid_item_price);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.price.setText(prices[position]);

        return vi;
    }

}
