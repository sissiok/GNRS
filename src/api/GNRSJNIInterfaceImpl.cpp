#include <jni.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include "../client/gnrsclient.h"

#include "GNRSJNIInterface.h"
#define MAX_LOOKUP_RESP_LEN 1024

/* handle into the gnrs client library */
static GNRSClient *gnrs_client;

JNIEXPORT void JNICALL Java_GNRSJNIInterface_init(
		JNIEnv * env, jobject jobj, jstring j_config_file){

	jboolean iscopy;
	const char *config_file = env->GetStringUTFChars(j_config_file, 
								&iscopy);
	printf("libgnrs-jni: init with config file: %s\n", config_file);
	int err = 0;

	//invoke the gnrs library init function
	gnrs_client = new GNRSClient((char*)config_file);

	if(err){
		jclass ex = env->FindClass("java/lang/Exception");
		env->ThrowNew(ex, NULL);
	}
	env->ReleaseStringUTFChars(j_config_file, config_file);
}

JNIEXPORT void JNICALL Java_GNRSJNIInterface_insert(
		JNIEnv * env, jobject jobj, jstring j_guid, jstring j_locators){

	jboolean iscopy;
	const char *guid = env->GetStringUTFChars(j_guid, &iscopy);
	const char *locators = env->GetStringUTFChars(j_locators, &iscopy);
	printf("libgnrs-jni: insert guid: %s locator(s): %s\n", guid, locators);
	int err = 0;

	//invoke the gnrs library function
	err = gnrs_client->insert((char*)guid, (char*)locators);

	if(err){
		jclass ex = env->FindClass("java/lang/Exception");
		env->ThrowNew(ex, NULL);
	}
	env->ReleaseStringUTFChars(j_guid, guid);
	env->ReleaseStringUTFChars(j_locators, locators);
}

JNIEXPORT void JNICALL Java_GNRSJNIInterface_update(
		JNIEnv * env, jobject jobj, jstring j_guid, jstring j_locators){

	jboolean iscopy;
	const char *guid = env->GetStringUTFChars(j_guid, &iscopy);
	const char *locators = env->GetStringUTFChars(j_locators, &iscopy);
	printf("libgnrs-jni: update guid: %s locator(s): %s\n", guid, locators);

	int err = 0;
	//invoke the gnrs library function
	//TODO - maps to insert; update semantics undefined
	err = gnrs_client->insert((char*)guid, (char*)locators);

	if(err){
		jclass ex = env->FindClass("java/lang/Exception");
		env->ThrowNew(ex, NULL);
	}
	env->ReleaseStringUTFChars(j_guid, guid);
	env->ReleaseStringUTFChars(j_locators, locators);
}

JNIEXPORT jstring JNICALL Java_GNRSJNIInterface_lookup(
				JNIEnv * env, jobject jobj, jstring j_guid){

	char response[MAX_LOOKUP_RESP_LEN];
	jboolean iscopy;
	const char *guid = env->GetStringUTFChars(j_guid, &iscopy);
	printf("libgnrs-gni: lookup guid %s\n", guid);

	int err = 0;
	//invoke the gnrs library function
	err = gnrs_client->lookup((char*)guid, response, MAX_LOOKUP_RESP_LEN);

	if(err){
		jclass ex = env->FindClass("java/lang/Exception");
		env->ThrowNew(ex, NULL);
	}

	env->ReleaseStringUTFChars(j_guid, guid);
	return env->NewStringUTF(response);
}
