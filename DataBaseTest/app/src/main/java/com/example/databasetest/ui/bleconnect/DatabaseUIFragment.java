package com.example.databasetest.ui.bleconnect;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.databasetest.BLEConnectActivity;
import com.example.databasetest.MainActivity;
import com.example.databasetest.R;
import java.util.ArrayList;

import DatabaseVersion.DatabaseVersionManager;
import DatabaseVersion.MyDatabaseHelper;

public class DatabaseUIFragment extends Fragment {

    private BleconnectViewModel mViewModel;
    private ExpandableListView  mDatabaseList;


    public static DatabaseUIFragment newInstance() {
        return new DatabaseUIFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.database_ui_fragment, container, false);
        mDatabaseList = view.findViewById(R.id.database_list);
        DatabaseAdapter myAdapter = new DatabaseAdapter(getActivity(), MainActivity.UserNameLogin);
        String table1 = myAdapter.getGroup(0);
        String table2 = myAdapter.getChild(0,0);
        Log.d("FragmentUI", "onCreateView: "+table1);
        mDatabaseList.setAdapter(myAdapter);
//        mDatabaseList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v,
//                                        int groupPosition, long id) {
//                return true;//返回true则表示无法收缩
//            }
//        });
        for (int i=0; i<myAdapter.getGroupCount(); i++)
        {
            mDatabaseList.expandGroup(i);
        };
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(BleconnectViewModel.class);
        // TODO: Use the ViewModel
    }

    class DatabaseAdapter extends BaseExpandableListAdapter {
        private Context mcontext;
        private SQLiteDatabase db;
        private MyDatabaseHelper dbHelper;
        private DatabaseVersionManager mDatabaseVersionManager = new DatabaseVersionManager();

        public ArrayList<String> DatabaseUserName = new ArrayList<String>();
        public ArrayList<ArrayList<String>> DatabaseTableName = new ArrayList<ArrayList<String>>();
        public DatabaseAdapter(Context context,String LoginUserName){
            this.mcontext = context;
            dbHelper =new MyDatabaseHelper(mcontext,"PULSE.db",null, mDatabaseVersionManager.VERSION);
            db = dbHelper.getWritableDatabase();
            DatabaseUserName.add(LoginUserName);
            DatabaseTableName = dbHelper.getUserAllDataTable(db,LoginUserName);
        }
        @Override
        // 获取分组的个数
        public int getGroupCount() {
            return DatabaseUserName.size();
        }
        //获取指定分组中的子选项的个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return DatabaseTableName.get(groupPosition).size();
        }
        //获取指定的分组数据
        @Override
        public String getGroup(int groupPosition) {
            return DatabaseUserName.get(groupPosition);
        }
        //获取指定分组中的指定子选项数据
        @Override
        public String getChild(int groupPosition, int childPosition) {
            return DatabaseTableName.get(groupPosition).get(childPosition);
        }
        //获取指定分组的ID, 这个ID必须是唯一的
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        //获取子选项的ID, 这个ID必须是唯一的
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
        //分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们
        @Override
        public boolean hasStableIds() {
            return true;
        }
        /**
         *
         * 获取显示指定组的视图对象
         *
         * @param groupPosition 组位置
         * @param isExpanded 该组是展开状态还是伸缩状态
         * @param convertView 重用已有的视图对象
         * @param parent 返回的视图对象始终依附于的视图组
         */
// 获取显示指定分组的视图
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder groupViewHolder;
            if (convertView == null){
                convertView = (View)getActivity().getLayoutInflater().from(mcontext).inflate(R.layout.database_user_item,parent,false);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.tvTitle = (TextView)convertView.findViewById(R.id.label_group_normal);
                convertView.setTag(groupViewHolder);
            }else {
                groupViewHolder = (GroupViewHolder)convertView.getTag();
            }
            groupViewHolder.tvTitle.setText(DatabaseUserName.get(groupPosition));
            return convertView;
        }
        /**
         *
         * 获取一个视图对象，显示指定组中的指定子元素数据。
         *
         * @param groupPosition 组位置
         * @param childPosition 子元素位置
         * @param isLastChild 子元素是否处于组中的最后一个
         * @param convertView 重用已有的视图(View)对象
         * @param parent 返回的视图(View)对象始终依附于的视图组
         * @return
         * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View,
         *      android.view.ViewGroup)
         */
        //取得显示给定分组给定子位置的数据用的视图
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder childViewHolder;
            if (convertView==null){
                convertView =  (View)getActivity().getLayoutInflater().from(mcontext).inflate(
                        R.layout.database_tablename_item, null);      //把界面放到缓冲区
                childViewHolder = new ChildViewHolder();
                childViewHolder.tvTitle = (TextView)convertView.findViewById(R.id.expand_child);
                convertView.setTag(childViewHolder);

            }else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }
            childViewHolder.tvTitle.setText(DatabaseTableName.get(groupPosition).get(childPosition));
            return convertView;
        }
        //指定位置上的子元素是否可选中
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
        class GroupViewHolder {
            public TextView tvTitle;
        }
        class ChildViewHolder {
            public TextView tvTitle;

        }
    }


}
