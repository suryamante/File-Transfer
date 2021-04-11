package com.suryamante.socketprogramming;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
@SuppressWarnings("ALL")
public class Home extends AppCompatActivity {
    private Socket cs = null;
    private ServerSocket ss = null;
    private static int portNumber;
    private static String ipAddress;
    private static Queue<String> downloadQueue = new LinkedList<>();
    private DataOutputStream ostream = null;
    private RecyclerView recyclerView;
    private Stack<String> stack = new Stack<>();
    private Socket downloadSocket=null;
    private Thread downloadThread=null;
    private Stack<Integer> stack2 = new Stack<>();
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private int id = 0, currentPosition = 0;
    private TextView downloadFileName = null, downloadSpeed = null;
    private DataInputStream istream = null;
    public static boolean notDownloading = true;
    private FileOutputStream fout = null;
    private Handler mainHandler = new Handler();
    private FileInputStream fin = null;
    private BufferedOutputStream bout = null;
    private String response = "";
    private File file = null, dir = null;
    public static int downloadQueueStatus = 1;
    private BufferedInputStream bin = null;
    private ProgressDialog progressDialog;
    private static final String endMessage = "i-am-done-if-you-have-something-then-try-another-request";
    private RelativeLayout homeLayout = null, dataLayout = null, connectionLayout = null, downloadLayout = null;
    private ProgressBar downloadProgressBar = null;
    private Button startServer = null, requestServer = null;
    private com.google.android.material.textfield.TextInputLayout ipAddressLayout = null, portLayout = null;
    private com.google.android.material.textfield.TextInputEditText ipAddressEditText = null, portEditText = null;
    private ArrayList<String> list = null;
    private static String currentDirectory = "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
    }

    private void initializeDataList(ArrayList<String> list) {
        recyclerView = findViewById(R.id.data_View);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);
