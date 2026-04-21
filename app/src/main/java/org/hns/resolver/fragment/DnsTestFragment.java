package org.hns.resolver.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.hns.resolver.Daedalus;
import org.hns.resolver.R;
import org.hns.resolver.util.Logger;
import org.hns.resolver.server.AbstractDnsServer;
import org.minidns.dnsmessage.DnsMessage;
import org.minidns.dnsmessage.Question;
import org.minidns.record.Record;
import org.minidns.source.NetworkDataSource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

public class DnsTestFragment extends ToolbarFragment {

    private class Type {
        private Record.TYPE type;
        private String name;

        private Type(String name, Record.TYPE type) {
            this.name = name;
            this.type = type;
        }

        private Record.TYPE getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static Thread mThread = null;
    private static Runnable mRunnable = null;
    private DnsTestHandler mHandler = null;

    // 🔥 SIMPLE CACHE (domain+type → result)
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dns_test, container, false);

        final TextView textViewTestInfo = view.findViewById(R.id.textView_test_info);

        ArrayList<Type> types = new ArrayList<Type>() {{
            add(new Type("A", Record.TYPE.A));
            add(new Type("NS", Record.TYPE.NS));
            add(new Type("CNAME", Record.TYPE.CNAME));
            add(new Type("MX", Record.TYPE.MX));
            add(new Type("TXT", Record.TYPE.TXT));
            add(new Type("AAAA", Record.TYPE.AAAA));
            add(new Type("TLSA", Record.TYPE.TLSA));
        }};

        final Spinner spinnerType = view.findViewById(R.id.spinner_type);
        spinnerType.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, types));

        final EditText textViewTestDomain = view.findViewById(R.id.autoCompleteTextView_test_url);
        textViewTestDomain.setHint("example.com");

        mRunnable = () -> {
            try {
String rawDomain = textViewTestDomain.getText().toString().trim();
Record.TYPE selectedType = ((Type) spinnerType.getSelectedItem()).getType();

String testDomain = rawDomain;

if (selectedType == Record.TYPE.TLSA && !testDomain.startsWith("_")) {
    testDomain = "_443._tcp." + testDomain;
}

// 🔥 MAKE FINAL COPY FOR THREADS
final String finalDomain = testDomain;
final Record.TYPE finalType = selectedType;

                if (testDomain.isEmpty()) {
                    mHandler.obtainMessage(DnsTestHandler.MSG_DISPLAY_STATUS, "Enter a domain").sendToTarget();
                    mHandler.obtainMessage(DnsTestHandler.MSG_TEST_DONE).sendToTarget();
                    return;
                }

                String cacheKey = testDomain + "_" + selectedType;

                // 🔥 CACHE HIT
                if (cache.containsKey(cacheKey)) {
                    mHandler.obtainMessage(DnsTestHandler.MSG_DISPLAY_STATUS,
                            "[CACHE]\n" + cache.get(cacheKey)).sendToTarget();
                    mHandler.obtainMessage(DnsTestHandler.MSG_TEST_DONE).sendToTarget();
                    return;
                }

                StringBuilder resultText = new StringBuilder();

                ArrayList<AbstractDnsServer> dnsServers = new ArrayList<>();
                dnsServers.add(new AbstractDnsServer("82.68.70.162", 53));
                dnsServers.add(new AbstractDnsServer("82.68.70.163", 53));

                ExecutorService executor = Executors.newFixedThreadPool(dnsServers.size());
                List<Future<String>> futures = new ArrayList<>();

                for (AbstractDnsServer server : dnsServers) {
                    futures.add(executor.submit(() ->
                            testServer(server, finalType, finalDomain)
                    ));
                }

                for (Future<String> future : futures) {
                    try {
                        resultText.append(future.get(3, TimeUnit.SECONDS)).append("\n");
                    } catch (TimeoutException e) {
                        resultText.append("Timeout\n\n");
                    }
                }

                executor.shutdownNow();

                // 🔥 SAVE CACHE
                cache.put(cacheKey, resultText.toString());

                mHandler.obtainMessage(DnsTestHandler.MSG_DISPLAY_STATUS, resultText.toString()).sendToTarget();
                mHandler.obtainMessage(DnsTestHandler.MSG_TEST_DONE).sendToTarget();

            } catch (Exception e) {
                Logger.logException(e);
            }
        };

        Button startTestBut = view.findViewById(R.id.button_start_test);

        startTestBut.setOnClickListener(v -> {
            startTestBut.setEnabled(false);

            InputMethodManager imm = (InputMethodManager) Daedalus.getInstance()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            textViewTestInfo.setText("");

            if (mThread == null) {
                mThread = new Thread(mRunnable);
                mThread.start();
            }
        });

        mHandler = new DnsTestHandler();
        mHandler.setViews(startTestBut, textViewTestInfo);

        return view;
    }

    // 🔥 PARALLEL TEST FUNCTION
    private String testServer(AbstractDnsServer server, Record.TYPE type, String domain) {
        StringBuilder text = new StringBuilder();

        text.append("Server: ").append(server.getAddress()).append("\n");

        try {
            DnsQuery dnsQuery = new DnsQuery();

            DnsMessage.Builder message = DnsMessage.builder()
                    .addQuestion(new Question(domain, type))
                    .setId(new Random().nextInt())
                    .setRecursionDesired(true);

            long start = System.currentTimeMillis();

            DnsMessage response = dnsQuery.queryDns(
                    message.build(),
                    InetAddress.getByName(server.getAddress()),
                    server.getPort()
            );

            long end = System.currentTimeMillis();

            if (!response.answerSection.isEmpty()) {
                for (Record record : response.answerSection) {
                    text.append(record.getPayload().getType())
                            .append(": ")
                            .append(record.getPayload().toString())
                            .append("\n");
                }

                text.append("Time: ").append(end - start).append(" ms\n");
            } else {
                text.append("No answer\n");
            }

        } catch (SocketTimeoutException e) {
            text.append("Timeout\n");
        } catch (Exception e) {
            text.append("Error\n");
            Logger.logException(e);
        }

        text.append("\n");
        return text.toString();
    }

    @Override
    public void checkStatus() {
        menu.findItem(R.id.nav_dns_test).setChecked(true);
        toolbar.setTitle(R.string.action_dns_test);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopThread();
        mHandler.shutdown();
        mHandler = null;
    }

    private static void stopThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private static class DnsTestHandler extends Handler {
        static final int MSG_DISPLAY_STATUS = 0;
        static final int MSG_TEST_DONE = 1;

        private Button startTestBtn;
        private TextView textViewTestInfo;

        void setViews(Button btn, TextView tv) {
            startTestBtn = btn;
            textViewTestInfo = tv;
        }

        void shutdown() {
            startTestBtn = null;
            textViewTestInfo = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (startTestBtn == null) return;

            switch (msg.what) {
                case MSG_DISPLAY_STATUS:
                    textViewTestInfo.setText((String) msg.obj);
                    break;
                case MSG_TEST_DONE:
                    startTestBtn.setEnabled(true);
                    stopThread();
                    break;
            }
        }
    }

    private class DnsQuery extends NetworkDataSource {
        public DnsMessage queryDns(DnsMessage message, InetAddress address, int port) throws IOException {
            return queryUdp(message, address, port);
        }
    }
}
