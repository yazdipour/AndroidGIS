package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;

import ir.gfpishro.geosuiteandroidprivateusers.Forms.RiserActivity;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class ScannerActivity extends AppCompatActivity {
    private CodeScanner codeScanner;

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        final CodeScannerView scannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this, scannerView);
        codeScanner.setFormats(CodeScanner.ONE_DIMENSIONAL_FORMATS);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> new AlertDialog
                .Builder(ScannerActivity.this)
                .setTitle("کد " + result.getText())
                .setCancelable(true)
                .setPositiveButton("تایید", (dialog, which) -> {
                    int senderId = getIntent().getIntExtra("sender", 0);
                    if (senderId == RiserActivity.REQUEST_CODE) {
                        Intent i = new Intent();
                        i.putExtra("barcode", result.getText());
                        setResult(RiserActivity.REQUEST_CODE, i);
                        finish();
                    } else {
                        Intent i = new Intent(ScannerActivity.this, SearchActivity.class);
                        i.putExtra("barcode", result.getText());
                        startActivityForResult(i, SearchActivity.REQUEST_CODE);
                    }
                })
                .setNegativeButton("مجدد", null)
                .create()
                .show()));
        scannerView.setOnClickListener(view -> codeScanner.startPreview());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }
}
