package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

public class StockWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{"Distinct " + QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.ISUP},
                        null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex("symbol")));
                views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex("bid_price")));
                if (data.getInt(data.getColumnIndex("is_up")) == 1){
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percent_change")));
                } else {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("change")));
                }

                final Intent fillInIntent = new Intent();
                Uri uri = QuoteProvider.Quotes.CONTENT_URI;
                fillInIntent.putExtra(StockDetailActivity.ARG_SYMBOL, data.getString(data.getColumnIndex("symbol")));
                views.setOnClickFillInIntent(R.id.stock_list_item, fillInIntent);


                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return getCount();
            }

/*            @Override
            public int getItemViewType(int position){
                  return position;
            }*/

            @Override
            public long getItemId(int position) {
                //if (data.moveToPosition(position))
                ///return data.getLong()
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }

}