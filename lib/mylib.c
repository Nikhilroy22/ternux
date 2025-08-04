#include <jni.h>
#include <unistd.h>
#include <fcntl.h>
#include <pty.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <stdio.h>

static int master_fd = -1;
static int child_pid = -1;

JNIEXPORT jint JNICALL
Java_com_npuja_nikhil_NativePTY_startShell(JNIEnv *env, jobject thiz, jstring shell_path) {
    const char *shell = (*env)->GetStringUTFChars(env, shell_path, 0);

    struct winsize sz = {24, 80, 0, 0};
    pid_t pid = forkpty(&master_fd, NULL, NULL, &sz);
    if (pid == 0) {
        // -------------------------
        // ✅ CHILD: use execve
        // -------------------------

        const char *app_dir = "/data/data/com.npuja.nikhil/files/home";

        // args: ["bash", "--rcfile", "/data/data/com.npuja.nikhil/files/.bashrc", NULL]
        char *argv[] = {
            "bash",
            "-login",
            
            NULL
        };

        // env: custom environment
        char *envp[] = {
            "LD_LIBRARY_PATH=/data/data/com.npuja.nikhil/files/usr/lib",
            "HOME=/data/data/com.npuja.nikhil/files/home",
            "PREFIX=/data/data/com.npuja.nikhil/files/usr",
            "PATH=/data/data/com.npuja.nikhil/files/usr/bin:/system/bin",
            "TERM=xterm-256color",
            NULL
        };

        // Working directory change (optional)
        chdir(app_dir);

        // Execute shell
        execve(shell, argv, envp);

        // If execve fails
        perror("execve failed");
        exit(1);
    }
    else if (pid > 0) {
        // ✅ PARENT
        child_pid = pid;
        (*env)->ReleaseStringUTFChars(env, shell_path, shell);
        return master_fd;
    }
    else {
        return -1; // Fork failed
    }
}

JNIEXPORT jint JNICALL
Java_com_npuja_nikhil_NativePTY_writeToShell(JNIEnv *env, jobject thiz, jstring input) {
    const char *str = (*env)->GetStringUTFChars(env, input, 0);
    int len = strlen(str);
    int written = write(master_fd, str, len);
    write(master_fd, "\n", 1); // Simulate Enter
    (*env)->ReleaseStringUTFChars(env, input, str);
    return written;
}

JNIEXPORT jstring JNICALL
Java_com_npuja_nikhil_NativePTY_readFromShell(JNIEnv *env, jobject thiz) {
    char buffer[1024];
    int count = read(master_fd, buffer, sizeof(buffer) - 1);
    if (count <= 0) return (*env)->NewStringUTF(env, "");
    buffer[count] = '\0';
    return (*env)->NewStringUTF(env, buffer);
}