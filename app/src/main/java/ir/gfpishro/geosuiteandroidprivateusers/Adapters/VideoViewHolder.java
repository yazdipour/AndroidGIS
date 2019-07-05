package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class VideoViewHolder extends RecyclerView.ViewHolder {
    private final TextView tv;
    private final ImageView img;

    VideoViewHolder(View row) {
        super(row);
        tv = row.findViewById(R.id.video_name);
        img = row.findViewById(R.id.video_img);
    }

    public void bindData(final String path) {
        Uri uri = Uri.parse(path);
        itemView.setOnClickListener(v -> FileUtils.openVideoFile(itemView.getContext(), uri));
        tv.setText(new File(path).getName().replace(".mp4",""));
        img.setImageBitmap(Utils.getThumbnail(uri));
    }
}
