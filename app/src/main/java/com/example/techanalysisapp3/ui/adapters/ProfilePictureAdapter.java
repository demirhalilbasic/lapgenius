package com.example.techanalysisapp3.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.techanalysisapp3.R;

import java.util.List;

public class ProfilePictureAdapter extends BaseAdapter {
    private Context context;
    private List<String> imageNames;
    private List<String> unlockedImages;
    private LayoutInflater inflater;

    public ProfilePictureAdapter(Context context, List<String> imageNames, List<String> unlockedImages) {
        this.context = context;
        this.imageNames = imageNames;
        this.unlockedImages = unlockedImages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageNames.size();
    }

    @Override
    public Object getItem(int position) {
        return imageNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_profile_picture, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.ivProfilePicture);
            holder.lockIcon = convertView.findViewById(R.id.ivLock);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imageName = imageNames.get(position);
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        holder.imageView.setImageResource(resId);

        // Show lock if image is not unlocked
        boolean isLocked = !unlockedImages.contains(imageName);
        holder.lockIcon.setVisibility(isLocked ? View.VISIBLE : View.GONE);

        if (isLocked) {
            holder.imageView.setAlpha(0.6f);
            holder.lockIcon.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setAlpha(1.0f);
            holder.lockIcon.setVisibility(View.GONE);
        }

        if (isLocked) {
            holder.imageView.setAlpha(0.5f);
            holder.lockIcon.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setAlpha(1.0f);
            holder.lockIcon.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        ImageView lockIcon;
    }
}