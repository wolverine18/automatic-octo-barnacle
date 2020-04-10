package edu.gvsu.cis.convcalc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import edu.gvsu.cis.convcalc.UnitsConverter.LengthUnits;
import edu.gvsu.cis.convcalc.UnitsConverter.VolumeUnits;
import edu.gvsu.cis.convcalc.dummy.HistoryContent;
import edu.gvsu.cis.convcalc.webservice.WeatherService;

public class MainActivity extends AppCompatActivity {

    public static int SETTINGS_RESULT = 1;
    public static int HISTORY_RESULT = 2;

    private enum Mode {Length, Volume};

    private Mode mode = Mode.Length;
    private Button calcButton;
    private Button clearButton;
    private Button modeButton;

    private EditText toField;
    private EditText fromField;


    private TextView toUnits;
    private TextView fromUnits;
    private TextView title;

    private TextView current;
    private TextView temperature;
    private ImageView weatherIcon;

    DatabaseReference topRef;
    public static List<HistoryContent.HistoryItem> allHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allHistory = new ArrayList<HistoryContent.HistoryItem>();

        calcButton = findViewById(R.id.calcButton);
        clearButton = findViewById(R.id.clearButton);
        modeButton = findViewById(R.id.modeButton);

        toField = findViewById(R.id.to);
        fromField = findViewById(R.id.from);

        fromUnits = findViewById(R.id.fromUnits);
        toUnits = findViewById(R.id.toUnits);

        title = findViewById(R.id.title);

        current = findViewById(R.id.tvCurrent);
        temperature = findViewById(R.id.tvTemp);
        weatherIcon = findViewById(R.id.ivWeather);

        calcButton.setOnClickListener(v -> {
            WeatherService.startGetWeather(this, "42.963686", "-85.888595", "p1");
            doConversion();
        });

        clearButton.setOnClickListener(v -> {
            toField.setText("");
            fromField.setText("");
            hideKeyboard();
        });

        modeButton.setOnClickListener(v -> {
            toField.setText("");
            fromField.setText("");
            hideKeyboard();
            switch(mode) {
                case Length:
                    mode = Mode.Volume;
                    fromUnits.setText(VolumeUnits.Gallons.toString());
                    toUnits.setText(VolumeUnits.Liters.toString());
                    break;
                case Volume:
                    mode = Mode.Length;
                    fromUnits.setText(LengthUnits.Yards.toString());
                    toUnits.setText(LengthUnits.Meters.toString());
                    break;
            }
            title.setText(mode.toString() + " Converter");
        });

