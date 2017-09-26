package org.huangxk.recycle;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class CardOperator {
    private static final String LOG_TAG = "CardOperator";

    private static final byte[] MAGIC_DATA = new byte[] {
            'H','U','A','N','G','X','I','A','O','K','A','N','G','W','T','F'
    };

    private NfcAdapter mNfcAdapter;
    private Activity mActivity;
    private boolean mStarted = false;

    public static String[][] TECHLISTS;
    public static IntentFilter[] FILTERS;

    static {
        try {
            TECHLISTS = new String[][] {
                    {   IsoDep.class.getName()  },
                    {   NfcA.class.getName()    },
                    {   NfcB.class.getName()    },
                    {   MifareClassic.class.getName()   }
            };
            FILTERS = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public boolean initializeNfc(Activity activity) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null)    return false;
        if (mNfcAdapter.isEnabled() == false)   return false;

        mActivity = activity;
        return true;
    }

    public void startListen() {
        if (mNfcAdapter == null)    return;
        if (mStarted == true) return;
        PendingIntent pi = PendingIntent.getActivity(mActivity, 0, new Intent(mActivity, mActivity.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mNfcAdapter.enableForegroundDispatch(mActivity, pi,
                FILTERS, TECHLISTS);
        mStarted = true;
    }

    public void stopListen() {
        if (mNfcAdapter == null)    return;
        if (mStarted == false)  return;
        mNfcAdapter.disableForegroundDispatch(mActivity);
        mStarted = false;
    }

    public boolean readCardData(Intent intent) {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(LOG_TAG, "tag id: " + dumpByte(detectedTag.getId()));

        String[] techlist = detectedTag.getTechList();
        for (int i = 0; i < techlist.length; ++i) {
            Log.d(LOG_TAG, "tag support tech: " + techlist[i]);
            if (techlist[i].equals(NfcB.class.getName())) {
                return readShenfenzheng(detectedTag);
            } else if (techlist[i].equals(MifareClassic.class.getName())) {
                return readMifareClassic(detectedTag);
            }
        }
        Log.d(LOG_TAG, "read card failed");
        return false;
    }

    private boolean readMifareClassic(Tag tag) {
        MifareClassic mifare = MifareClassic.get(tag);
        /*
        if (true) {
            writeUser(mifare, null);
            return false;
        }
        */

        try {
            return readUser(mifare, TaskData.getInstance().getUserInfo());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mifare.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean readShenfenzheng(Tag tag) {
        return true;
    }

    public boolean writeUser(MifareClassic mifare, TaskData.UserInfo user) {
        try {
            mifare.connect();
            byte[][] write_data = {
                    MAGIC_DATA,
                    new byte[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
                    new byte[] {0x0, 0x0, 0x10, 0x1}, // 0 farmer
                    "李四".getBytes(),
                    "2017-07-24".getBytes(),
                    "其它信息".getBytes(),
            };

            writeSector(mifare, write_data[0], write_data[1], write_data[2], 1);
            writeSector(mifare, write_data[3], write_data[4], write_data[5], 2);
            writeSector(mifare, null, null, null, 0);

            mifare.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "read card ok");
        return true;
    }

    public boolean readUser(MifareClassic mifare, TaskData.UserInfo user) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        boolean ret = true;
        mifare.connect();
        {
            byte[] magic = readBlock(mifare, 1, 0);
            byte[] cardId = readBlock(mifare, 1, 1);
            byte[] cardType = readBlock(mifare, 1, 2);
            byte[] name = readBlock(mifare, 2, 0);
            byte[] registerDate = readBlock(mifare, 2, 1);
            byte[] otherInfo = readBlock(mifare, 2, 2);

            if (Arrays.equals(magic, MAGIC_DATA) == false) {
                ret = false;
            } else {
                user.mCardNum = getCardNum(cardId);
                user.mUserType = (cardType[0] << 24) | (cardType[1] << 16) | (cardType[2] << 8) | (cardType[3] << 0);
                user.mUserName = new String(name, charset);
                user.mRegisterDate = new String(registerDate, charset);
                user.mInfo = new String(otherInfo, charset);
            }
        }
        mifare.close();
        return ret;
    }

    private void writeSector(MifareClassic mifare, byte[] data0, byte[] data1, byte[] data2, int sector) throws IOException, InterruptedException {
        byte[][] writeBuffer = new byte[][] {
                new byte[16], new byte[16], new byte[16]
        };

        if (data0 != null)  System.arraycopy(data0, 0, writeBuffer[0], 0, Math.min(data0.length, 16));
        if (data1 != null)  System.arraycopy(data1, 0, writeBuffer[1], 0, Math.min(data1.length, 16));
        if (data2 != null)  System.arraycopy(data2, 0, writeBuffer[2], 0, Math.min(data2.length, 16));
        for (int i = 0; i < 3; ++ i) {
            if (mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                int bIndex = mifare.sectorToBlock(sector);
                mifare.writeBlock(bIndex + i, writeBuffer[i]);  Thread.sleep(1000); Log.d(LOG_TAG, "writing " + dumpByte(writeBuffer[i]));
            } else {
                Log.w(LOG_TAG, "failed 1");
            }
        }
    }

    private byte[] readBlock(MifareClassic mifare, int sector, int idx) throws IOException {
        if (mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT) == false) {
            return null;
        }
        int blockBase = mifare.sectorToBlock(sector);
        return mifare.readBlock(blockBase + idx);
    }

    private String getCardNum(byte[] data) {
        if (data == null)   return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            sb.append(String.format("%X", data[i]));
        }
        return sb.toString();
    }

    private String dumpByte(byte[] data) {
        return dumpByte(data, 0, data.length);
    }

    private String dumpByte(byte[] data, int offset, int len) {
        if (data == null)   return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            sb.append(String.format("0x%x ", data[offset + i]));
        }
        return sb.toString();
    }

    private static CardOperator sInstance = null;
    public static CardOperator getInstance() {
        if (sInstance == null) {
            sInstance = new CardOperator();
        }
        return sInstance;
    }
}
