package cn.aura.feimayun.vhall.util.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import cn.aura.feimayun.R;

public class EmojiAdapter extends ArrayAdapter<String> {

    private int itemWidth;

    public EmojiAdapter(Context context, int textViewResourceId, List<String> objects, int itemWidth) {
        super(context, textViewResourceId, objects);
        this.itemWidth = itemWidth;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.vhall_emoji_row, null);
        }
        ImageView imageView = convertView.findViewById(R.id.iv_emoji);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(itemWidth, itemWidth);
        imageView.setLayoutParams(params);
        String filename = getItem(position);
        int resId = getContext().getResources().getIdentifier(filename, "mipmap", getContext().getPackageName());
        imageView.setImageResource(resId);
        return convertView;
    }
}

