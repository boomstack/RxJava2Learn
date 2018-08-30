package com.boomstack.rxjava2learn;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class MainActivity extends AppCompatActivity {
    private final String baseUrl = "https://api.douban.com/";
    private CompositeDisposable disposableContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        initData();
    }

    private void initData() {
        disposableContainer = new CompositeDisposable();
    }


    //click
    public void onBasic(View view) {
        Disposable d = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onComplete();
                e.onNext(5);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.holaPrint(String.valueOf(integer));
            }
        });
        if (!d.isDisposed()) {
            d.dispose();
        }
    }

    public void onNetwork(View view) {
        Disposable disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                    }
                });
        disposable.dispose();
        Flowable<Long> flowable = Flowable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());
        flowable.subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
        flowable.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {

            }
        });


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        api.testInternet(1220562)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableContainer.add(d);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (responseBody != null) {

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposableContainer != null) {
            disposableContainer.clear();
        }
    }

    public void onNetworkFlat(View view) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        final Api api = retrofit.create(Api.class);
        api.testInternet(1220562)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        //step1
                        if (responseBody != null) {
                            Utils.holaPrint(responseBody.string());
                        }
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Function<ResponseBody, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(ResponseBody responseBody) throws Exception {
                        return api.testInternet(1220563);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        //step2
                        if (responseBody != null) {
                            Utils.holaPrint(responseBody.string());
                        }
                    }
                });
    }

    public void onTestProtoBuf(View view) {
        Myproto.Person.Builder persionBuilder = Myproto.Person.newBuilder();
        persionBuilder.setId(123);
        persionBuilder.setName("Ethan");
        Myproto.Person.Phone phoneOne = Myproto.Person.Phone.newBuilder().setNumber("15710069830").setType(Myproto.Person.PhoneType.MOBILE).build();
        Myproto.Person.Phone phoneTwo = Myproto.Person.Phone.newBuilder().setNumber("18811497512").setType(Myproto.Person.PhoneType.MOBILE).build();
        persionBuilder.addPhone(phoneOne);
        persionBuilder.addPhone(phoneTwo);
        Myproto.Person person = persionBuilder.build();

        byte[] buff = person.toByteArray();

        if (buff != null && buff.length > 0) {
            try {
                Myproto.Person outputPerson = Myproto.Person.parseFrom(buff);
                System.out.println("ID:" + outputPerson.getId() + " \nname: " + outputPerson.getName());
                outputPerson.getPhoneList().forEach(phone -> System.out.println("phone: " + phone.getNumber()));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }
}
