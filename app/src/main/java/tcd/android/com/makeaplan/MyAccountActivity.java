package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import tcd.android.com.makeaplan.Entities.User;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class MyAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        User user = (User) getIntent().getSerializableExtra(getString(R.string.my_account));
        ((TextView)findViewById(R.id.tv_account_name)).setText(user.getName());
        ((TextView)findViewById(R.id.tv_account_email)).setText(user.getEmail());
//        ((TextView)findViewById(R.id.tv_account_number_plans)).setText(user.getPlansList().size());
        ((TextView)findViewById(R.id.tv_account_id)).setText(user.getId());

        try {
            Bitmap bmp = encodeAsBitmap(user.getId() + "," + user.getName());
            ((ImageView) findViewById(R.id.iv_account_id_qr_code)).setImageBitmap(bmp);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.create_qr_code_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_plan_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.edit_menu:
                new AlertDialog.Builder(MyAccountActivity.this)
                        .setMessage(R.string.under_development_message)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        int width = 500;
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, width, width, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : Color.TRANSPARENT;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
        return bitmap;
    }
}