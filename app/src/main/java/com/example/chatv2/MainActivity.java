package com.example.chatv2;


import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



import com.example.chatv2.messageSet.Message;
import com.example.chatv2.messageSet.MessageAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private EditText editText;
    public static final String clientId = "jmsmqttdemo"; //192.168.43.2---///10.0.2.2
    public static final String serverURI = "tcp://192.168.1.171:61616"; //replace with your ip
    String subscribeTopic = "travelPath";
    MqttAndroidClient client;
    String mqttUsername = "admin";
    String mqttPassword = "admin";

    MessageAdapter messageAdapter;
    private ListView messagesView;

    static Random randomGenerator;
    String color;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=findViewById(R.id.editText);
        messageAdapter=new MessageAdapter(getApplicationContext());
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        connect();
        randomGenerator = new Random();
        int newColor = 0x1000000 + randomGenerator.nextInt(0x1000000);
       color = generateColor(newColor);
    }



    public void sendMessage(View view) {

        String message = editText.getText().toString();


        if (message.length() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name","deneme");
                jsonObject.put("color", color);
                jsonObject.put("message", message);
                jsonObject.put("belongsToCurrentUser", false);




            } catch (JSONException e) {
                e.printStackTrace();
            }

            MqttMessage  mqttMessage=new MqttMessage(jsonObject.toString().getBytes());

            try {
                client.publish(subscribeTopic,mqttMessage);
                final Message myMessage = new Message(message, "deneme",color,true);

                messageAdapter.add(myMessage);
                messagesView.setSelection(messagesView.getCount() - 1);
                editText.setText("");
            }catch (MqttException e){
                Toast.makeText(getApplicationContext(), " Message Send Error" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

        }

    }


    private void connect() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setUserName(mqttUsername);

        connectOptions.setPassword(mqttPassword.toCharArray());

        client = new MqttAndroidClient(this, serverURI, clientId);
        try {
            client.connect(connectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    subscribe();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable e) {

                    Toast.makeText(getApplicationContext(), " Server Error" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), " Server Error" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void subscribe() {

        try {
            client.subscribe(subscribeTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            try {

                               final JSONObject obj = new JSONObject(message.toString());

                                // if the clientID of the message sender is the same as our's it was sent by us
                                boolean belongsToCurrentUser =obj.getBoolean("belongsToCurrentUser");
                                // since the message body is a simple string in our case we can use json.asText() to parse it as such
                                // if it was instead an object we could use a similar pattern to data parsing
                                final Message message = new Message(obj.getString("message"), obj.getString("name"),obj.getString("color"),belongsToCurrentUser);
                                 if(!obj.getString("name").equals("deneme")){
                                     messageAdapter.add(message);
                                     messagesView.setSelection(messagesView.getCount() - 1);
                                 }

                                ///Toast.makeText(getApplicationContext(), "Message"+obj, Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Subscribe Server Error" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }


                        }
                    });
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static String generateColor(int colorInt) {

        return "#" + Integer.toHexString(colorInt).substring(1, 7);
    }

}
