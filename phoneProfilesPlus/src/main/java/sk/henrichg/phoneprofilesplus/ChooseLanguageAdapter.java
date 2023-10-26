package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

class ChooseLanguageAdapter extends BaseAdapter {

    final String[] languageNameChoices;

    private final ChooseLanguageDialog dialog;

    private final Context context;

    //private final LayoutInflater inflater;

    ChooseLanguageAdapter(ChooseLanguageDialog dialog, Context c, String[] languageNameChoices)
    {
        this.dialog = dialog;
        context = c;

        this.languageNameChoices = languageNameChoices;

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return languageNameChoices.length;
    }

    public Object getItem(int position) {
        return languageNameChoices[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChooseLanguageViewHolder holder;

        View vi = convertView;

        if (convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.listitem_choose_language, parent, false);

            holder = new ChooseLanguageViewHolder();
            holder.radioButton = vi.findViewById(R.id.choose_language_dlg_item_radio_button);
            holder.languageLabel = vi.findViewById(R.id.choose_language_dlg_item_label);
            vi.setTag(holder);
        }
        else
        {
            holder = (ChooseLanguageViewHolder)vi.getTag();
        }

        if ((position >= 0) && (position < getCount())) {
            holder.languageLabel.setText(languageNameChoices[position]);

            holder.radioButton.setTag(position);
            holder.radioButton.setChecked(position == dialog.activity.selectedLanguage);
            holder.radioButton.setOnClickListener(v -> {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(true);
                dialog.doOnItemSelected((Integer)rb.getTag());
            });
        }

        return vi;
    }

}
