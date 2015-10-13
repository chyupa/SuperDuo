package it.jaschke.alexandria;

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import it.jaschke.alexandria.CameraPreview.CameraSourcePreview;
import it.jaschke.alexandria.CameraPreview.GraphicOverlay;

/**
 * Created by chyupa on 16-Sep-15.
 */
public class Scanner extends Fragment {

    private View rootView;

    private static final String TAG = "MultiTracker";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private String mbarcodeValue;

    public Scanner(){

    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.camera);
//
//        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
//        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
//
//        createCameraSource();
//    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.camera, container, false);
        mPreview = (CameraSourcePreview)rootView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay)rootView.findViewById(R.id.faceOverlay);

        createCameraSource();

        return rootView;
    }

    private void createCameraSource(){
        Context context = getActivity();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeTrackerFactory = new BarcodeTrackerFactory(mGraphicOverlay, new GraphicTracker.Callback() {
            @Override
            public void onFound(String barcodeValue) {
                mbarcodeValue = barcodeValue;
//                if( mCameraSource != null ){
//                    mCameraSource.stop();
//                }
//                Intent intent = new Intent(getApplicationContext(), AddBook.class);
//                intent.putExtra("barcode_value", barcodeValue);
//                startActivity(intent);
                try {
                    Bundle args = new Bundle();
                    args.putString("scanned_ean", mbarcodeValue);
                    AddBook fragment = new AddBook();
                    fragment.setArguments(args);
                    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                } catch (Exception e) {
                    Log.e("SUPPORT FRAGMENT", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeTrackerFactory).build());

        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(barcodeDetector)
                .build();

        mCameraSource = new CameraSource.Builder(getActivity(), multiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .build();


    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    public void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
}