//        recyclerView.scrollToPosition(currentPosition);
    }

    private void initializeViews() {
        homeLayout = findViewById(R.id.home_Layout);
        dataLayout = findViewById(R.id.data_Layout);
        ipAddressEditText = findViewById(R.id.ip_address);
        portEditText = findViewById(R.id.port);
        downloadProgressBar = findViewById(R.id.download_Progress);
        downloadSpeed = findViewById(R.id.download_Speed);
        downloadFileName = findViewById(R.id.download_File_Name);
        downloadLayout = findViewById(R.id.download_Layout);
        dataLayout = findViewById(R.id.data_Layout);
        ipAddressLayout = findViewById(R.id.ip_address_layout);
        portLayout = findViewById(R.id.port_layout);
        connectionLayout = findViewById(R.id.connection_layout);
        recyclerView = findViewById(R.id.data_View);
        startServer = findViewById(R.id.start_Server);
        requestServer = findViewById(R.id.request_Server);
    }

    public void requestServer(View view) {
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");
        progressDialog.show();
        if(!validConfig()){
            return;
        }else{
            ipAddress=ipAddressEditText.getText().toString();
            portNumber=Integer.parseInt(portEditText.getText().toString());
        }
//        ipAddress = "192.168.43.186";
//        portNumber = 2221;

//        new Thread(new _Thread()).start();
        new Thread() {
            public void run() {
                try {
                    cs = new Socket(InetAddress.getByName(ipAddress), portNumber);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("msg", "92: " + "Socket created successfully.");
                        }
                    });
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("socketerror", "98: " + e.getMessage());
                            progressDialog.dismiss();
                            Snackbar.make(connectionLayout,ipAddress+"/"+portNumber+" is offline",Snackbar.LENGTH_LONG).show();                        }
                    });
                    return;
                }

                //socket created successfully
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        homeLayout.setVisibility(View.GONE);
                        dataLayout.setVisibility(View.VISIBLE);
                        connectionLayout.setVisibility(View.GONE);
                    }
                });

                //initialising streams
                try {
                    ostream = new DataOutputStream(cs.getOutputStream());
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "122: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }
                try {
                    istream = new DataInputStream(cs.getInputStream());
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "137: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
                startCommunication();
            }
        }.start();

    }

    private boolean validConfig() {
        portLayout.setHelperText("");
        ipAddressLayout.setHelperText("");

        String portNumber = portEditText.getText().toString();
        String ipAddress = ipAddressEditText.getText().toString();
        if (!validIP(ipAddress)) {
            ipAddressLayout.setHelperText("Invalid IPv4 address");
            return false;
        } else if (portNumber.length() < 1 || (Integer.parseInt(portNumber) < 0 || Integer.parseInt(portNumber) > 65535)) {
            portLayout.setHelperText("Invalid port number");
            return false;
        } else {
            return true;
        }
    }

    private boolean validIP(String ipAddress) {
        int i = 0;
        if (ipAddress.length() < 7) {
            return false;
        }
        int octetCount = 0;
        while (i < ipAddress.length()) {
            StringBuilder octet = new StringBuilder("");
            while (i < ipAddress.length() && ipAddress.charAt(i) != '.') {
                octet.append(ipAddress.charAt(i));
                i++;
            }
            if (octet.length() < 1 || octet.length() > 3) {
                return false;
            }
            if (Integer.parseInt(octet.toString()) < 0 || Integer.parseInt(octet.toString()) > 255) {
                return false;
            }
            i++;
            octetCount++;
        }
        return octetCount == 4;
    }

    private void startCommunication() {
        new Thread() {
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(Home.this);
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        progressDialog.setMessage("Loading...");
                        progressDialog.show();
                    }
                });
                //requesting required data from server
                try {
                    ostream.writeUTF("*" + currentDirectory);
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "162: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }

                //First response
                try {
                    response = istream.readUTF();
                    if (!response.equals("Ok")) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    cs.close();
                                } catch (IOException e) {
                                    Log.e("socketerror", "179: " + e.getMessage());
                                }
                            }
                        });
                        return;
                    }
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "192: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }

                try {
                    ostream.writeUTF("Ok");
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "208: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }

                try {
                    if (istream.readUTF().equals("***")) {
                        ostream.writeUTF("***");
                        if (notDownloading) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadLayout.setVisibility(View.VISIBLE);
                                    downloadProgressBar=findViewById(R.id.download_Progress);
                                    downloadSpeed=findViewById(R.id.download_Speed);
                                    downloadFileName=findViewById(R.id.download_File_Name);
                                    downloadProgressBar.setProgress(0);
                                    downloadFileName.setText(file.getName());
                                    downloadSpeed.setText("");
                                    notDownloading = false;
                                    startDownload(file, currentDirectory, ipAddress, ++id, portNumber);
                                    currentDirectory = stack.pop();
                                    currentPosition = stack2.pop();
                                }
                            });
                        } else {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    currentDirectory = stack.pop();
                                    currentPosition = stack2.pop();
                                    Toast.makeText(Home.this, "Cannot download two files at a time", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        progressDialog.dismiss();
                        return;
                    }
                } catch (IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cs.close();
                            } catch (IOException e) {
                                Log.e("socketerror", "229: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }

                //getting data froms server

                response = "";
                while (true) {
                    try {
                        String tempResponse = istream.readUTF();
                        if (tempResponse.contains(endMessage)) break;
                        response += tempResponse;
                    } catch (IOException e) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    cs.close();
                                } catch (IOException e) {
                                    Log.e("socketerror", "250: " + e.getMessage());
                                }
                            }
                        });
                        return;
                    }
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        initializeDataList(getListFromRawData(response));
                    }
                });
            }
        }.start();
    }

    private void startDownload(File file, String currentDirectory, String ipAddress, int i, int portNumber) {
        new Thread() {
            int speed = 0;
            int p1 = 0, p2 = 0;
            final int N = 256;
            int progress = 0;
            long totalSize = 0, transfered = 0;
            FileOutputStream fout;
            BufferedOutputStream bout;
            DataOutputStream dout;
            DataInputStream din;

            public void run() {
                try {
                    Socket socket = new Socket(InetAddress.getByName(ipAddress), portNumber);
                    din = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());
                    fout = new FileOutputStream(file);
                    bout = new BufferedOutputStream(fout);
                    dout.writeUTF("**" + currentDirectory);

                    if (din.readUTF().equals("***")) {
                        dout.writeUTF("***");
                    } else {
                        bout.close();
                        fout.close();
                        din.close();
                        dout.close();
                        socket.close();
                        return;
                    }


                    totalSize = Long.parseLong(din.readUTF());
                    if (totalSize > 0) {
//                        listen(socket);
                        new Thread() {
                            public void run() {
                                while (socket.isConnected() && progress < 100) {
                                    try {
                                        speed = ((p1 - p2) * N * 8) / (1024 * 1024);
                                        p2 = p1;
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try{
                                                    downloadProgressBar.setProgress(progress);
                                                    downloadSpeed.setText(speed + " Mb/s");
                                                }catch (Exception e){
                                                    progress=100;
                                                    return;
                                                }
                                            }
                                        });
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadLayout.setVisibility(View.GONE);
                                    }
                                });
                                notDownloading = true;
                            }
                        }.start();
                    }
                    dout.writeUTF(totalSize + "");
                    Log.e("Service1", "" + totalSize);
                    while (socket.isConnected() && progress<100) {
                        try{
                            downloadSpeed.getText();
                        }catch (Exception e){
                            progress=100;
                            return;
                        }
                        String data = din.readUTF();
                        p1++;
                        transfered += data.length();
                        progress = (int) (transfered * 100 / totalSize);
                        if (data.equals(Home.endMessage)) {
                            dout.writeUTF(Home.endMessage);
                            bout.close();
                            fout.close();
                            din.close();
                            dout.close();
                            socket.close();
                            break;
                        }
                        for (int i = 0; i < data.length(); i++) {
                            bout.write((byte) data.charAt(i));
                        }
                    }
                } catch (Exception e) {
                    Log.e("tag", "getFile: " + e.toString());
                }
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void startServer(View view) {
//        new Thread(new _Thread1()).start();
    }

    private ArrayList<String> getListFromRawData(String response) {
        ArrayList<String> list = new ArrayList<>();
        String item = "";
        int i = 0;
        while (i < response.length()) {
            if (response.charAt(i) != '*') {
                item += response.charAt(i);
            } else {
                list.add(item);
                item = "";
            }
            i++;
        }
        return list;
    }

    public void request(View view) {
        dataLayout.setVisibility(View.GONE);
        homeLayout.setVisibility(View.GONE);
        connectionLayout.setVisibility(View.VISIBLE);
        ipAddressEditText.setText("192.168.43.186");
        portEditText.setText("2221");
    }

    public void cancelDownload(View view) {
        downloadProgressBar=null;
        downloadSpeed=null;
        downloadFileName=null;
        downloadLayout.setVisibility(View.GONE);
        notDownloading=true;
        try{
            file.delete();
            downloadSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            if (list.get(position).charAt(1) == ':') {
                holder.item_logo.setBackground(getDrawable(R.drawable.drives));
            } else if (isDirectory(list.get(position))) {
                holder.item_logo.setBackground(getDrawable(R.drawable.folder));
            } else {
                holder.item_logo.setBackground(getDrawable(R.drawable.file));
            }
            holder.item_text.setText(list.get(position));
            holder.item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String item = list.get(position);
                    stack.push(currentDirectory);
                    stack2.push(position);
                    dir = getExternalFilesDir("/Received");
                    dir.mkdir();
                    file = new File(dir + "/" + item);
                    if (currentDirectory.equals("/")) {
                        currentDirectory = item + "/";
                    } else {
                        currentDirectory += "/" + item;
                    }
                    startCommunication();
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private ArrayList<String> list;

        public Adapter(ArrayList<String> list) {
            this.list = list;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView item_text;
            private ImageView item_logo;
            private RelativeLayout item_layout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                item_text = itemView.findViewById(R.id.item_text);
                item_logo = itemView.findViewById(R.id.item_logo);
                item_layout = itemView.findViewById(R.id.item_layout);
            }
        }

    }

    private boolean isDirectory(String s) {
        int i = s.length() - 1;
        while (i >= 0 && s.charAt(i) != '.') {
            i--;
        }
        if (i < 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (connectionLayout.getVisibility() == View.VISIBLE) {
            dataLayout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
            connectionLayout.setVisibility(View.GONE);
        } else if (!stack.isEmpty()) {
            currentDirectory = stack.pop();
            currentPosition = stack2.pop();
            Log.e("socketerror", "401: " + currentDirectory);
            if (cs.isConnected()) {
                startCommunication();
            } else {
                Log.e("socketerror", "405: socket is connected");
            }
        } else if (dataLayout.getVisibility() == View.VISIBLE) {
            try {
                if (cs != null) {
                    cs.close();
                }
            } catch (IOException e) {
                Log.e("socketerror", "409: " + e.toString());
            }
            dataLayout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}