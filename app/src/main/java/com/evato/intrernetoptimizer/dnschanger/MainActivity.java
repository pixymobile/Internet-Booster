package com.evato.intrernetoptimizer.dnschanger;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.evato.intrernetoptimizer.DNSChangerApp;
import com.evato.intrernetoptimizer.PingUtility.Ping;
import com.evato.intrernetoptimizer.PingUtility.PingResult;
import com.evato.intrernetoptimizer.PingUtility.PingStats;
import com.evato.intrernetoptimizer.R;
import com.evato.intrernetoptimizer.menu.DrawerAdapter;
import com.evato.intrernetoptimizer.menu.DrawerItem;
import com.evato.intrernetoptimizer.menu.SimpleItem;
import com.evato.intrernetoptimizer.menu.SpaceItem;
import com.evato.intrernetoptimizer.model.DNSModel;
import com.evato.intrernetoptimizer.model.DNSModelJSON;
import com.evato.intrernetoptimizer.settings.SettingsActivity;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.hanks.htextview.scale.ScaleTextView;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hotchemi.android.rate.AppRate;

import static com.evato.intrernetoptimizer.Constant.BANNER_AD_UNIT;
import static com.evato.intrernetoptimizer.Constant.INTERSTITIAL_AD_UNIT;
import static com.evato.intrernetoptimizer.dnschanger.DNSPresenter.SERVICE_OPEN;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, IDNSView, DialogInterface.OnClickListener {

    private static final int POS_HOME = 0;
    private static final int POS_SETTING = 1;
    private static final int POS_HELP = 2;
    private static final int POS_EXIT = 4;
    private static final int REQUEST_CONNECT = 21;
    private static final Pattern IP_PATTERN = Patterns.IP_ADDRESS;
    @BindView(R.id.lightning_logo)
    ImageView lightning_iv;
    @BindView(R.id.menu_btn)
    RelativeLayout menu_btn;
    @BindView(R.id.dinamic_gradient)
    RelativeLayout lightning_gradient;
    @BindView(R.id.startButton)
    Button startButton;
    @Inject
    DNSPresenter presenter;
    @Inject
    Gson gson;
    @BindView(R.id.chooser)
    ConstraintLayout chosser_rl;
    @BindView(R.id.key_iv)
    ImageView key_iv;
    @BindView(R.id.net_type)
    TextView net_type;
    @BindView(R.id.status)
    TextView status_tv;
    @BindView(R.id.dns_name)
    ScaleTextView dns_name_tv;
    @BindView(R.id.scale_tv)
    ScaleTextView scale_tv;

    @BindView(R.id.firstDnsEdit)
    TextView firstDnsEdit;
    @BindView(R.id.secondDnsEdit)
    TextView secondDnsEdit;
    @BindView(R.id.ping_tv)
    ScaleTextView ping_tv;
    @BindView(R.id.orange_target)
    RelativeLayout orange_target;
    String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private InterstitialAd interstitialAd;
    private List<DNSModel> dnsList;
    private AnimationDrawable animationDrawable;
    @BindView(R.id.banner_container)
    RelativeLayout banner_container;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DaggerDNSComponent.builder().applicationComponent(DNSChangerApp.getApplicationComponent()).dNSModule(new DNSModule(this)).build().inject(this);
        ButterKnife.bind(this);
        Glide.with(this).load(R.drawable.app_logo).into(lightning_iv);
        initViews();

        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            showTaptarget();
        }

        slidingRootNav = new SlidingRootNavBuilder(this)
                // .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(true)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_HOME).setChecked(true),
                createItemFor(POS_SETTING),
                createItemFor(POS_HELP),
                new SpaceItem(48),
                createItemFor(POS_EXIT)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        getServiceStatus();
        parseIntent();
        NetwordDetect();

        animationDrawable = (AnimationDrawable) lightning_gradient.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);

        Animation slide_up_anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        startButton.startAnimation(slide_up_anim);


        AppRate.with(MainActivity.this)
                .setInstallDays(3)
                .setLaunchTimes(7)
                .setRemindInterval(7)
                .setShowLaterButton(true)
                .setShowNeverButton(false)
                //  .setDebug(true)
                .setTitle(getString(R.string.rate_title))
                .setMessage(getString(R.string.rate_text))
                .setTextLater(getString(R.string.rate_later))
                .setTextRateNow("Rate")
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(MainActivity.this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });


        adapter.setSelected(POS_HOME);

        adView = new AdView(this);
        adView.setAdUnitId(BANNER_AD_UNIT);
        banner_container.addView(adView);
      //  loadBanner();
    }

    private void parseIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            String dnsModelJSON = getIntent().getExtras().getString("dnsModel", "");
            if (!dnsModelJSON.isEmpty()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1903);
                if (dnsList == null)
                    getDNSItems();
                DNSModel model = gson.fromJson(dnsModelJSON, DNSModel.class);
                if (model.getName().equals(getString(R.string.custom_dns))) {
                    firstDnsEdit.setText(model.getFirstDns());
                    secondDnsEdit.setText(model.getSecondDns());
                } else {
                    for (int i = 0; i < dnsList.size(); i++) {
                        DNSModel dnsModel = dnsList.get(i);
                        if (dnsModel.getName().equals(model.getName())) {
                            onClick(null, i);
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scale_tv.animateText(getString(R.string.dns_starting));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startButton.performClick();
                    }
                });
            }
        }
    }

    private void getServiceStatus() {
        if (presenter.isWorking()) {
            serviceStarted();
            presenter.getServiceInfo();
        } else {
            serviceStopped();
        }
    }

    @Override
    public void changeStatus(int serviceStatus) {
        if (serviceStatus == SERVICE_OPEN) {
            serviceStarted();
            scale_tv.animateText(getString(R.string.service_started));
        } else {
            serviceStopped();
            scale_tv.animateText(getString(R.string.service_stoppped));
        }
    }


    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    public void setServiceInfo(DNSModel model) {
        dns_name_tv.animateText(model.getName());
        firstDnsEdit.setText(model.getFirstDns());
        secondDnsEdit.setText(model.getSecondDns());
    }

    private void serviceStopped() {
        stopAnimationLightning();
        ping_tv.animateText(" ");
        dns_name_tv.animateText(getString(R.string.app_name));
        status_tv.setText(getString(R.string.not_connected));
        status_tv.setTextColor(getResources().getColor(R.color.colorRed));
        startButton.setText(R.string.start);
        startButton.setBackgroundResource(R.drawable.button);
        firstDnsEdit.setText(" ");
        secondDnsEdit.setText(" ");
        chosser_rl.setClickable(true);
        key_iv.setImageResource(R.drawable.ic_action_downarrow);

    }

    private void serviceStarted() {
        starAnimationLightning();
        runPing();
        status_tv.setText(getString(R.string.connected));
        status_tv.setTextColor(getResources().getColor(R.color.colorGreen));
        startButton.setText(R.string.stop);
        startButton.setBackgroundResource(R.drawable.button_red);
        chosser_rl.setClickable(false);
        key_iv.setImageResource(R.drawable.ic_action_vpnlock);
    }


    private void initViews() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                            source.subSequence(start, end) +
                            destTxt.substring(dend);
                    if (!resultingTxt.matches("^\\d{1,3}(\\." +
                            "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i = 0; i < splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };
        firstDnsEdit.setFilters(filters);
        secondDnsEdit.setFilters(filters);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            presenter.startService(getDnsModel());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private DNSModel getDnsModel() {
        DNSModel dnsModel = new DNSModel();
        String first = firstDnsEdit.getText().toString();
        String second = secondDnsEdit.getText().toString();

        dnsModel.setName("CloudFlare");

        if (dnsList != null)
            for (DNSModel model : dnsList) {
                if (model.getFirstDns().equals(first) && model.getSecondDns().equals(second)) {
                    dnsModel.setName(model.getName());
                }
            }

        dnsModel.setFirstDns(first);
        dnsModel.setSecondDns(second);

        return dnsModel;
    }

    private boolean isValid() {
        boolean result = true;

        if (!IP_PATTERN.matcher(firstDnsEdit.getText()).matches()) {
            firstDnsEdit.setText("1.1.1.1");
            result = true;
        }

        if (!IP_PATTERN.matcher(secondDnsEdit.getText()).matches()) {
            firstDnsEdit.setText("1.1.1.1");
            result = true;
        }

        return result;
    }

    @OnClick({R.id.chooser, R.id.startButton, R.id.menu_btn})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.chooser:
                openChooser();
                break;

            case R.id.startButton:

                startDNS();

                break;

            case R.id.menu_btn:

                if (slidingRootNav.isMenuOpened()) {
                    slidingRootNav.closeMenu();
                } else {
                    slidingRootNav.openMenu();
                }
                break;
        }

    }


    private void openChooser() {
        CharSequence[] items = getDNSItems();

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).setItems(items, this).setTitle(R.string.choose_dns_server).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            //    showInterstitial();
            }
        }).create();

        ListView listView = dialog.getListView();

        listView.setDivider(ContextCompat.getDrawable(this, R.drawable.divider)); // set color
        listView.setDividerHeight(1);
        listView.setPadding(16, 16, 16, 16);
        dialog.show();


    }

    private CharSequence[] getDNSItems() {
        CharSequence[] result = new CharSequence[18];

        try {
            InputStream is = getAssets().open("dns_servers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            DNSModelJSON dnsModels = gson.fromJson(json, DNSModelJSON.class);
            dnsList = dnsModels.getModelList();
            int counter = 0;
            for (DNSModel dnsModel : dnsList) {
                result[counter] = (dnsModel.getName());
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void startDNS() {
        if (presenter.isWorking()) {
            presenter.stopService();
        } else if (isValid()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {

                Intent intent = VpnService.prepare(this);

                new CountDownTimer(2000, 9999) {
                    public void onTick(long millisUntillFinished) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //  ping_tv.setText("Connecting");
                                startButton.setText(getString(R.string.connecting));
                                startButton.setClickable(false);
                                scale_tv.animateText(getString(R.string.connecting));
                            }
                        });
                    }

                    public void onFinish() {
                        if (intent != null) {
                            startActivityForResult(intent, REQUEST_CONNECT);
                        } else {
                            onActivityResult(REQUEST_CONNECT, RESULT_OK, null);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startButton.setText(getString(R.string.stop));
                                startButton.setClickable(true);
                            }
                        });
                    }
                }.start();


            } else {
                scale_tv.animateText(getString(R.string.no_internet));
            }


        } else {

            scale_tv.animateText(getString(R.string.enter_valid_dns));

        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        DNSModel model = dnsList.get(which);
        firstDnsEdit.setText(model.getFirstDns());
        secondDnsEdit.setText(model.getSecondDns());
        dns_name_tv.animateText(model.getName());

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.textColorSecondary))
                .withTextTint(color(R.color.textColorPrimary))
                .withSelectedIconTint(color(R.color.colorAccent))
                .withSelectedTextTint(color(R.color.colorAccent));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void onItemSelected(int position) {
        switch (position) {
            case POS_HOME:
                slidingRootNav.closeMenu();
                break;

            case POS_SETTING:

                openSettingsActivity();

                break;

            case POS_HELP:

                slidingRootNav.closeMenu();

                new CountDownTimer(400, 9999) {
                    public void onTick(long millisUntillFinished) {
                    }

                    public void onFinish() {
                        showTaptarget();
                    }
                }.start();

                break;

            case POS_EXIT:

                finish();

                break;

            default:
                break;
        }
    }


    private void NetwordDetect() {
        boolean WIFI = false;
        boolean MOBILE = false;
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = CM.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))

                if (netInfo.isConnected())
                    WIFI = true;

            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))

                if (netInfo.isConnected())
                    MOBILE = true;
        }
        if (WIFI == true) {
            net_type.setText(getString(R.string.wifi_data));
        }
        if (MOBILE == true) {
            net_type.setText(getString(R.string.mobile_data));
        }
    }

    private void starAnimationLightning() {
        Animation fadein = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
        lightning_gradient.startAnimation(fadein);
        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                lightning_gradient.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationDrawable.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    private void stopAnimationLightning() {
        Animation fadeout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
        lightning_gradient.startAnimation(fadeout);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationDrawable.stop();
                lightning_gradient.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void runPing() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void doPing() throws Exception {
        String ipAddress = firstDnsEdit.getText().toString();
        // Perform a single synchronous ping
        PingResult pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();

        Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(1).doPing(new Ping.PingListener() {
            @Override
            public void onResult(PingResult pingResult) {
                appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));
            }

            @Override
            public void onFinished(PingStats pingStats) {
            }
        });

    }


    private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ping_tv.animateText(text);
            }
        });
    }

    public void showInterstitial() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    public void showTaptarget() {
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(orange_target, getString(R.string.taptarget1_title), getString(R.string.taptarget1_sub))
                                .transparentTarget(true)
                                // .targetCircleColor(android.R.color.darker_gray)
                                //.outerCircleColorInt(getResources().getColor(R.color.textColorPrimary))
                                .targetRadius(110)
                                .textColor(android.R.color.white)
                                .cancelable(false)
                                .id(1),

                        TapTarget.forView(startButton, getString(R.string.taptarget2_title), getString(R.string.taptarget2_sub))
                                .transparentTarget(true)
                                .targetCircleColor(android.R.color.darker_gray)
                                .targetRadius(140)
                                .textColor(android.R.color.white)
                                .cancelable(false)
                                .id(2),

                        TapTarget.forView(key_iv, getString(R.string.taptarget3_title), getString(R.string.taptarget3_sub))
                                .cancelable(false)
                                .id(3),

                        TapTarget.forView(ping_tv, getString(R.string.taptarget4_title), getString(R.string.taptarget4_sub))
                                .targetCircleColor(android.R.color.white)
                                .icon(getResources().getDrawable(R.drawable.ic_ping))

                                .cancelable(false)
                                .id(4),

                        TapTarget.forView(menu_btn, getString(R.string.taptarget5_title), getString(R.string.taptarget5_sub))
                                .transparentTarget(true)
                                .targetCircleColor(android.R.color.darker_gray)
                                .textColor(android.R.color.white)
                                .cancelable(false)
                                .id(5)

                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        // Toast.makeText(MainActivity.this, "not execute",Toast.LENGTH_LONG).show();
                        if (settings.getBoolean("my_first_time", true)) {
                            slidingRootNav.openMenu();
                            settings.edit().putBoolean("my_first_time", false).apply();
                            // Toast.makeText(MainActivity.this, "openmenu ",Toast.LENGTH_LONG).show();
                        } else {
                            showInterstitial();
                            // Toast.makeText(MainActivity.this, "show i ",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                    }
                });
        sequence.start();
    }

   //private void loadBanner() {
   //    AdRequest adRequest = new AdRequest.Builder().build();
   //    AdSize adSize = getAdSize();
   //    adView.setAdSize(adSize);
   //    adView.loadAd(adRequest);
   //}

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }


}
