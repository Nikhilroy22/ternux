# Compiler
CC = clang

# Output file
OUT_DIR = ./app/src/main/jniLibs/armeabi-v7a

TARGET = $(OUT_DIR)/libmylib.so

# Source files
KK = ./lib
SRCS = $(KK)/mylib.c

# Compiler flags
CFLAGS = -fPIC
LDFLAGS = -shared

# Build rule


# Default rule
all: $(TARGET) gradle_build

# Step 1: Build .so
$(TARGET): $(SRCS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(SRCS) -o $(TARGET)

# Step 2: Build APK via Gradle
gradle_build:
	bash ./gradlew assembleDebug


# Clean rule
clean:
	rm -f $(TARGET)