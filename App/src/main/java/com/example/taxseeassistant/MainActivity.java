package com.example.taxseeassistant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.*;
import android.os.Bundle;
import android.graphics.Typeface;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView gps, lokasi, internet;
    LocationManager manager;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(32, 40, 32, 32);

        TextView judul = new TextView(this);
        judul.setText("TAXSEE ASSISTANT");
        judul.setTextSize(25);
        judul.setTypeface(null, Typeface.BOLD);
        layout.addView(judul);

        TextView pembuat = new TextView(this);
        pembuat.setText("Dibuat oleh Aswin");
        pembuat.setTextSize(16);
        layout.addView(pembuat);

        gps = tambah(layout, "GPS: Memeriksa...");
        lokasi = tambah(layout, "Lokasi: Menunggu...");
        internet = tambah(layout, "Internet: Memeriksa...");

        Button tombol = new Button(this);
        tombol.setText("PERIKSA GPS & INTERNET");
        layout.addView(tombol);

        tombol.setOnClickListener(v -> periksa());

        TextView catatan = tambah(layout,
            "Aplikasi pendamping GPS dan internet. " +
            "Tidak mengubah prioritas atau FOKUS Taxsee.");

        setContentView(layout);

        manager = (LocationManager)
            getSystemService(Context.LOCATION_SERVICE);

        periksa();
    }

    TextView tambah(LinearLayout l, String teks) {
        TextView t = new TextView(this);
        t.setText(teks);
        t.setTextSize(17);
        t.setPadding(0, 22, 0, 22);
        l.addView(t);
        return t;
    }

    void periksa() {
        periksaInternet();
        periksaGPS();
    }

    void periksaInternet() {
        ConnectivityManager cm =
            (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE);

        Network n = cm.getActiveNetwork();
        NetworkCapabilities c =
            n == null ? null : cm.getNetworkCapabilities(n);

        if (c == null) {
            internet.setText("Internet: Tidak terhubung");
            return;
        }

        boolean valid = c.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        String jenis = c.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI)
            ? "Wi-Fi" : "Data seluler / jaringan lain";

        internet.setText("Internet: " + jenis +
            (valid ? " - Terverifikasi" : " - Belum terverifikasi"));
    }

    void periksaGPS() {
        boolean izin =
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;

        boolean izinKasar =
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;

        if (!izin && !izinKasar) {
            requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
            gps.setText("GPS: Menunggu izin lokasi");
            return;
        }

        boolean aktif = manager.isProviderEnabled(
            LocationManager.GPS_PROVIDER);

        gps.setText("GPS: " + (aktif ? "Aktif" : "Nonaktif"));

        if (!aktif) {
            lokasi.setText("Aktifkan GPS pada pengaturan HP.");
            return;
        }

        try {
            String provider = izin
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;

            if (!manager.isProviderEnabled(provider)) {
                lokasi.setText("Provider lokasi belum aktif.");
                return;
            }

            Location last =
                manager.getLastKnownLocation(provider);

            if (last != null) tampilkanLokasi(last);

            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location loc) {
                    tampilkanLokasi(loc);
                }
            };

            manager.requestLocationUpdates(
                provider, 5000, 5, listener);

        } catch (SecurityException e) {
            lokasi.setText("Izin lokasi tidak tersedia.");
        } catch (Exception e) {
            lokasi.setText("Gagal membaca lokasi.");
        }
    }

    void tampilkanLokasi(Location loc) {
        String akurasi = loc.hasAccuracy()
            ? String.format(Locale.getDefault(),
                "%.0f meter", loc.getAccuracy())
            : "Tidak diketahui";

        lokasi.setText(String.format(
            Locale.getDefault(),
            "Latitude: %.5f\nLongitude: %.5f\nAkurasi: %s",
            loc.getLatitude(),
            loc.getLongitude(),
            akurasi
        ));
    }
          }
