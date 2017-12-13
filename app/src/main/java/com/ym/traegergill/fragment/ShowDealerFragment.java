package com.ym.traegergill.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalantis.phoenix.PullToRefreshView;
import com.ym.traegergill.R;
import com.ym.traegergill.activity.BaseActivity;
import com.ym.traegergill.adapter.DealerRvAdapter;
import com.ym.traegergill.bean.DealerBean;
import com.ym.traegergill.iview.IShowDealerView;
import com.ym.traegergill.presenter.ShowDealerPresenter;
import com.ym.traegergill.tuya.utils.DialogUtil;
import com.ym.traegergill.view.loadmore.DefaultFootItem;
import com.ym.traegergill.view.loadmore.OnLoadMoreListener;
import com.ym.traegergill.view.loadmore.RecyclerViewWithFooter;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2017/11/24.
 */
@SuppressLint("ValidFragment")
public class ShowDealerFragment extends BaseFragment implements IShowDealerView {
    @BindView(R.id.rv_load_more)
    RecyclerViewWithFooter rvLoadMore;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshView pullToRefresh;
    DealerRvAdapter adapter;
    ShowDealerPresenter showDealerPresenter;
    List<DealerBean> mData;
    int distributorTypeid;

    public int getDistributorTypeid() {
        return distributorTypeid;
    }

    public ShowDealerFragment(int distributorTypeid ){
        this.distributorTypeid = distributorTypeid;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_items, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        initPresenter();
        initRecycler();
    }

    private void initPresenter() {
        showDealerPresenter = new ShowDealerPresenter(this,this);
    }

    private void initRecycler() {
        pullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //page = 0;
                        if(rvLoadMore == null) return;
                        rvLoadMore.setLoading();
                        showDealerPresenter.getDataFromServer(false);
                        pullToRefresh.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mData = new ArrayList<>();
        rvLoadMore.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DealerRvAdapter(getActivity(), mData);
        adapter.setOnMyItemClickListener(new DealerRvAdapter.OnMyItemClickListener() {
            @Override
            public void onNormalClick(View v, final int position) {
                switch (v.getId()){
                    case R.id.call:
                        DialogUtil.customDialog(getActivity(), null, getActivity().getString(R.string.CALL)+mData.get(position).getTel()+"?"
                                , getActivity().getString(R.string.Confirm), getActivity().getString(R.string.action_close), null, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                showDealerPresenter.callClick(mData.get(position));
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                break;
                                        }
                                    }
                                }).show();

                        break;
                    case R.id.location:
                        showDealerPresenter.locationClick(mData.get(position));
                        break;

                }
            }
        });
        rvLoadMore.setAdapter(adapter);
        rvLoadMore.setFootItem(new DefaultFootItem());//默认是这种
        rvLoadMore.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rvLoadMore.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDealerPresenter.getDataFromServer(true);
                    }
                }, 1000);
            }
        });
        //page = 0;
        showDealerPresenter.getDataFromServer(false);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void updateDeviceData(List<DealerBean> datas) {
        mData.clear();
        if(datas!=null)
            mData.addAll(datas);
        if( rvLoadMore!=null && rvLoadMore.getAdapter()!=null)
            rvLoadMore.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void updateDeviceDataEnd() {
        rvLoadMore.setEnd(getResources().getString(R.string.rv_with_footer_empty));
    }

    @Override
    public void showRenetDialog(DialogInterface.OnClickListener listener) {
        super.showRenetDialog(listener);
    }
}
