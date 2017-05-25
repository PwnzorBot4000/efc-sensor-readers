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

public class RPMSensorApp {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private Gson gson;
    private final String EFC_URL = "http://efcv2.azurewebsites.net/api/ValuesApi/PostValuesi";
    private final String FEEDBACK_URL = "http://localhost/api/motor/setrpmreading";
    
    private static String port = null;

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.err.println("You should provide a port argument");
            System.exit(1);
        }
        port = args[0];
        new RPMSensorApp();
    }

    public RPMSensorApp() {
        client = new OkHttpClient();
        gson = new Gson();
        System.out.println("Opening port " + port);
        SerialPort serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE,
                                 false,
                                 false);
            System.out.println("Port " + port + " opened successfully");

            String valueBuffer[] = new String[30];
            int i = 0;
            do {
                String value = "";
                String inputChar = serialPort.readString(1);
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
                    RpmSensorValue rpmSensorValue = new RpmSensorValue();
                    
                    rpmSensorValue.setValue(Integer.parseInt(valueBuffer[valueBuffer.length-1]));
                    rpmSensorValue.setSensoriId(1);
                    rpmSensorValue.setTimeStamp(new SimpleDateFormat("YYYY-MM-d HH:MM:ss").format(new Date()).toString());
                    System.out.println("Senting: " + gson.toJson(rpmSensorValue));
                    Request.Builder builder = new Request.Builder().post(RequestBody.create(JSON,
                                                                                            gson.toJson(rpmSensorValue)));
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
                    
                    Request.Builder builder2 = new Request.Builder().post(RequestBody.create(JSON, valueBuffer[9]));
                    Request request2 = builder2.url(FEEDBACK_URL).build();
                    client.newCall(request).enqueue(new Callback() {
                        
                        public void onResponse(Call call, Response response) throws IOException {
                            
                        }
                        
                        public void onFailure(Call call, IOException e) {
                            
                        }
                    });
                    // }
                }
            } while (true);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }
}
