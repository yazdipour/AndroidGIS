package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.MapFile;
import ir.gfpishro.geosuiteandroidprivateusers.R;

class DownloaderViewHolder extends RecyclerView.ViewHolder {
    private ImageButton btn;
    private TextView tv_title, tv_subtitle, tv_progress, tv_date;
    private ProgressBar progressBar;

    DownloaderViewHolder(View itemView) {
        super(itemView);
        btn = itemView.findViewById(R.id.btn);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_subtitle = itemView.findViewById(R.id.tv_subtitle);
        tv_date = itemView.findViewById(R.id.tv_date);
        tv_progress = itemView.findViewById(R.id.tv_progress);
        progressBar = itemView.findViewById(R.id.pb_download);
    }

    public void bindData(final MapFile file, final DownloaderAdapter.Events listener, int position) {
        if (!file.needsUpdate()) btn.setVisibility(View.GONE);
        else {
            progressBar.setVisibility(View.VISIBLE);
            tv_progress.setVisibility(View.VISIBLE);
            btn.setOnClickListener(v -> listener.onBtnClicked(file, btn, progressBar, tv_progress));
        }
        tv_title.setText(String.format("نام فایل: %s", file.getName()));
        tv_subtitle.setText(String.format("سایز: %s", Utils.humanReadableByteCount(file.getSize(), true)));
        if (file.getDataModified() > 0) {
            String date = Utils.unixToPersianDate(file.getDataModified(), true, true);
            date = date.replaceAll("\\d\\d:\\d\\d", "").trim();
            tv_date.setText(String.format("تاریخ: %s", date));
        }
    }
}
