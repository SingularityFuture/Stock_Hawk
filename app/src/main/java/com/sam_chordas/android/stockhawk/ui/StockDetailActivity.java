package com.sam_chordas.android.stockhawk.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.StockHistoryData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;


public class StockDetailActivity extends AppCompatActivity {
    public static final String ARG_SYMBOL = "ARG_SYMBOL";
    private String symbol;// = "GOOG";
    private StockHistoryData historyData;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        Resources r = getResources();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            symbol = bundle.getString(ARG_SYMBOL);
        }

        getSupportActionBar().setTitle(symbol);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setDescription(String.format(r.getString(R.string.chart_description), symbol));

        mChart.setTouchEnabled(false);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart.setBackgroundColor(Color.LTGRAY);

        new GetJSONpFromYAHOOFinanceTask().execute();

        mChart.animateX(2000);
    }
    private class GetJSONpFromYAHOOFinanceTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Resources r = getResources();
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(String.format(r.getString(R.string.history_url), symbol))
                        .build();
                Response responses = null;

                try {
                    responses = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String jsonData = responses.body().string();
                    jsonData = jsonData.replace(r.getString(R.string.callback_to_be_removed), "");
                    jsonData = jsonData.replace(")", "");

                    JsonParser parser = new JsonParser();
                    JsonObject object = parser.parse(jsonData).getAsJsonObject();

                    JsonArray openData = object.getAsJsonArray(r.getString(R.string.key_series));


                    ArrayList<Entry> openVals = new ArrayList<Entry>();
                    ArrayList<Entry> closeVals = new ArrayList<Entry>();

                    String openKey = r.getString(R.string.key_open);
                    String closeKey = r.getString(R.string.key_close);

                    for (int i = 0; i < openData.size(); i++){
                        JsonObject temp = openData.get(i).getAsJsonObject();
                        openVals.add(new Entry(i, temp.get(openKey).getAsFloat()));
                        closeVals.add(new Entry(i, temp.get(closeKey).getAsFloat()));
                    }

                    LineDataSet openSet, closeSet;
                    openSet = new LineDataSet(openVals, r.getString(R.string.legend_chart_open));
                    openSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    openSet.setColor(ColorTemplate.getHoloBlue());
                    openSet.setCircleColor(Color.WHITE);
                    openSet.setLineWidth(2f);
                    openSet.setCircleRadius(3f);
                    openSet.setFillAlpha(65);
                    openSet.setDrawCircleHole(false);

                    closeSet = new LineDataSet(closeVals, r.getString(R.string.legend_chart_close));
                    closeSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                    closeSet.setColor(Color.RED);
                    closeSet.setCircleColor(Color.WHITE);
                    closeSet.setLineWidth(2f);
                    closeSet.setCircleRadius(3f);
                    closeSet.setFillAlpha(65);
                    closeSet.setDrawCircleHole(false);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(openSet);
                    dataSets.add(closeSet);

                    final LineData lineData = new LineData(dataSets);
                    lineData.setValueTextColor(Color.WHITE);
                    lineData.setValueTextSize(9f);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mChart.setData(lineData);
                            Legend l = mChart.getLegend();
                            l.setForm(Legend.LegendForm.LINE);
                            l.setTextSize(11f);
                            l.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);

                            mChart.invalidate();
                        }
                    });
                } catch (IOException e){
                    e.printStackTrace();
                }
            } catch (UnknownError e){
                e.printStackTrace();
            }
            return null;
        }
    }
}