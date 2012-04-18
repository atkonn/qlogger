#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <fcntl.h>

#include <string.h>
#include <jni.h>

#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <android/log.h>

#define LOG_TAG "libps"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define DEBUG 0

#define PRINTF(...) \
  do { \
    char __buf[256]; \
    snprintf(__buf, 256, __VA_ARGS__); \
    jstring jstr = (*(obj->env))->NewStringUTF(obj->env, __buf); \
    (*(obj->env))->CallBooleanMethod(obj->env, obj->result, obj->listAddMethod, jstr); \
  } while(0)

#include <pwd.h>


typedef struct _OBJ {
  JNIEnv *env;
  jclass runtimeExceptionClass;
  jclass stringClass;
  jclass listClass;
  jmethodID listConstructorMethod;
  jmethodID listAddMethod;
  jobject result;
} OBJ;

static char *
nexttoksep(char **strp, char *sep)
{
  char *p = strsep(strp,sep);
  return (p == 0) ? "" : p;
}
static char *nexttok(char **strp)
{
  return nexttoksep(strp, " ");
}

#define SHOW_PRIO 1
#define SHOW_TIME 2

static int display_flags = 0;

static int 
ps_line(OBJ *obj, int pid, int tid, char *namefilter)
{
  char statline[1024];
  char cmdline[1024];
  char user[32];
  struct stat stats;
  int fd, r;
  char *ptr, *name, *state;
  int ppid, tty;
  unsigned wchan, rss, vss, eip;
  unsigned utime, stime;
  int prio, nice, rtprio, sched;
  struct passwd *pw;
  
  sprintf(statline, "/proc/%d", pid);
  stat(statline, &stats);

  if(tid) {
    sprintf(statline, "/proc/%d/task/%d/stat", pid, tid);
    cmdline[0] = 0;
  } else {
    sprintf(statline, "/proc/%d/stat", pid);
    sprintf(cmdline, "/proc/%d/cmdline", pid);  
    fd = open(cmdline, O_RDONLY);
    if(fd == 0) {
      r = 0;
    } else {
      r = read(fd, cmdline, 1023);
      close(fd);
      if(r < 0) r = 0;
    }
    cmdline[r] = 0;
  }
  
  fd = open(statline, O_RDONLY);
  if(fd == 0) return -1;
  r = read(fd, statline, 1023);
  close(fd);
  if(r < 0) return -1;
  statline[r] = 0;

  ptr = statline;
  nexttok(&ptr); // skip pid
  ptr++;      // skip "("

  name = ptr;
  ptr = strrchr(ptr, ')'); // Skip to *last* occurence of ')',
  *ptr++ = '\0';       // and null-terminate name.

  ptr++;      // skip " "
  state = nexttok(&ptr);
  ppid = atoi(nexttok(&ptr));
  nexttok(&ptr); // pgrp
  nexttok(&ptr); // sid
  tty = atoi(nexttok(&ptr));
  
  nexttok(&ptr); // tpgid
  nexttok(&ptr); // flags
  nexttok(&ptr); // minflt
  nexttok(&ptr); // cminflt
  nexttok(&ptr); // majflt
  nexttok(&ptr); // cmajflt
#if 1
  utime = atoi(nexttok(&ptr));
  stime = atoi(nexttok(&ptr));
#else
  nexttok(&ptr); // utime
  nexttok(&ptr); // stime
#endif
  nexttok(&ptr); // cutime
  nexttok(&ptr); // cstime
  prio = atoi(nexttok(&ptr));
  nice = atoi(nexttok(&ptr));
  nexttok(&ptr); // threads
  nexttok(&ptr); // itrealvalue
  nexttok(&ptr); // starttime
  vss = strtoul(nexttok(&ptr), 0, 10); // vsize
  rss = strtoul(nexttok(&ptr), 0, 10); // rss
  nexttok(&ptr); // rlim
  nexttok(&ptr); // startcode
  nexttok(&ptr); // endcode
  nexttok(&ptr); // startstack
  nexttok(&ptr); // kstkesp
  eip = strtoul(nexttok(&ptr), 0, 10); // kstkeip
  nexttok(&ptr); // signal
  nexttok(&ptr); // blocked
  nexttok(&ptr); // sigignore
  nexttok(&ptr); // sigcatch
  wchan = strtoul(nexttok(&ptr), 0, 10); // wchan
  nexttok(&ptr); // nswap
  nexttok(&ptr); // cnswap
  nexttok(&ptr); // exit signal
  nexttok(&ptr); // processor
  rtprio = atoi(nexttok(&ptr)); // rt_priority
  sched = atoi(nexttok(&ptr)); // scheduling policy
  
  tty = atoi(nexttok(&ptr));
  
  if(tid != 0) {
    ppid = pid;
    pid = tid;
  }

  pw = getpwuid(stats.st_uid);
  if(pw == 0) {
    sprintf(user,"%d",(int)stats.st_uid);
  } else {
    strcpy(user,pw->pw_name);
  }
  
  char buf[256];
  if(!namefilter || !strncmp(name, namefilter, strlen(namefilter))) {
    snprintf(buf, 256, "%-9s %-5d %-5d %-6d %-5d", user, pid, ppid, vss / 1024, rss * 4);
    if(display_flags&SHOW_PRIO)
      snprintf(buf, 256,"%s %-5d %-5d %-5d %-5d", buf,prio, nice, rtprio, sched);
    snprintf(buf, 256,"%s %08x %08x %s %s", buf, wchan, eip, state, cmdline[0] ? cmdline : name);
    if(display_flags&SHOW_TIME)
      snprintf(buf, 256, "%s (u:%d, s:%d)", buf, utime, stime);

    PRINTF("%s\n", buf);
  }
  return 0;
}


