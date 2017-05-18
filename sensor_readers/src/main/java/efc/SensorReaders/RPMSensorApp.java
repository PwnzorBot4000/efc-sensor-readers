package efc.SensorReaders;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RPMSensorApp implements SerialPortEventListener {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private Gson gson;
    private final String EFC_URL = "http://efcv2.azurewebsites.net/api/ValuesApi/PostValuesi";

    public static void main(String[] args) {
        System.out.println("Hello World!");
//        System.out.println(new SimpleDateFormat("YYYY-MM-d HH:MM:ss").format(new Date()).toString());
        new RPMSensorApp();
    }

    public RPMSensorApp() {
        client = new OkHttpClient();
        gson = new Gson();
        SerialPort serialPort = new SerialPort("/dev/ttyUSB1");
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE,
                                 false,
                                 false);

            String valueBuffer[] = new String[30];
            int i = 0;
            do {
                String value = "";
                String inputChar = serialPort.readString(1);
//                System.out.println(inputChar);
                while ((int) inputChar.charAt(0) != 10) {
                    if ((int) inputChar.charAt(0) != 13) value += inputChar;
                    inputChar = serialPort.readString(1);
                }
                // if (inputChar.equals("\n")) {
                valueBuffer[i] = value;
                System.out.println(value);
                i++;
                i %= 30;
                if (i == 0) {
                    ValuesI valuesI = new ValuesI();
                    valuesI.value = Integer.parseInt(valueBuffer[9]);
                    valuesI.sensoriId = 1;
                    valuesI.timestamp = new SimpleDateFormat("YYYY-MM-d HH:MM:ss").format(new Date()).toString();
                    System.out.println("Senting: " + gson.toJson(valuesI));
                    Request.Builder builder = new Request.Builder().post(RequestBody.create(JSON,
                                                                                            gson.toJson(valuesI)));
                    Request request = builder.url(EFC_URL).build();
                    client.newCall(request).enqueue(new Callback() {

                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                System.out.println(response.body().charStream().toString());
                            } else {
                                System.out.println(response.code() + "");
                                System.out.println(response.message());
                            }
                        }

                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }
                    });
                    // }
                }
            } while (true);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.BREAK) {
            System.out.println("Event arrived!");
        }
    }
}
