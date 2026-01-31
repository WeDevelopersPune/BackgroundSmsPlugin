package com.j.background_sms;

import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** BackgroundSmsPlugin */
public class BackgroundSmsPlugin implements FlutterPlugin, MethodCallHandler {

  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    channel = new MethodChannel(binding.getBinaryMessenger(), "background_sms");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("sendSms")) {
      String num = call.argument("phone");
      String msg = call.argument("msg");
      Integer simSlot = call.argument("simSlot");
      sendSMS(num, msg, simSlot, result);
    } else if (call.method.equals("isSupportMultiSim")) {
      isSupportCustomSim(result);
    } else {
      result.notImplemented();
    }
  }

  private void isSupportCustomSim(Result result) {
    result.success(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
  }

  private void sendSMS(String num, String msg, Integer simSlot, Result result) {
    try {
      SmsManager smsManager;

      if (simSlot != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        smsManager = SmsManager.getSmsManagerForSubscriptionId(simSlot);
      } else {
        smsManager = SmsManager.getDefault();
      }

      if (msg.length() < 120) {
        smsManager.sendTextMessage(num, null, msg, null, null);
      } else {
        int size = 120;
        ArrayList<String> parts = new ArrayList<>();
        for (int i = 0; i <= msg.length() / size; i++) {
          parts.add(msg.substring(i * size, Math.min((i + 1) * size, msg.length())));
        }
        smsManager.sendMultipartTextMessage(num, null, parts, null, null);
      }

      result.success("Sent");
    } catch (Exception e) {
      e.printStackTrace();
      result.error("FAILED", "Sms not sent", null);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
