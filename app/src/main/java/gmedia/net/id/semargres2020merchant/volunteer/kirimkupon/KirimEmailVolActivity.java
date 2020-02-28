package gmedia.net.id.semargres2020merchant.volunteer.kirimkupon;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.semargres2020merchant.R;
import gmedia.net.id.semargres2020merchant.kategoriKupon.SettingKategoriKuponModel;
import gmedia.net.id.semargres2020merchant.util.ApiVolley;
import gmedia.net.id.semargres2020merchant.util.SessionManager;
import gmedia.net.id.semargres2020merchant.util.TwoItemModel;
import gmedia.net.id.semargres2020merchant.util.URL;
import gmedia.net.id.semargres2020merchant.volunteer.MerchantAdapter;
import gmedia.net.id.semargres2020merchant.volunteer.MerchantModel;
import gmedia.net.id.semargres2020merchant.volunteer.SessionMerchant;

import static gmedia.net.id.semargres2020merchant.volunteer.SessionMerchant.SP_ID_MERCHANT_EMAIL;
import static gmedia.net.id.semargres2020merchant.volunteer.SessionMerchant.SP_ID_MERCHANT_SMS;
import static gmedia.net.id.semargres2020merchant.volunteer.SessionMerchant.SP_NAMA_MERCHANT_EMAIL;
import static gmedia.net.id.semargres2020merchant.volunteer.SessionMerchant.SP_NAMA_MERCHANT_SMS;

public class KirimEmailVolActivity extends AppCompatActivity implements MerchantAdapter.MerchantAdapterCallback {

    Toolbar toolbar;
    ScrollView svContainer;

    public static Dialog dgMerchant;
    private RecyclerView rvMerchant;
    MerchantAdapter merchantAdapter;
    private List<MerchantModel> merchantModels = new ArrayList<>();
    private List<TwoItemModel> caraBayarList = new ArrayList<>();
    private List<SettingKategoriKuponModel> kategoriKupon = new ArrayList<>();
    private SessionMerchant sessionMerchant;
    private String keyword_merchant="";
    private int start =0, count=20;

    private LinearLayout llSelectMerchant;
    private TextView tvMerchantEmail;
    private EditText edtNama, edtAlamat, edtNik, edtEmail, edtNominal;
    private Button btnKirim;
    private Spinner spCaraBayar, spKategori;