        toField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                fromField.getText().clear();
            }
        });

        fromField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                toField.getText().clear();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        allHistory.clear();
        topRef = FirebaseDatabase.getInstance().getReference("history");
        topRef.addChildEventListener (chEvListener);
        IntentFilter weatherFilter = new IntentFilter(WeatherService.BROADCAST_WEATHER);
        LocalBroadcastManager.getInstance(this).registerReceiver(weatherReceiver, weatherFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        topRef.removeEventListener(chEvListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(weatherReceiver);
    }

    private void doConversion() {
        EditText dest = null;
        String val = "";
        String fromVal = fromField.getText().toString();
        if (fromVal.intern() != "" ) {
            val = fromVal;
            dest = toField;
        }
        String toVal = toField.getText().toString();
        if (toVal.intern() != "") {
            val = toVal;
            dest = fromField;
        }

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        if (dest != null) {
            switch(mode) {
                case Length:
                    LengthUnits tUnits, fUnits;
                    if(dest == toField) {
                        fUnits = LengthUnits.valueOf(fromUnits.getText().toString());
                        tUnits = LengthUnits.valueOf(toUnits.getText().toString());
                    } else {
                        fUnits = LengthUnits.valueOf(toUnits.getText().toString());
                        tUnits = LengthUnits.valueOf(fromUnits.getText().toString());
                    }
                    Double dVal = Double.parseDouble(val);
                    Double cVal = UnitsConverter.convert(dVal, fUnits, tUnits);
                    dest.setText(Double.toString(cVal));
                    // remember the calculation.
                    HistoryContent.HistoryItem item = new HistoryContent.HistoryItem(dVal, cVal, mode.toString(),
                            fUnits.toString(), tUnits.toString(), fmt.print(DateTime.now()));
                    HistoryContent.addItem(item);
                    topRef.push().setValue(item);
                    break;
                case Volume:
                    VolumeUnits vtUnits, vfUnits;
                    if(dest == toField) {
                        vfUnits = VolumeUnits.valueOf(fromUnits.getText().toString());
                        vtUnits = VolumeUnits.valueOf(toUnits.getText().toString());
                    } else {
                        vfUnits = VolumeUnits.valueOf(toUnits.getText().toString());
                        vtUnits = VolumeUnits.valueOf(fromUnits.getText().toString());
                    }
                    Double vdVal = Double.parseDouble(val);
                    Double vcVal = UnitsConverter.convert(vdVal, vfUnits, vtUnits);
                    dest.setText(Double.toString(vcVal));
                    // remember the calculation.
                    HistoryContent.HistoryItem vitem = new HistoryContent.HistoryItem(vdVal, vcVal, mode.toString(),
                            vfUnits.toString(), vtUnits.toString(), fmt.print(DateTime.now()));
                    HistoryContent.addItem(vitem);
                    topRef.push().setValue(vitem);
                    break;
            }
        }
        hideKeyboard();

    }

    private void hideKeyboard()
    {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            //this.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, MySettingsActivity.class);
            intent.putExtra("mode", mode.toString());
            intent.putExtra("fromUnits", fromUnits.getText().toString());
            intent.putExtra("toUnits", toUnits.getText().toString());
            startActivityForResult(intent, SETTINGS_RESULT );
            return true;
        } else if(item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivityForResult(intent, HISTORY_RESULT );
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SETTINGS_RESULT) {
            this.fromUnits.setText(data.getStringExtra("fromUnits"));
            this.toUnits.setText(data.getStringExtra("toUnits"));
        } else if (resultCode == HISTORY_RESULT) {
            String[] vals = data.getStringArrayExtra("item");
            this.fromField.setText(vals[0]);
            this.toField.setText(vals[1]);
            this.mode = Mode.valueOf(vals[2]);
            this.fromUnits.setText(vals[3]);
            this.toUnits.setText(vals[4]);
            this.title.setText(mode.toString() + " Converter");
        }

    }

    private ChildEventListener chEvListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HistoryContent.HistoryItem entry =
                    (HistoryContent.HistoryItem) dataSnapshot.getValue(HistoryContent.HistoryItem.class);
            entry._key = dataSnapshot.getKey();
            allHistory.add(entry);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            HistoryContent.HistoryItem entry =
                    (HistoryContent.HistoryItem) dataSnapshot.getValue(HistoryContent.HistoryItem.class);
            List<HistoryContent.HistoryItem> newHistory = new ArrayList<HistoryContent.HistoryItem>();
            for (HistoryContent.HistoryItem t : allHistory) {
                if (!t._key.equals(dataSnapshot.getKey())) {
                    newHistory.add(t);
                }
            }
            allHistory = newHistory;
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            double temp = bundle.getDouble("TEMPERATURE");
            String summary = bundle.getString("SUMMARY");
            String icon = bundle.getString("ICON");
            String key = bundle.getString("KEY");
            icon = "img" + icon;
            int resID = getResources().getIdentifier(icon , "drawable", getPackageName());

            if (key.equals("p1"))  {
                current.setVisibility(View.VISIBLE);
                current.setText(summary);
                temperature.setVisibility(View.VISIBLE);
                temperature.setText(Double.toString(temp));
                weatherIcon.setVisibility(View.VISIBLE);
                weatherIcon.setImageResource(resID);
            }
        }
    };

}
