import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {

    private final JChannel communicationChannel;
    private final Map<String, Integer> hashTable;

    public DistributedMap() throws Exception {
        this.hashTable = new HashMap<>();

        this.communicationChannel = new JChannel(false);
        ProtocolStack stack = new ProtocolStack();
        this.communicationChannel.setProtocolStack(stack);

        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("224.7.7.7")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE());
        stack.init();

        this.communicationChannel.setReceiver(this);
        this.communicationChannel.setDiscardOwnMessages(true);
        this.communicationChannel.connect("distributed_map_cluster");
        this.communicationChannel.getState(null, 0);
    }

    @Override
    public boolean containsKey(String key) {
        return this.hashTable.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return this.hashTable.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        this.hashTable.put(key, value);
        putNotify(key, value);
    }

    private void putNotify(String key, Integer value) {
        PutNotification notification = new PutNotification(key, value);
        Message message = new Message(null, notification);
        try {
            this.communicationChannel.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer remove(String key) {
        Integer removed = this.hashTable.remove(key);
        removeNotify(key);
        return removed;
    }

    private void removeNotify(String key) {
        RemovalNotification notification = new RemovalNotification(key);
        Message message = new Message(null, notification);
        try {
            this.communicationChannel.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Message msg) {
        System.out.println("Otrzymalem wiadomosc od " + msg.getSrc() + ": " + msg.getObject());

        Object notification = msg.getObject();

        if (notification instanceof RemovalNotification) {
            System.out.println("To powiadomienie o usunieciu elemnetu ("
                    + ((RemovalNotification) notification).key
                    + ")");
            this.hashTable.remove(((RemovalNotification) notification).key);
        } else if (notification instanceof PutNotification) {
            System.out.println("To powiadomienie o dodaniu elementu ("
                    + ((PutNotification) notification).key
                    + " -> "
                    + ((PutNotification) notification).value
                    + ")");
            this.hashTable.put(((PutNotification) notification).key, ((PutNotification) notification).value);
        }
    }


    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (this.hashTable) {
            Util.objectToStream(this.hashTable, new DataOutputStream(output));
        }
        System.out.println("Ktos pytal o stan poczatkowy");
    }

    @Override
    public void setState(InputStream input) throws Exception {

        Map<String, Integer> currentHashTableState;
        currentHashTableState = (Map<String, Integer>) Util.objectFromStream(new DataInputStream(input));

        synchronized (this.hashTable) {
            this.hashTable.clear();
            this.hashTable.putAll(currentHashTableState);
        }
        System.out.println("Ustalilem poczatkowy stan");
    }

    @Override
    public void viewAccepted(View view) {
        if (view instanceof MergeView) {
            // wybranie w osobnym watku nowej primary partition po merge'u
            MergeViewHandler handler = new MergeViewHandler(this.communicationChannel, (MergeView) view);
            handler.start();
        }
    }


    private static class MergeViewHandler extends Thread {
        JChannel communicationChannel;
        MergeView view;

        private MergeViewHandler(JChannel communicationChannel, MergeView view) {
            this.communicationChannel = communicationChannel;
            this.view = view;
        }

        public void run() {
            List<View> subgroups = view.getSubgroups();

            // wybranie jako primary partiotion grupy najliczniejszej
            View primaryPartitionView = subgroups.get(0);
            for (View view : subgroups) {
                if (view.size() > primaryPartitionView.size())
                    primaryPartitionView = view;
            }

            Address localAddress = this.communicationChannel.getAddress();

            if (!primaryPartitionView.getMembers().contains(localAddress)) {
                System.out.println("Nie jestes czlonkiem primary partition ("
                        + primaryPartitionView
                        + "), ustalisz stan od nowa");
                try {
                    this.communicationChannel.getState(null, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Jestes czlonkiem primary partition ("
                        + primaryPartitionView
                        + "), nic nie zrobisz");
            }
        }
    }

    public void close() {
        this.communicationChannel.close();
    }
}
