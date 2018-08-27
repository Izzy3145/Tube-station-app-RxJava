package com.example.android.citymapperchallenge;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.example.android.citymapperchallenge.adapters.SequenceAdapter;
import com.example.android.citymapperchallenge.adapters.StationArrivalsAdapter;
import com.example.android.citymapperchallenge.constants.Const;
import com.example.android.citymapperchallenge.model.ArrivalLineTime;
import com.example.android.citymapperchallenge.model.NearbyEndPoint.StationsWithinRadius;
import com.example.android.citymapperchallenge.model.SequenceEndPoint.LineSequence;
import com.example.android.citymapperchallenge.model.SequenceEndPoint.Station;
import com.example.android.citymapperchallenge.model.SequenceEndPoint.StopPoint;
import com.example.android.citymapperchallenge.model.StationArrivals;
import com.example.android.citymapperchallenge.retrofit.App;
import com.example.android.citymapperchallenge.retrofit.TfLUnifyService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LineActivity extends AppCompatActivity {

    private ArrivalLineTime mSelectedArrival;
    private double mDistance;
    private String mLineId;
    private TfLUnifyService apiService;
    private final String LOG_TAG = LineActivity.class.getSimpleName();
    private LinearLayoutManager mLayoutManager;
    private SequenceAdapter mAdapter;
    private ArrayList<StopPoint> mStopPointList = new ArrayList<>();

    @BindView(R.id.toolbar_line)
    Toolbar toolbar;
    @BindView(R.id.stops_recycler_view)
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);
        ButterKnife.bind(this);
        //set up action bar
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        apiService = ((App) getApplication()).getService();

        //set up recycler view
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SequenceAdapter(this, mStopPointList);
        mRecyclerView.setAdapter(mAdapter);

        //TODO: if savedInstanceState = null, as with Baking App?

        Intent receivedIntent = getIntent();
        mSelectedArrival = receivedIntent.getParcelableExtra(Const.SELECTED_ARRIVAL);
        mDistance = receivedIntent.getDoubleExtra(Const.DISTANCE_TO_STATION, 0);
        mLineId = mSelectedArrival.getLineId();

        if(ab!= null) {
            getSupportActionBar().setTitle(mSelectedArrival.getLineName());
        }

        loadStopSequence();
    }

    private void loadStopSequence(){
        Observable<LineSequence> sequenceObservable = apiService.getLineSequence(mLineId);
        sequenceObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LineSequence>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v(LOG_TAG, "Subscribed to line sequence observable");
                    }

                    @Override
                    public void onNext(LineSequence lineSequence) {
                        //lineNameTv.setText(lineSequence.getStopPointSequences().toString());
                        //get first stop point sequence
                        List<StopPoint> foundStops = lineSequence.
                                getStopPointSequences().get(0).getStopPoint();
                        for (int i = 0; i < foundStops.size(); i++ ){
                            StopPoint stopPoint = foundStops.get(i);
                            mStopPointList.add(stopPoint);
                        }
                        mAdapter.notifyDataSetChanged();
                        Log.v(LOG_TAG, "Line sequence observable onNext reached");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "Error with line sequence observable");
                    }

                    @Override
                    public void onComplete() {
                        Log.v(LOG_TAG, "Line sequence observable completed");
                    }
                });
    }
}
