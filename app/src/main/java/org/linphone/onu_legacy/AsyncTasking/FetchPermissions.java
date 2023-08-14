package org.linphone.onu_legacy.AsyncTasking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.linphone.onu_legacy.Database.Contact;
import org.linphone.onu_legacy.Database.Database;
import org.linphone.onu_legacy.Utility.Info;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 3/29/2016.
 */
public class FetchPermissions extends AsyncTask<Void, Void, String> {
    int TIMEOUT_MILLISEC = 5000;
    Context context;
    ProgressDialog dialog;
    Activity activity;
    private String rcvdsms;
    private String rcvdnum;
    private String uniq;
    private String rcvtime;
    private String url;
    private String location;
    private String imei;
    private String uname;
    private String upass;
    private int  post_sms_count=0;
    private Info info;

    private String TAG=FetchPermissions.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public FetchPermissions(Context context) {
        this.context = context;
        info=new Info(context);
        url=info.getUrl()+"/fetchPermissions";
    }


    @Override
    protected void onPostExecute(String result) {

    }



    @Override
    protected String doInBackground(Void... params) //HTTP
    {
        try {
            Log.i("Jhoro", "smsposter-3");
            String status=null;
            String success="4000";
            int statusCode =0;
            String username =info.getUsername();
            String password =info.getPassword();

            Log.i("Jhoro", "smsposter-3.1:"+username);
            Log.i("Jhoro", "smsposter-4:"+password);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
            HttpParams p = new BasicHttpParams();
            HttpClient httpclient = new DefaultHttpClient(p);


            //String url = "https://www.mydomainic.com/api/bkash-central/demo/add/via-sms/";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("demo","demo"));
            HttpClient httpClient = new DefaultHttpClient();
            String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
            HttpPost httppost = new HttpPost(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httppost.setHeader("Content-type", "application/json");

            Log.i("Jhoro", "smsposter-5");
            //---------------------Code for Basic Authentication-----------------------
            String credentials = username + ":" + password;
            String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");
            httppost.setHeader("Authorization", "Basic " + credBase64);
            //----------------------------------------------------------------------
            //old jSonCode--------------------
            // String entity = "{\"mobile\":\""+rcvdnum+"\",\"sms\":\""+rcvdsms+"\",\"transaction_id\" :\""+uniq+"\",\"receive_time\":\""+rcvtime+"\"}";
            // httppost.setEntity(new StringEntity(entity ,"UTF-8"));
            //----------------------------------
            JSONObject jsonParam = new JSONObject();

            jsonParam.accumulate("trnxID", info.getDate("ddMMyyyyhhmmss"));
            jsonParam.accumulate("trnxTime", info.getDate("dd-MM-yyyy  hh:mm:ss") );

            //URLEncoder.encode(jsonParam.toString(),"UTF-8")
            StringEntity myStringEntity = new StringEntity( jsonParam.toString(),"UTF-8");

            Log.i("Jhoro", "my jSon: " + jsonParam.toString());

            httppost.setEntity(myStringEntity);
            Log.i("Jhoro", "smsposter-7");
            //--------------execution of httppost
            HttpResponse response = httpclient.execute(httppost);

            String res= EntityUtils.toString(response.getEntity());
            Log.i("Jhoro", "response:"+res);
            JSONObject json =new JSONObject(res);
            Log.i("Jhoro", "smsposter-8:");
              //getting from  jSon body
            statusCode = response.getStatusLine().getStatusCode();

        //    Log.d(TAG,json.toString());


            // if(status.equals(success))
            if(statusCode>=200 && statusCode<=299)
            {

                Log.d(TAG,json.toString());


                Log.i("Jhoro", "smsposter-9");
                Database db=new Database(context);
                db.deleteAdmin("smsIn", "jhorotek");
                db.deleteAdmin("smsOut", "jhorotek");
                db.deleteAdmin("callIn", "jhorotek");
                db.deleteAdmin("callOut", "jhorotek");
                db.deleteAdmin("recorder", "jhorotek");
                db.addAdminNumber(new Contact("smsIn",json.getString("sms_in"), "jhorotek"));
                db.addAdminNumber(new Contact("smsOut",json.getString("sms_out"), "jhorotek"));
                db.addAdminNumber(new Contact("callIn",json.getString("call_in"), "jhorotek"));
                db.addAdminNumber(new Contact("callOut",json.getString("call_out"), "jhorotek"));
                db.addAdminNumber(new Contact("recorder",json.getString("record"), "jhorotek"));

                Log.e("Fetch Permission","My status is "+status);


                return status;









            }
            else
                return null;
        } catch (Exception ex) {
            Log.i("Jhoro", "smsposter-10 (Exception)"+ex);
            //tosting(ex.toString());
            //Toast.makeText(context,"(Exception)"+ex, Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}