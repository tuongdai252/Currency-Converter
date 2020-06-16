package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner FromCurrency;
    private Spinner ToCurrency;
    private TextView input;
    private TextView output;
    private Button ConvertButton;
    private int countcurrency = 0;
    private String[] arraySpinner;
    private double invalue;
    private double outvalue;

    public class GetDataSync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            FromCurrency.setAdapter(adapter);
            ToCurrency.setAdapter(adapter);
        }
    }

    private void getData() throws IOException {
        Document doc = Jsoup.connect("https://www.fxexchangerate.com/currency-converter-rss-feed.html").get();
        try {
            Elements rss = doc.select("div.rss");
            Elements options = rss.select("a[href]");
            countcurrency = options.size();
            arraySpinner = new String[countcurrency];
            int i = 0;
            for(Element option : options) {
                arraySpinner[i] = option.text();
                i++;
            }
            Arrays.sort(arraySpinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FromCurrency = (Spinner) findViewById(R.id.FromCurrency);
        ToCurrency = (Spinner) findViewById(R.id.ToCurrency);
        input = (TextView) findViewById(R.id.input);
        output = (TextView) findViewById(R.id.output);
        ConvertButton = (Button) findViewById(R.id.ConvertButton);
        new GetDataSync().execute();
        ConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().length() > 0)
                    new GetDataConvert().execute();
                else
                    output.setText("");
            }
        });
    }

    public class GetDataConvert extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getCurrency();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            output.setText(formatDouble(outvalue));
        }
    }

    public String formatDouble(double n)
    {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        nf.setMaximumFractionDigits(9);
        nf.setMinimumFractionDigits(0);
        return nf.format(n);
    }

    private void getCurrency() throws IOException {
        String incurrency = FromCurrency.getSelectedItem().toString();
        String outcurrency = ToCurrency.getSelectedItem().toString();
        invalue = Double.valueOf(input.getText().toString());
        if(incurrency == outcurrency) {
            outvalue = invalue;
        }
        else {
            incurrency = incurrency.split("-")[0];
            incurrency = incurrency.replaceAll("\\s+", "");
            incurrency = incurrency.toLowerCase();
            outcurrency = outcurrency.split("-")[1];
            String doc = readFromUrl("https://" + incurrency + ".fxexchangerate.com/rss.xml");
            String value = doc.split(outcurrency)[0];
            value = value.substring(value.lastIndexOf("=")+1);
            value = value.replaceAll("\\s+", "");
            outvalue = Double.valueOf(value) * invalue;
        }
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String readFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String Text = readAll(rd);
            return Text;
        } finally {
            is.close();
        }
    }
}