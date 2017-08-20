package com.oneupsquad.mediadisplay;

import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by User on 20.8.2017.
 */

public class FetchWeatherData {

    private static final String WEATHER_API_CALL =
            "http://data.fmi.fi/fmi-apikey/3e1a96c9-51d8-45ad-9093-458efc1a28c1/wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::simple&place=vuosaari&starttime=%s&endtime=%s&parameters=Temperature,WindSpeedMS,WeatherSymbol3";

    public static NodeList getXMLData(Context context, String start, String end) {
        try {
            URL url = new URL(String.format(WEATHER_API_CALL, start, end));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("wfs:member");

            return nodeList;

        } catch (Exception e) {
            return null;
        }
    }
}
