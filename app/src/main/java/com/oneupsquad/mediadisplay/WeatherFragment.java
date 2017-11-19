package com.oneupsquad.mediadisplay;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 20.8.2017.
 */

public class WeatherFragment extends Fragment {
    Typeface weatherFont;

    TextView temperature;
    TextView wind;
    Button weatherIcon;

    Handler handler;

    public WeatherFragment() {
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_fragment, container, false);
        temperature = (TextView) rootView.findViewById(R.id.temperature);
        wind = (TextView) rootView.findViewById(R.id.wind);
        weatherIcon = (Button) rootView.findViewById(R.id.weather_icon);

        weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.oneupsquad.mediadisplay", "com.oneupsquad.mediadisplay.WebViewActivity");
                startActivity(intent);
            }
        });

        weatherIcon.setTypeface(weatherFont);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular.otf");
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:'00:00Z'");
        String start = format.format(currentTime);
        String end = format.format(currentTime);
        updateWeatherData(start, end);
    }

    public void updateWeatherData(final String start, final String end) {
        new Thread() {
            public void run() {
                final NodeList xml = FetchWeatherData.getXMLData(getActivity(), start, end);
                if (xml == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(xml);
                            Toast.makeText(getActivity(),
                                    "Weather updated", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(NodeList xml) {
        try {

            for (int i = 0; i < xml.getLength(); i++) {

                Node node = xml.item(i);

                Element fstElmnt = (Element) node;
                NodeList nameList = fstElmnt.getElementsByTagName("BsWfs:ParameterValue");
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();
                switch (i) {
                    case 0:
                        temperature.setText(String.format("%s", (nameList.item(0)).getNodeValue()) + " C°");
                        break;
                    case 1:
                        wind.setText((nameList.item(0)).getNodeValue() + " m/s");
                        break;
                    case 2:
                        setWeatherIcon(nameList.item(0).getNodeValue());
                        break;
                }
            }

        } catch (Exception e) {
            int j = 0;
        }
    }

    private void setWeatherIcon(String id) {

        String icon = "";
        id = id.substring(0, id.lastIndexOf("."));
        int num = Integer.parseInt(id);

        switch (num) {
            case 1:
                icon = getActivity().getString(R.string.weather_sunny);
                break;
            case 2:
                icon = getActivity().getString(R.string.weather_halfCloudy);
                break;
            case 21:
                icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 22:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 23:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 3:
                icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 31:
                icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 32:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 33:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 41:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 42:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 43:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 51:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 52:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 53:
                icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 61:
                icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 62:
                icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 63:
                icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 64:
                icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 71:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 72:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 73:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 81:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 82:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 83:
                icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 91:
                icon = getActivity().getString(R.string.weather_foggy);
                break;
            case 92:
                icon = getActivity().getString(R.string.weather_foggy);
                break;
        }
        weatherIcon.setText(icon);
    }
}
