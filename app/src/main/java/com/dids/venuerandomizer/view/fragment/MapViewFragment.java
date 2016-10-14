package com.dids.venuerandomizer.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dids.venuerandomizer.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    private static final String LAT = "lat";
    private static final String LONG = "long";
    private static final String NAME = "name";
    private static final int ZOOM_LEVEL = 15;
    private LatLng mPostion;
    private GoogleMap mGoogleMap;

    private MapView mMapView;

    public static MapViewFragment getInstance(String name, double lat, double lng) {
        MapViewFragment fragment = new MapViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(NAME, name);
        bundle.putDouble(LAT, lat);
        bundle.putDouble(LONG, lng);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostion = new LatLng(getArguments().getDouble(LONG, 0), getArguments().getDouble(LAT, 0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapview, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);

        MapsInitializer.initialize(getActivity().getApplicationContext());
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setCameraToLocation();
    }

    public void setCameraToLocation() {
        mGoogleMap.addMarker(new MarkerOptions().position(mPostion).title(getArguments().
                getString(NAME)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mPostion).
                zoom(ZOOM_LEVEL).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }
}