static void 
ps_threads(OBJ *obj, int pid, char *namefilter)
{
  char tmp[128];
  DIR *d;
  struct dirent *de;

  sprintf(tmp,"/proc/%d/task",pid);
  d = opendir(tmp);
  if(d == 0) return;
  
  while((de = readdir(d)) != 0){
    if(isdigit(de->d_name[0])){
      int tid = atoi(de->d_name);
      if(tid == pid) continue;
      ps_line(obj, pid, tid, namefilter);
    }
  }
  closedir(d);  
}



static int 
ps_main(OBJ *obj, int argc, char **argv)
{
  DIR *d;
  char buf[256];
  struct dirent *de;
  char *namefilter = 0;
  int pidfilter = 0;
  int threads = 0;
  
  d = opendir("/proc");
  if(d == 0) return -1;

  while(argc > 1){
    if(!strcmp(argv[1],"-t")) {
      threads = 1;
    } else if(!strcmp(argv[1],"-x")) {
      display_flags |= SHOW_TIME;
    } else if(!strcmp(argv[1],"-p")) {
      display_flags |= SHOW_PRIO;
    }  else if(isdigit(argv[1][0])){
      pidfilter = atoi(argv[1]);
    } else {
      namefilter = argv[1];
    }
    argc--;
    argv++;
  }


  PRINTF("USER   PID   PPID  VSIZE  RSS    WCHAN  PC     NAME\n");

  while((de = readdir(d)) != 0){
    if(isdigit(de->d_name[0])){
      int pid = atoi(de->d_name);
      if(!pidfilter || (pidfilter == pid)) {
        ps_line(obj, pid, 0, namefilter);
        if(threads) ps_threads(obj, pid, namefilter);
      }
    }
  }
  closedir(d);
  return 0;
}



jobjectArray
Java_jp_co_qsdn_android_qlogger_commands_Ps_runJni( JNIEnv* env, jobject thiz ,jobjectArray argv, jobject result)
{
  OBJ obj;
  if (DEBUG) LOGV(">>> Java_jp_co_qsdn_android_qlogger_commands_Ps_runJni");

  obj.env = env;
  obj.result = result;
  obj.runtimeExceptionClass = (*obj.env)->FindClass(obj.env, "java/lang/RuntimeException");
  if (obj.runtimeExceptionClass == NULL) {
    goto error1;
  }
  if((*obj.env)->ExceptionOccurred(obj.env)) {
    goto error1;
  }
  if (DEBUG) LOGD("runtimeExceptionClass found.");

  obj.stringClass = (*obj.env)->FindClass(obj.env, "java.lang.String");
  if (obj.stringClass == NULL) {
    (*obj.env)->ThrowNew(obj.env, obj.runtimeExceptionClass, "unable to find java/lang/String");
    goto error1;
  }
  if((*obj.env)->ExceptionOccurred(obj.env)) {
    goto error1;
  }
  if (DEBUG) LOGD("stringClass found.");
  obj.listClass = (*obj.env)->FindClass(obj.env, "java/util/ArrayList");
  if (obj.listClass == NULL) {
    (*obj.env)->ThrowNew(obj.env, obj.runtimeExceptionClass, "unable to find java/util/ArrayList");
    goto error1;
  }
  if((*obj.env)->ExceptionOccurred(obj.env)) {
    goto error1;
  }
  if (DEBUG) LOGD("listClass found.");
  obj.listConstructorMethod = (*obj.env)->GetMethodID(obj.env, obj.listClass, "<init>", "()V");
  if (obj.listConstructorMethod == NULL) {
    (*obj.env)->ThrowNew(obj.env, obj.runtimeExceptionClass, "GetMethodID (listClass.<init>) failure");
    goto error1;
  }
  if((*obj.env)->ExceptionOccurred(obj.env)) {
    goto error1;
  }
  if (DEBUG) LOGD("listClass's constructor found.");
  obj.listAddMethod = (*obj.env)->GetMethodID(obj.env, obj.listClass, "add", "(Ljava/lang/Object;)Z");
  if (obj.listAddMethod == NULL) {
    (*obj.env)->ThrowNew(obj.env, obj.runtimeExceptionClass, "GetMethodID (listClass.add) failure");
    goto error1;
  }
  if((*obj.env)->ExceptionOccurred(obj.env)) {
    goto error1;
  }
  if (DEBUG) LOGD("listClass's add method found.");


  jsize ct = (*obj.env)->GetArrayLength(obj.env, argv);
  const char *_argv[256];
  int ii=0; 
  _argv[ii++] = "ps";
  for (; ii<ct+1; ii++) {
    jobject elemj = (*obj.env)->GetObjectArrayElement(obj.env, argv, ii-1);
    _argv[ii] = (*obj.env)->GetStringUTFChars(obj.env, elemj, 0);
    (*env)->DeleteLocalRef(env, elemj);
  }
  _argv[ii] = NULL;

  ps_main(&obj, (int)ct+1, (char **)_argv);

error1:
  if (DEBUG) LOGV("<<< Java_jp_co_qsdn_android_qlogger_commands_Ps_runJni");
  return;
}

jint
JNI_OnLoad(JavaVM* vm, void* reserved)
{
  JNIEnv* env;
  if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
    return -1;
  return JNI_VERSION_1_6;
}
