package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  private int result;

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), getString(R.string.intent_service));
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra(getString(R.string.tag)).equals(getString(R.string.add))){
      args.putString(getString(R.string.symbol), intent.getStringExtra(getString(R.string.symbol)));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    result=stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(getString(R.string.tag)), args));
    Intent intent_result = new Intent(getString(R.string.message)); //put the same message as in the filter you used in the activity when registering the receiver
    intent_result.putExtra(getString(R.string.results), result);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent_result);
  }
}
