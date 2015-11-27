package com.dengyi.myidea.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.dengyi.myidea.R;
import com.dengyi.myidea.utils.BitmapUtil;
import com.dengyi.myidea.utils.StringUtil;
import com.google.zxing.WriterException;

import java.io.UnsupportedEncodingException;

/**
 * Created by deng on 2015/11/27.
 */
public class HomeActivity extends Activity {
    private String username;
    private String usernumber;
    private ImageView iv_qr_image;
    protected int mScreenWidth;
    private TextView tvShowInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
    }

    private void initUI() {
        iv_qr_image = (ImageView) findViewById(R.id.iv_qr_image);
        tvShowInfo = (TextView) findViewById(R.id.tv_show_info);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;

    }

    public void scan(View view) {
        Intent intent = new Intent(this, com.zxing.activity.CaptureActivity.class);
        startActivityForResult(intent, 1);
    }

    public void choose(View view) {

        startActivityForResult(new Intent(
                Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            //此处分解字段获取联系人和号码


            if (requestCode == 2) {
                ContentResolver reContentResolverol = getContentResolver();
                Uri contactData = data.getData();
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null);
                while (phone.moveToNext()) {
                    usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                text.setText(usernumber+" ("+username+")");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("选择联系人");
                builder.setMessage("是否生成" + username + "的二维码信息？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        create2D();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(HomeActivity.this, "已取消", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (requestCode == 1) {
                /**
                 * 这儿是响应拍照后的
                 */
                Bundle bundle = data.getExtras();
                String scanResult = bundle.getString("result");
                if (scanResult.substring(0,5).equals("name,")){

//                此处分解字段获取联系人和号码
                    String[] contactInfo = scanResult.split(",");
                    final String name=contactInfo[1];
                    final String number=contactInfo[3];

                    tvShowInfo.setText(name+":"+number);


                    AlertDialog.Builder builderSuccess = new AlertDialog.Builder(HomeActivity.this);
                    builderSuccess.setTitle("联系人信息确认");
                    builderSuccess.setMessage("是否确认添加" + name + "为联系人？");
                    builderSuccess.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addContacts(name, number);

                        }
                    });
                    builderSuccess.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HomeActivity.this, "已经取消", Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog dialog = builderSuccess.create();
                    dialog.show();

                }else {
                    Toast.makeText(HomeActivity.this,"您扫描的不是本软件生成的二维码,内容见下方文本",Toast.LENGTH_LONG).show();
                    tvShowInfo.setText(scanResult);
                }
            }


        }
    }


    /**
     * 此处生成二维码
     */
    private void create2D() {


        String uri = "name," + username +","+ "number," + usernumber;
//		Bitmap bitmap = BitmapUtil.create2DCoderBitmap(uri, mScreenWidth/2, mScreenWidth/2);
        // String uri = resultTextView.getText().toString();
//		Bitmap bitmap = BitmapUtil.create2DCoderBitmap(uri, mScreenWidth/2, mScreenWidth/2);
        Bitmap bitmap;
        try {
            bitmap = BitmapUtil.createQRCode(uri, mScreenWidth);

            if (bitmap != null) {
                iv_qr_image.setImageBitmap(bitmap);
                tvShowInfo.setText("姓名:" + username + "  " + "号码:" + usernumber);
            }

        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 添加联系人
     *
     * @param name   姓名
     * @param number 号码
     */
    private void addContacts(String name, String number) {
        /* 往 raw_contacts 中添加数据，并获取添加的id号*/
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        long contactId = ContentUris.parseId(resolver.insert(uri, values));
        /* 往 data 中添加数据（要根据前面获取的id号） */
        // 添加姓名
        uri = Uri.parse("content://com.android.contacts/data");
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/name");
        values.put("data2", name);
        resolver.insert(uri, values);
        // 添加电话
        values.clear();
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/phone_v2");
        values.put("data2", "2");
        values.put("data1", number);
        resolver.insert(uri, values);
        //添加邮箱
        values.clear();
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/email_v2");
        values.put("data2", "2");
        values.put("data1", "");
        resolver.insert(uri, values);
        Toast.makeText(this, "添加成功", Toast.LENGTH_LONG).show();
    }
}
