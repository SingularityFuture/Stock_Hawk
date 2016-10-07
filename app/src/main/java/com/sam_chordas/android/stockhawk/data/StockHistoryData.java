package com.sam_chordas.android.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class StockHistoryData implements Parcelable {
    private ArrayList<SeriesData> historicalData;

    private class SeriesData {
        int date;
        float close;
        float high;
        float low;
        float open;
        long volume;

        public SeriesData(int date, float close, float high, float low, float open, long volume){
            this.date = date;
            this.close = close;
            this.high = high;
            this.low = low;
            this.open = open;
            this.volume = volume;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.historicalData);
    }

    public StockHistoryData() {
        historicalData = new ArrayList<SeriesData>();
    }

    public void addSeries(int date, float close, float high, float low, float open, long volume) {
        SeriesData data = new SeriesData(date, close, high, low, open, volume);
        historicalData.add(data);
    }

    public ArrayList<SeriesData> getHistoricalData() {
        return historicalData;
    }

    protected StockHistoryData(Parcel in) {
        this.historicalData = new ArrayList<SeriesData>();
        in.readList(this.historicalData, SeriesData.class.getClassLoader());
    }

    public static final Parcelable.Creator<StockHistoryData> CREATOR = new Parcelable.Creator<StockHistoryData>() {
        @Override
        public StockHistoryData createFromParcel(Parcel source) {
            return new StockHistoryData(source);
        }

        @Override
        public StockHistoryData[] newArray(int size) {
            return new StockHistoryData[size];
        }
    };
}