    String id_merchant="0";
    String cara_bayar="", kategori_kupon="";

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_email_vol);
        sessionMerchant = new SessionMerchant(this);
        session = new SessionManager(this);
        getCaraBayarData();
        initUi();
    }

    private void initUi(){
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.statusbar));
        }

        toolbar = findViewById(R.id.toolbar);
        svContainer = findViewById(R.id.sv_container);
        svContainer.setVerticalScrollBarEnabled(false);
        svContainer.setHorizontalScrollBarEnabled(false);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Kalkulator E-Kupon By Email");
        }

        llSelectMerchant = findViewById(R.id.ll_select_merchant);
        llSelectMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionMerchant = new SessionMerchant(getApplicationContext());
                showDialogMerchant();
            }
        });
        tvMerchantEmail = findViewById(R.id.tv_merchant);
        tvMerchantEmail.setText(sessionMerchant.getSpNamaEmail());

        edtNama = findViewById(R.id.edt_nama);
        edtAlamat = findViewById(R.id.edt_alamat);
        edtEmail = findViewById(R.id.edt_email);
        edtNik = findViewById(R.id.edt_nik);
        edtNominal = findViewById(R.id.edt_nominal);
        spCaraBayar = (Spinner) findViewById(R.id.sp_cara_bayar);
        spKategori = (Spinner) findViewById(R.id.sp_kategori);
        id_merchant = sessionMerchant.getSpIdSms();
        getKategoriByMerchant(id_merchant);
        btnKirim = findViewById(R.id.btn_send);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllMerchant(){
        JSONObject params = new JSONObject();
        try {
            params.put("keyword",keyword_merchant);
            params.put("start",start);
            params.put("count",count);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new ApiVolley(KirimEmailVolActivity.this, params, "POST", URL.urlAllMerchant, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject object = new JSONObject(result);

                    String status = object.getJSONObject("metadata").getString("status");
                    String message = object.getJSONObject("metadata").getString("message");
                    if (status.equals("200")) {
                        JSONArray arr = object.getJSONArray("response");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject isi = arr.getJSONObject(i);
                            merchantModels.add(new MerchantModel(
                                    isi.getString("id"),
                                    isi.getString("nama"),
                                    isi.getString("alamat"),
                                    isi.getString("email"),
                                    isi.getString("deskripsi"),
                                    isi.getString("kategori"),
                                    isi.getString("latitude"),
                                    isi.getString("longitude")
                            ));
                        }
                        merchantAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDialogMerchant(){
        merchantModels.clear();
        keyword_merchant="";
        dgMerchant = new Dialog(KirimEmailVolActivity.this);
        dgMerchant.setContentView(R.layout.popup_merchant);
        dgMerchant.setCancelable(false);
        RelativeLayout rvCancel = dgMerchant.findViewById(R.id.rv_cancel);
        rvMerchant = dgMerchant.findViewById(R.id.rv_merchant);
        setupListMerchantSms();
        setupListScrollListenerMerchantSms();
        start =0;
        count =20;
        getAllMerchant();

        final EditText edtSearchMerchant = dgMerchant.findViewById(R.id.edt_search_merchant);
        edtSearchMerchant.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(edtSearchMerchant.getWindowToken(), 0);
                    keyword_merchant = edtSearchMerchant.getText().toString().trim();
                    start =0;
                    count =20;
                    merchantModels.clear();
                    getAllMerchant();
                    merchantAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        edtSearchMerchant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(edtSearchMerchant.getText().toString().length() == 0) {
                    keyword_merchant="";
                    start=0;
                    count=20;
                    getAllMerchant();
                }
            }
        });

        rvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dgMerchant.dismiss();
            }
        });
        dgMerchant.show();
    }

    private void setupListMerchantSms() {
        merchantAdapter = new MerchantAdapter(KirimEmailVolActivity.this, merchantModels,this);
        rvMerchant.setLayoutManager(new LinearLayoutManager(KirimEmailVolActivity.this));
        rvMerchant.setAdapter(merchantAdapter);
    }

    private void setupListScrollListenerMerchantSms() {
        rvMerchant.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (! recyclerView.canScrollVertically(1)) {
                    start += count;
                    getAllMerchant();
                }
            }
        });
    }

    private void getCaraBayarData() {

        new ApiVolley(KirimEmailVolActivity.this, new JSONObject(), "GET", URL.urlCaraBayar, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject object = new JSONObject(result);
                    String status = object.getJSONObject("metadata").getString("status");

                    if (status.equals("200")) {
                        JSONArray jArray = object.getJSONArray("response");
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jo = jArray.getJSONObject(i);
                            caraBayarList.add(new TwoItemModel(jo.getString("value"), jo.getString("text")));
                        }
                        spCaraBayar.setAdapter(new ArrayAdapter<>(KirimEmailVolActivity.this, R.layout.layout_simple_list, caraBayarList));
                    } else {
                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getKategoriByMerchant(String id_merchant){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id_merchant",id_merchant);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new ApiVolley(KirimEmailVolActivity.this, jsonObject, "POST", URL.urlKategoriKuponByMerchant, "", "", 0, new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                kategoriKupon.clear();
                try {
                    JSONObject object = new JSONObject(result);
                    String status = object.getJSONObject("metadata").getString("status");

                    Log.d(">>>>>", String.valueOf(object));

                    if (status.equals("200")) {
                        JSONArray jArray = object.getJSONObject("response").getJSONArray("kategori");
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jo = jArray.getJSONObject(i);
                            kategoriKupon.add(new SettingKategoriKuponModel(jo.getString("id"), jo.getString("nama"),jo.getString("nominal")));
                        }
                        spKategori.setAdapter(new ArrayAdapter<>(KirimEmailVolActivity.this, R.layout.layout_simple_list, kategoriKupon));
                    }
//                    else {
//                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRowMerchantAdapterClicked(String id_m, String nama) {
        spKategori.setSelection(-1);
        id_merchant = id_m;
        getKategoriByMerchant(id_merchant);
        sessionMerchant.saveSPString(SP_ID_MERCHANT_EMAIL, id_merchant);
        sessionMerchant.saveSPString(SP_NAMA_MERCHANT_EMAIL, nama);
        tvMerchantEmail.setText(sessionMerchant.getSpNamaEmail());
        dgMerchant.dismiss();
    }
}
