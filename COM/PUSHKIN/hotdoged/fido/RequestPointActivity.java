package com.pushkin.hotdoged.fido;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestPointActivity extends AppCompatActivity {
    private static final Uri SERVERS_URI = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers");
    private static final String TAG = "RequestPointActivity";
    private String about;
    private Button buttonSubmit;
    private String email;
    private EditText etAbout;
    private EditText etEmail;
    private EditText etName;
    private EditText etPassword;
    private Spinner lvNode;
    private String name;
    private NodesAdapter nodesAdapter = null;
    private String password;
    private TextView tvNote;

    private class AsyncNodeFetcher extends AsyncTask<String, Object, String> {
        private AsyncNodeFetcher() {
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(String... arg0) {
            try {
                String result = RequestPointActivity.this.postHttpNodesRequest();
                return result;
            } catch (HotdogedException e) {
                Main.SystemInfo.getLogger().log("AsyncNodeFetcher", "Node config request returned error: " + e.getLocalizedMessage());
                return null;
            }
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String result) {
            RequestPointActivity.this.setSupportProgressBarIndeterminateVisibility(false);
            RequestPointActivity.this.nodesRequestResultReceived(result);
            RequestPointActivity.this.buttonSubmit.setEnabled(true);
            super.onPostExecute((AsyncNodeFetcher) result);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            Main.SystemInfo.getLogger().log("AsyncNodeFetcher", "Requesting list of nodes");
            RequestPointActivity.this.buttonSubmit.setEnabled(false);
            RequestPointActivity.this.setSupportProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }
    }

    /* INFO: Access modifiers changed from: private */
    public class AsyncRequestSender extends AsyncTask<Object, Object, String> {
        private NodeInfo nodeInfo;

        private AsyncRequestSender() {
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(Object... arg0) {
            String name = (String) arg0[0];
            String email = (String) arg0[1];
            String password = (String) arg0[2];
            String about = (String) arg0[3];
            this.nodeInfo = (NodeInfo) arg0[4];
            String result = RequestPointActivity.this.postHttpPointRequest(name, email, password, about, this.nodeInfo);
            return result;
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String result) {
            RequestPointActivity.this.setSupportProgressBarIndeterminateVisibility(false);
            RequestPointActivity.this.pointRequestResultReceived(result, this.nodeInfo);
            super.onPostExecute((AsyncRequestSender) result);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            Main.SystemInfo.getLogger().log("AsyncRequestSender", "Sending point request");
            RequestPointActivity.this.setSupportProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }
    }

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(5);
        setSupportProgressBarIndeterminate(true);
        setContentView(R.layout.activity_request_point);
        this.tvNote = (TextView) findViewById(R.id.textViewNote);
        this.etName = (EditText) findViewById(R.id.editTextName);
        this.etEmail = (EditText) findViewById(R.id.editTextEmail);
        this.etPassword = (EditText) findViewById(R.id.editTextPassword);
        this.etAbout = (EditText) findViewById(R.id.editTextAboutYourself);
        this.lvNode = (Spinner) findViewById(R.id.spinnerNode);
        this.lvNode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.pushkin.hotdoged.fido.RequestPointActivity.1
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                NodeInfo nodeInfo = (NodeInfo) RequestPointActivity.this.lvNode.getSelectedItem();
                if (TextUtils.isEmpty(nodeInfo.getNote())) {
                    RequestPointActivity.this.tvNote.setText("");
                    RequestPointActivity.this.tvNote.setVisibility(8);
                    return;
                }
                RequestPointActivity.this.tvNote.setText(RequestPointActivity.this.getResources().getString(R.string.note_from_sysop) + "\n" + nodeInfo.getNote().trim());
                RequestPointActivity.this.tvNote.setVisibility(0);
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        this.buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        this.buttonSubmit.setOnClickListener(new View.OnClickListener() { // from class: com.pushkin.hotdoged.fido.RequestPointActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                RequestPointActivity.this.submitRequest();
            }
        });
        setSupportProgressBarIndeterminateVisibility(false);
        Toast.makeText(this, "Ждем обновления настроек нод через интернет...", 1).show();
        new AsyncNodeFetcher().execute(new String[0]);
    }

    public void nodesRequestResultReceived(String result) {
        Log.d(TAG, "Получен результат:\n" + result);
        if (result == null) {
            setResult(1);
            showMessage("Ошибка отправки запроса настройки ноды. Попробуй снова позже.", true);
        }
        try {
            this.nodesAdapter = new NodesAdapter(this, result);
            if (!this.nodesAdapter.isEmpty()) {
                requestLocationInfo();
                return;
            }
            setResult(1);
            showMessage("К сожалению, в данный момент для тебя нет доступных нод.", true);
        } catch (HotdogedException e) {
            setResult(0);
            showMessage("Ошибка разбора настройки ноды: " + e.getLocalizedMessage() + "\n\nВозможно, нода не работает. Попробуй снова позже.", true);
            Main.SystemInfo.getLogger().log("nodesRequestResultReceived", "Ошибка разбора настроек ноды: " + e.getLocalizedMessage());
        }
    }

    private void requestLocationInfo() {
        Intent intent = new Intent(this, RequestLocationActivity.class);
        HashMap<String, ArrayList<String>> locations = this.nodesAdapter.getLocations();
        for (String country : locations.keySet()) {
            intent.putStringArrayListExtra(country, locations.get(country));
        }
        ArrayList<String> list = new ArrayList<>(locations.keySet());
        intent.putExtra("countries", list);
        startActivityForResult(intent, 0);
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            String yourCountry = data.getStringExtra("country");
            String yourCity = data.getStringExtra("city");
            Log.d(TAG, "Установи свое местоположение: " + yourCountry + ", " + yourCity);
            this.nodesAdapter.setYourLocation(yourCountry, yourCity);
            this.lvNode.setAdapter((SpinnerAdapter) this.nodesAdapter);
            if (!this.nodesAdapter.isEmpty()) {
                this.lvNode.setSelection(0);
            } else {
                setResult(1);
                showMessage("К сожалению, невозможно послать запрос на ФИДО-адрес нодам, на которых ты уже имеешь AKA. Ого, тебе они действительно нужны? :)\n\nТолько один AKA на ноду допускается в ХотДоге.", true);
            }
            if (resultCode == 1) {
                finish();
            }
        }
    }

    public String postHttpNodesRequest() throws HotdogedException {
        String nodesUri = getResources().getString(R.string.nodes_config_url);
        try {
            String serverResponse = Utils.getHttpRequest(nodesUri);
            Main.SystemInfo.getLogger().log("getHttpNodesRequest", "Ответ сервера:\n" + serverResponse);
            String result = getServerResponseBody(serverResponse);
            Main.SystemInfo.getLogger().log("getHttpNodesRequest", "Ответ:u\n" + result);
            return result;
        } catch (ClientProtocolException e) {
            throw new HotdogedException(e);
        } catch (IOException e2) {
            throw new HotdogedException(e2);
        }
    }

    public String postHttpPointRequest(String name, String email, String password, String about, NodeInfo nodeInfo) {
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("_name", name));
        nameValuePairList.add(new BasicNameValuePair("_email", email));
        nameValuePairList.add(new BasicNameValuePair("_password", password));
        nameValuePairList.add(new BasicNameValuePair("_about", about));
        try {
            String serverResponse = Utils.postHttpRequest(nodeInfo.getPntRequestUrl(), nameValuePairList);
            Main.SystemInfo.getLogger().log("postHttpPointRequest", "Ответ сервера:\n" + serverResponse);
            String result = getServerResponseBody(serverResponse);
            Main.SystemInfo.getLogger().log("postHttpPointRequest", "Ответ:\n" + result);
            return result;
        } catch (ClientProtocolException e) {
            Main.SystemInfo.getLogger().log("postHttpPointRequest", "HTTP исключение клиента:" + e.getMessage());
            String result2 = "HTTP исключение клиента:" + e.getMessage();
            return result2;
        } catch (HotdogedException e2) {
            Main.SystemInfo.getLogger().log("postHttpPointRequest", "Исключение сервера:" + e2.getMessage());
            String result3 = "Исключение сервера:" + e2.getMessage();
            return result3;
        } catch (IOException e3) {
            Main.SystemInfo.getLogger().log("postHttpPointRequest", "HTTP исключение ввода/вывода:" + e3.getMessage());
            String result4 = "HTTP исключение ввода/вывода:" + e3.getMessage();
            return result4;
        }
    }

    private String getServerResponseBody(String serverResponse) {
        if (TextUtils.isEmpty(serverResponse)) {
            return null;
        }
        int start = serverResponse.indexOf("<body>");
        if (start >= 0) {
            int end = serverResponse.indexOf("</body>", start);
            if (end >= 0) {
                return serverResponse.substring(start + 6, end).trim();
            }
            return serverResponse.substring(start + 6).trim();
        }
        return serverResponse;
    }

    public void pointRequestResultReceived(String response, NodeInfo nodeInfo) {
        setResult(1);
        if (response != null) {
            String address = parsePointRequestResponse(response);
            if (address != null) {
                addServerRecord(nodeInfo.getIpaddress(), nodeInfo.getFtnAddress(), address, this.password, this.name, nodeInfo.getPreferredAreasUrl());
                showMessage("Твой запрос был успешео отправлен системному оператору.\nТебе назначен ФИДО-адрес: " + address + ".\n\nОжидай, пока твой адрес будет активирован на ноде, после того как ты получишь ответ - сможешь использовать своего ФИДО-пойнта.\nВсе настройки ХотДога будут произведены автоматически.", true);
                return;
            }
            showMessage("Ошибка обработки твоего запроса:\n" + response + "\n\nСвяжись с системным оператором по емейлу и дай ему знать: " + nodeInfo.getEmail(), false);
            return;
        }
        showMessage("Запрос вернулся без ответа", true);
    }

    private void addServerRecord(String bossIp, String bossNode, String pointAddress, String password, String name, String areasUrl) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Constants.INTENT_EXTRA_SERVERACTIVE, (Integer) 1);
            cv.put(Constants.INTENT_EXTRA_SERVERNAME, bossNode);
            cv.put(Constants.INTENT_EXTRA_SERVERIP, bossIp);
            cv.put(Constants.INTENT_EXTRA_SERVERAREASURL, areasUrl);
            cv.put(Constants.INTENT_EXTRA_SERVERCODEPAGE, "Cp866");
            cv.put(Constants.INTENT_EXTRA_KEEPMSGAMOUNTPERGROUP, Integer.valueOf((int) HttpStatus.SC_OK));
            cv.put(Constants.INTENT_EXTRA_KEEPMSGDAYSPERGROUP, (Integer) 30);
            cv.put(Constants.INTENT_EXTRA_NAME, name);
            cv.put(Constants.INTENT_EXTRA_ADDRESS, pointAddress);
            cv.put(Constants.INTENT_EXTRA_SERVERQUOTING, "FTN");
            cv.put(Constants.INTENT_EXTRA_SIGNATURE, getString(R.string.default_signature));
            Uri resultUri = getContentResolver().insert(SERVERS_URI, cv);
            ConfigureServerActivity.insertOrUpdateServerCredentials(this, resultUri.getLastPathSegment(), pointAddress, password);
            ConfigureServerActivity.createServerFolders(this, resultUri);
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось добавить запись ноды: " + e.getMessage(), 1).show();
            Main.SystemInfo.getLogger().log("addServerRecord", "Не удалось добавить запись ноды: " + e.getMessage());
        }
    }

    private String parsePointRequestResponse(String response) {
        if (TextUtils.isEmpty(response)) {
            return null;
        }
        String[] sa = response.split("\n", 2);
        if (sa.length == 2 && sa[0].equalsIgnoreCase("OK")) {
            String[] rsp = sa[1].trim().split("\n", 2);
            if (rsp.length == 1) {
                return rsp[0].trim();
            }
            return null;
        }
        return null;
    }

    protected void submitRequest() {
        this.name = this.etName.getText().toString();
        this.email = this.etEmail.getText().toString();
        this.password = this.etPassword.getText().toString();
        this.about = this.etAbout.getText().toString();
        if (TextUtils.isEmpty(this.name) || TextUtils.isEmpty(this.email) || TextUtils.isEmpty(this.password) || TextUtils.isEmpty(this.about)) {
            Toast.makeText(this, "Все поля должны быть заполнены в", 1).show();
        } else if (this.password.length() > 8) {
            Toast.makeText(this, "missing STRING_LITERAL", 1).show();
        } else if (this.nodesAdapter == null || this.nodesAdapter.getCount() == 0) {
            Toast.makeText(this, "Не выбрана нода для запроса. Повтори снова позже.", 1).show();
        } else {
            NodeInfo nodeInfo = (NodeInfo) this.lvNode.getSelectedItem();
            sendPointRequest(this.name, this.email, this.password, this.about, nodeInfo);
        }
    }

    private void showMessage(String message, boolean dismissActivity) {
        startActivity(new Intent(this, MessageActivity.class).putExtra("message", message));
        if (dismissActivity) {
            finish();
        }
    }

    private void sendPointRequest(String name, String email, String password, String about, NodeInfo nodeInfo) {
        if (nodeInfo.getRequestBy().equalsIgnoreCase(HttpHost.DEFAULT_SCHEME_NAME)) {
            new AsyncRequestSender().execute(name, email, password, about, nodeInfo);
        } else if (nodeInfo.getRequestBy().equalsIgnoreCase("email")) {
            sendEmailRequest(name, email, password, about, nodeInfo);
        }
    }

    private void sendEmailRequest(String name, String email, String password, String about, NodeInfo nodeInfo) {
        try {
            setResult(1);
            Intent emailIntent = new Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", nodeInfo.getEmail(), null));
            emailIntent.putExtra("android.intent.extra.SUBJECT", nodeInfo.getFtnAddress() + " запрос  пойнта");
            String msgText = String.format(getString(R.string.emailRequestText), nodeInfo.getSysop(), nodeInfo.getFtnAddress(), name, password, email, about);
            emailIntent.putExtra("android.intent.extra.TEXT", msgText + "\n");
            String pointAddress = nodeInfo.getFtnAddress() + ".999";
            addServerRecord(nodeInfo.getIpaddress(), nodeInfo.getFtnAddress(), pointAddress, password, name, null);
            showMessage("Твой запрос был успешно отправлен системному оператору.\nВременный ФИДО-адрес записан в настройках твоего ХотДога: " + pointAddress + ".\n\nОжидай, пока твой запрос будет обработан сисопом, ты получишь от него правильный адрес и сможешь использовать своего ФИДО-пойнта.\nУбедись, что написал ФИДО-адрес правильно в настройках этой ноды.", true);
            startActivity(Intent.createChooser(emailIntent, "Отправка мейла..."));
        } catch (Exception e) {
            Log.e("sendEmailRequest", "Невозможно отправить e-mail: " + e.getMessage());
            Toast.makeText(this, "Невозможно отправить e-mail: " + e.getMessage(), 1).show();
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }
}
