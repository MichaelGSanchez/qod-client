package edu.cnm.deepdive.qod.controller;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import edu.cnm.deepdive.qod.R;
import edu.cnm.deepdive.qod.model.Source;
import edu.cnm.deepdive.qod.service.QodService.GetQodTask;

public class MainActivity extends AppCompatActivity {

  private ShakeListener listener;
  private TextView quoteText;
  private TextView sourceName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    quoteText = findViewById(R.id.quote_text);
    sourceName = findViewById(R.id.source_name);
    findViewById(R.id.answer_background).setOnClickListener((v) -> changeAnswer());
    listener = new ShakeListener();
  }

  @Override
  protected void onResume() {
    super.onResume();
    listener.register();
  }

  @Override
  protected void onPause() {
    super.onPause();
    listener.unregister();
  }

  private void changeAnswer() {
    new GetQodTask()
        .setSuccessListener((quote) -> {
          quoteText.setText(quote.getText());
          StringBuilder builder = new StringBuilder();
          for (Source source : quote.getSources()) {
            builder.append(source.getName());
            builder.append("; ");
          }
          if (builder.length() > 0) {
            sourceName.setText(builder.substring(0, builder.length() - 2));
          }else{
            sourceName.setText("");
          }
          fadeTogether(quoteText, sourceName);
        })
        .execute();
  }

  private void fadeTogether(TextView... textViews) {
    ObjectAnimator[] animators = new ObjectAnimator[textViews.length];
    for (int i = 0; i < textViews.length; i++) {
      ObjectAnimator fade =
          (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.text_fade);
      fade.setTarget(textViews[i]);
      animators[i] = fade;
    }
    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    set.start();
  }

  private class ShakeListener implements SensorEventListener {

    private static final float DAMPING_FACTOR = 0.9f;
    private static final float THRESHOLD = 8;

    private SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    private float currentAcceleration;
    private float dampedAcceleration;

    private void register() {
      dampedAcceleration = 0;
      currentAcceleration = SensorManager.GRAVITY_EARTH;
      manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
          SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregister() {
      manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
      float x = se.values[0];
      float y = se.values[1];
      float z = se.values[2];
      float previous = currentAcceleration;
      dampedAcceleration *= DAMPING_FACTOR;
      currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
      dampedAcceleration += currentAcceleration - previous;
      if (dampedAcceleration > THRESHOLD) {
        changeAnswer();
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // Do nothing.
    }

  }

}
