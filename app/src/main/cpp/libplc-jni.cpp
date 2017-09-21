#include <jni.h>
#include <string>
#include <android/log.h>
#include "snap7/snap7.h"

#define LOG_TAG "PLC_JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static TS7Client *Client = NULL;

static void S7API CliCompletion(void *usrPtr, int opCode, int opResult) {
    LOGD("opCode = %x, result = %x", opCode, opResult);
}

static TS7Client* getClient() {
    if (Client == NULL) {
        Client = new TS7Client();
        Client->SetAsCallback(CliCompletion, NULL);
    }
    return Client;
}

static bool checkResult(int result, const char* function) {
    if (result == 0) {
        return true;
    }
    if (result < 0) {
        LOGE("Libaray error in %s", function);
    } else {
        LOGE("%s", CliErrorText(result).c_str());
    }
    return false;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_huangxk_recycle_plc_Connection_connectToPLC(JNIEnv *env, jobject obj, jstring ip) {
    bool ret = false;
    const char *native_ip = env->GetStringUTFChars(ip, 0);
    {
        LOGD("IP = %s", native_ip);
        getClient()->SetConnectionParams(native_ip, 0x0200, 0x0201);
        ret = checkResult(Client->Connect(), __FUNCTION__);
    }
    env->ReleaseStringUTFChars(ip, native_ip);
    return ret;
}

JNIEXPORT void JNICALL
Java_org_huangxk_recycle_plc_Connection_closeConnection(JNIEnv *env, jobject obj) {
    LOGD("Disconnect");
    getClient()->Disconnect();
}

JNIEXPORT jbyteArray JNICALL
Java_org_huangxk_recycle_plc_Connection_readMerker(JNIEnv *env, jobject obj, jint start, jint count) {
    byte readData[count];
    jbyteArray output = NULL;

    TS7DataItem Items;
    Items.Area     = S7AreaMK;
    Items.WordLen  = S7WLByte;
    Items.DBNumber = 0;
    Items.Start    = start;
    Items.Amount   = count;
    Items.pdata    = &readData;

    int res = getClient()->ReadMultiVars(&Items, 1);
    if (checkResult(res, __FUNCTION__)) {
        output = env->NewByteArray(count);
        env->SetByteArrayRegion(output, 0, count, (jbyte*)readData);
    }
    return output;
}

JNIEXPORT jbyteArray JNICALL
Java_org_huangxk_recycle_plc_Connection_readDB(JNIEnv *env, jobject /*obj*/, jint start, jint count) {
    byte readData[count];
    jbyteArray output = NULL;

    TS7DataItem Items;
    Items.Area     = S7AreaDB;
    Items.WordLen  = S7WLByte;
    Items.DBNumber = 1;
    Items.Start    = start;
    Items.Amount   = count;
    Items.pdata    = &readData;

    int res = getClient()->ReadMultiVars(&Items, 1);
    if (checkResult(res, __FUNCTION__)) {
        output = env->NewByteArray(count);
        env->SetByteArrayRegion(output, 0, count, (jbyte*)readData);
    }
    return output;
}

JNIEXPORT jbyteArray JNICALL
Java_org_huangxk_recycle_plc_Connection_readArea(JNIEnv *env, jobject /*obj*/, jint Area, jint DBNum, jint start, jint count) {
    byte readData[count];
    jbyteArray output = NULL;

    TS7DataItem Items;
    Items.Area     = (int)Area;
    Items.WordLen  = S7WLByte;
    Items.DBNumber = (int)DBNum;
    Items.Start    = (int)start;
    Items.Amount   = (int)count;
    Items.pdata    = &readData;

    int res = getClient()->ReadMultiVars(&Items, 1);
    if (checkResult(res, __FUNCTION__)) {
        output = env->NewByteArray(count);
        env->SetByteArrayRegion(output, 0, count, (jbyte*)readData);
    }
    return output;
}

JNIEXPORT jboolean JNICALL
Java_org_huangxk_recycle_plc_Connection_setBit(JNIEnv */*env*/, jobject /*obj*/, jint addr_byte, jint addr_bit, jboolean on) {
    byte readData;

    TS7DataItem Items;
    Items.Area     = S7AreaMK;
    Items.WordLen  = S7WLByte;
    Items.DBNumber = 0;
    Items.Start    = addr_byte;
    Items.Amount   = 1;
    Items.pdata    = &readData;

    int res = getClient()->ReadMultiVars(&Items, 1);
    if (checkResult(res, __FUNCTION__) == false) {
        return 0;
    }

    byte olddata= readData;
    if (on) {
        readData |= (1 << addr_bit);
    } else {
        readData &= ~(1 << addr_bit) ;
    }
    LOGD("SetBit, from %x to %x", olddata, readData);

    res = getClient()->WriteArea(S7AreaMK, 0, addr_byte, 1, S7WLByte, &readData);
    return checkResult(res, __FUNCTION__);
}

}