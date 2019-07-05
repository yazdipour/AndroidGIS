package ir.gfpishro.geosuiteandroidprivateusers.Controls;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressRingControl extends FrameLayout {
    private ProgressBar progressBar;
    private TextView progressText;
    private String savedTextMsg;

    public ProgressRingControl(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.control_progress_ring, this, true);
        progressText = view.findViewById(R.id.progressBarText);
        progressBar = view.findViewById(R.id.progressBarRing);
        checkInternet(context);
    }

    private void checkInternet(final Context context) {
        startSpinning();
        final Settings settings = SettingsHandler.getSettings(context);
        ApiHandler.getApi(settings.serverIp).ping().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                settings.isOnline = response.isSuccessful();
                setText(settings.getNetworkMessage());
                stopSpinning();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                SettingsHandler.setOnline(false, context);
                setText(settings.getNetworkMessage());
                stopSpinning();
            }
        });
    }

    public ProgressRingControl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressRingControl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setText(String text) {
        savedTextMsg = progressText.getText().toString();
        progressText.setText(text);
        switch (text.trim()) {
            case "انلاین":
            case "آنلاین":
                progressText.setTextColor(Color.GREEN);
                break;
            case "افلاین":
            case "آفلاین":
                progressText.setTextColor(Color.RED);
                break;
            default:
                progressText.setTextColor(Color.WHITE);
                break;
        }
    }

    public void setText(int textId) {
        setText(getResources().getString(textId));
    }

    public void setPreviousText() {
        setText(savedTextMsg);
    }

    public void stopSpinning() {
        progressBar.setVisibility(GONE);
    }

    public void startSpinning() {
        progressBar.setVisibility(VISIBLE);
    }
}
