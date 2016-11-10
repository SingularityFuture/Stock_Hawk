package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.security.AccessController.getContext;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(Context context, String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(context.getString(R.string.query));
        int count = Integer.parseInt(jsonObject.getString(context.getString(R.string.count)));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(context.getString(R.string.results))
                  .getJSONObject(context.getString(R.string.quote));
          if (jsonObject.isNull(context.getString(R.string.Change))) { // Check that there was a stock found with that symbol.
              //Toast.makeText(StockIntentService.this, "No stock found", Toast.LENGTH_LONG).show();
              return batchOperations;
          }
          batchOperations.add(buildBatchOperation(context,jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject(context.getString(R.string.results)).getJSONArray(context.getString(R.string.quote));

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(context, jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, context.getString(R.string.string_json_fail) + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(Context context, String bidPrice){
    bidPrice = String.format(context.getString(R.string.format), Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(Context context, String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format(context.getString(R.string.format), round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(Context context, JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString(context.getString(R.string.Change));
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(context.getString(R.string.symbol)));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(context, jsonObject.getString(context.getString(R.string.Bid))));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(context,
          jsonObject.getString(context.getString(R.string.ChangeinPercent)), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(context, change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }
}
