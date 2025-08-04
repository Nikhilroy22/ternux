#include <stdio.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>

#define COLOR_RESET  "\033[0m"
#define COLOR_BLUE   "\033[34m"
#define COLOR_GREEN  "\033[32m"
#define COLOR_WHITE  "\033[37m"

void print_colored(const char *name, const char *path) {
    struct stat st;
    char fullpath[1024];
    snprintf(fullpath, sizeof(fullpath), "%s/%s", path, name);

    if (stat(fullpath, &st) == 0) {
        if (S_ISDIR(st.st_mode)) {
            printf(COLOR_BLUE "%s" COLOR_RESET "\n", name);
        } else if (st.st_mode & S_IXUSR) {
            printf(COLOR_GREEN "%s" COLOR_RESET "\n", name);
        } else {
            printf(COLOR_WHITE "%s" COLOR_RESET "\n", name);
        }
    }
}

int main(int argc, char *argv[]) {
    const char *path = ".";

    if (argc > 1) path = argv[1];

    DIR *dir = opendir(path);
    if (!dir) {
        perror("opendir");
        return 1;
    }

    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {
        // Hide . and .. by default
        if (entry->d_name[0] == '.' && strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0)
            continue;
        print_colored(entry->d_name, path);
    }

    closedir(dir);
    return 0;
}
